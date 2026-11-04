package learning_management_system.backend.serviceimpl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import learning_management_system.backend.dto.BlueprintSyncRunDto;
import learning_management_system.backend.dto.StartBlueprintSyncRequest;
import learning_management_system.backend.entity.*;
import learning_management_system.backend.enums.*;
import learning_management_system.backend.exception.ApiException;
import learning_management_system.backend.repository.*;
import learning_management_system.backend.security.Access;
import learning_management_system.backend.security.CourseAccess;
import learning_management_system.backend.security.SystemTx;
import learning_management_system.backend.service.AuditLogService;
import learning_management_system.backend.service.BlueprintSyncRunService;
import learning_management_system.backend.utility.BlueprintCopyMap;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.*;
import java.util.function.Consumer;

/**
 * Pushes a blueprint course to its associated courses, one course per transaction, so one failing course does not
 * undo the others. Per item and aspect (see {@link BlueprintLockAspect}): a locked aspect is always overwritten; an
 * unlocked one only when the course has not changed the item since the last sync. Otherwise the course's version is
 * kept and recorded as an exception.
 */
@Service
public class BlueprintSyncRunServiceImplementation implements BlueprintSyncRunService {
    private static final Set<BlueprintSyncRunStatus> ACTIVE = EnumSet.of(BlueprintSyncRunStatus.QUEUED, BlueprintSyncRunStatus.RUNNING);
    private static final String MODULE = "MODULE", CONTENT = "CONTENT", ASSIGNMENT = "ASSIGNMENT";

    private final org.springframework.transaction.support.TransactionTemplate tx;
    private final BlueprintSyncRunRepository runs;
    private final BlueprintAssociationRepository associations;
    private final BlueprintContentLockRepository locks;
    private final CourseRepository courses;
    private final CourseModuleRepository modules;
    private final ContentRepository contents;
    private final VideoLectureRepository videos;
    private final AssignmentRepository assignments;
    private final AssignmentSubmissionRepository submissions;
    private final ModulePrerequisiteRepository modulePrerequisites;
    private final ModuleCompletionRequirementRepository requirements;
    private final CourseAccess access;
    private final SystemTx sys;
    private final ObjectMapper json;
    private final AuditLogService audit;

    private final ContentBlockRepository blocks;

    public BlueprintSyncRunServiceImplementation(BlueprintSyncRunRepository runs, BlueprintAssociationRepository associations,
                                                 BlueprintContentLockRepository locks, CourseRepository courses, CourseModuleRepository modules,
                                                 ContentRepository contents, VideoLectureRepository videos, AssignmentRepository assignments,
                                                 AssignmentSubmissionRepository submissions, ModulePrerequisiteRepository modulePrerequisites,
                                                 ModuleCompletionRequirementRepository requirements, CourseAccess access, SystemTx sys,
                                                 ObjectMapper json, AuditLogService audit,
                                                 org.springframework.transaction.PlatformTransactionManager tm,
            ContentBlockRepository blocks) {
        this.blocks = blocks;
        this.tx = new org.springframework.transaction.support.TransactionTemplate(tm);
        this.runs = runs;
        this.associations = associations;
        this.locks = locks;
        this.courses = courses;
        this.modules = modules;
        this.contents = contents;
        this.videos = videos;
        this.assignments = assignments;
        this.submissions = submissions;
        this.modulePrerequisites = modulePrerequisites;
        this.requirements = requirements;
        this.access = access;
        this.sys = sys;
        this.json = json;
        this.audit = audit;
    }

    @Transactional(readOnly = true)
    @Override
    public List<BlueprintSyncRunDto> list(UUID blueprintCourseId) {
        blueprint(blueprintCourseId);
        access.requireCourseEditor(blueprintCourseId);
        return runs.findByBlueprintCourseIdOrderByDateCreatedDesc(blueprintCourseId).stream().map(this::toDto).toList();
    }

    @Transactional(readOnly = true)
    @Override
    public BlueprintSyncRunDto get(UUID blueprintCourseId, UUID syncRunId) {
        access.requireCourseEditor(blueprintCourseId);
        return toDto(runs.findBySyncRunIdAndBlueprintCourseId(syncRunId, blueprintCourseId).orElseThrow(() -> ApiException.notFound("Sync run")));
    }

    /** Not transactional: each associated course is synced in its own transaction. */
    @Override
    public BlueprintSyncRunDto sync(UUID blueprintCourseId, StartBlueprintSyncRequest r) {
        Access.requireTenant();
        UUID tenantId = Access.ctx().tenantId();
        UUID me = Access.ctx().userId();
        Course bp = inRequest(() -> {
            Course c = blueprint(blueprintCourseId);
            access.requireCourseEditor(blueprintCourseId);
            return c;
        });
        BlueprintSyncRun run = sys.inTenant(tenantId, () -> {
            if (runs.existsByBlueprintCourseIdAndRunStatusIn(blueprintCourseId, ACTIVE)) {
                throw ApiException.conflict("SYNC_RUNNING", "A sync of this blueprint is already running");
            }
            if (associations.findByBlueprintCourseIdAndIsActiveTrue(blueprintCourseId).isEmpty()) {
                throw ApiException.conflict("NO_ASSOCIATED_COURSES", "Associate courses with the blueprint first");
            }
            BlueprintSyncRun n = new BlueprintSyncRun();
            n.setBlueprintCourseId(blueprintCourseId);
            n.setInitiatedBy(me);
            n.setSyncComment(r == null ? null : r.syncComment());
            n.setIncludeCourseSettings(r != null && Boolean.TRUE.equals(r.includeCourseSettings()));
            n.setNotifyUsers(r != null && Boolean.TRUE.equals(r.notifyUsers()));
            n.setRunStatus(BlueprintSyncRunStatus.RUNNING);
            n.setDateStart(OffsetDateTime.now());
            return runs.saveAndFlush(n);
        });

        Map<UUID, Map<String, UUID>> copies = sys.inTenant(tenantId, () -> runs
                .findFirstByBlueprintCourseIdAndRunStatusInOrderByDateCreatedDesc(blueprintCourseId, BlueprintContentLockServiceImplementation.FINISHED)
                .map(x -> BlueprintCopyMap.read(x.getChanges())).orElse(new HashMap<>()));
        ArrayNode exceptions = json.createArrayNode();
        ObjectNode perCourse = json.createObjectNode();
        int synced = 0, failed = 0;
        List<BlueprintAssociation> targets = sys.inTenant(tenantId, () -> associations.findByBlueprintCourseIdAndIsActiveTrue(blueprintCourseId));
        for (BlueprintAssociation a : targets) {
            Map<String, UUID> map = new HashMap<>(copies.getOrDefault(a.getAssociatedCourseId(), Map.of()));
            ArrayNode courseExceptions = json.createArrayNode();
            try {
                ObjectNode counts = sys.inTenant(tenantId, () -> syncCourse(bp, a.getBlueprintAssociationId(), map, courseExceptions,
                        Boolean.TRUE.equals(run.getIncludeCourseSettings())));
                copies.put(a.getAssociatedCourseId(), map);
                perCourse.set(a.getAssociatedCourseId().toString(), counts);
                synced++;
            } catch (RuntimeException e) {
                failed++;
                ObjectNode x = courseExceptions.addObject();
                x.put("reason", "SYNC_FAILED");
                x.put("message", e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage());
            }
            for (JsonNode x : courseExceptions) {
                ObjectNode o = ((ObjectNode) x).deepCopy();
                o.put("courseId", a.getAssociatedCourseId().toString());
                exceptions.add(o);
            }
        }
        int ok = synced;
        int bad = failed;
        BlueprintSyncRun done = sys.inTenant(tenantId, () -> {
            BlueprintSyncRun x = runs.findById(run.getSyncRunId()).orElseThrow();
            ObjectNode changes = json.createObjectNode();
            changes.set("courses", perCourse);
            changes.set("copies", BlueprintCopyMap.write(json, copies));
            x.setChanges(changes);
            x.setExceptions(exceptions);
            x.setCoursesSynced(ok);
            x.setRunStatus(ok == 0 ? BlueprintSyncRunStatus.FAILED
                    : exceptions.isEmpty() ? BlueprintSyncRunStatus.COMPLETED : BlueprintSyncRunStatus.COMPLETED_WITH_EXCEPTIONS);
            x.setDateEnd(OffsetDateTime.now());
            return runs.saveAndFlush(x);
        });
        inRequest(() -> {
            audit.record("BLUEPRINT_SYNCED", "COURSE", blueprintCourseId, null, bad > 0 ? Severity.WARNING : Severity.INFO, null,
                    Map.of("coursesSynced", ok, "failed", bad, "exceptions", exceptions.size()));
            return null;
        });
        return toDto(done);
    }

    // ================================================================ one associated course

    private ObjectNode syncCourse(Course bp, UUID associationId, Map<String, UUID> map, ArrayNode exceptions, boolean includeSettings) {
        BlueprintAssociation a = associations.findById(associationId).orElseThrow();
        Course child = courses.findById(a.getAssociatedCourseId()).orElseThrow();
        Ctx x = new Ctx(child, a.getDateLastSynced(), map, exceptions, lockIndex(bp.getCourseId()));

        // 1. modules (content and assignments need their copies)
        List<CourseModule> bpModules = modules.findByCourseIdOrderByDisplayOrderAscModuleNumberAsc(bp.getCourseId());
        Set<UUID> modulesWritten = new HashSet<>();
        for (CourseModule m : bpModules) {
            if (m.getSectionId() != null) continue;                       // section-only modules belong to the blueprint's sections
            CourseModule copy = syncModule(x, m);
            if (copy != null && x.wroteSettings) modulesWritten.add(m.getModuleId());
        }
        // 2. content and assignments
        for (Content c : contents.findByCourseIdOrderByDisplayOrderAscContentTitleAsc(bp.getCourseId())) syncContent(x, c);
        for (Assignment s : assignments.findByCourseIdOrderByDateDueAscAssignmentTitleAsc(bp.getCourseId())) syncAssignment(x, s);
        // 3. module rules (prerequisites and requirements) of modules whose settings were written
        for (CourseModule m : bpModules) if (modulesWritten.contains(m.getModuleId())) syncModuleRules(x, m);
        // 4. items deleted from the blueprint
        Set<String> present = new HashSet<>();
        bpModules.forEach(m -> present.add(BlueprintCopyMap.key(MODULE, m.getModuleId())));
        contents.findByCourseIdOrderByDisplayOrderAscContentTitleAsc(bp.getCourseId()).forEach(c -> present.add(BlueprintCopyMap.key(CONTENT, c.getContentId())));
        assignments.findByCourseIdOrderByDateDueAscAssignmentTitleAsc(bp.getCourseId()).forEach(s -> present.add(BlueprintCopyMap.key(ASSIGNMENT, s.getAssignmentId())));
        for (String key : new ArrayList<>(map.keySet())) if (!present.contains(key)) removeCopy(x, key);
        // 5. course settings
        if (includeSettings) syncSettings(x, bp);

        a.setDateLastSynced(OffsetDateTime.now());
        associations.saveAndFlush(a);
        ObjectNode counts = json.createObjectNode();
        counts.put("created", x.created);
        counts.put("updated", x.updated);
        counts.put("deleted", x.deleted);
        counts.put("kept", x.kept);
        return counts;
    }

    // ---------------------------------------------------------------- modules

    private CourseModule syncModule(Ctx x, CourseModule m) {
        String key = BlueprintCopyMap.key(MODULE, m.getModuleId());
        CourseModule copy = existing(x, key, modules::findById, CourseModule::getCourseId);
        BlueprintContentLock lock = x.locks.get(key);
        x.wroteSettings = false;
        if (copy == null) {
            if (x.map.containsKey(key) && lock == null) { exception(x, MODULE, m.getModuleId(), m.getModuleTitle(), "DELETED_IN_COURSE"); x.map.remove(key); x.kept++; return null; }
            copy = new CourseModule();
            copy.setCourseId(x.child.getCourseId());
            copy.setModuleNumber(modules.maxModuleNumber(x.child.getCourseId()) + 1);
            moduleContent(m, copy);
            moduleAvailability(m, copy);
            moduleSettings(m, copy);
            copy = modules.saveAndFlush(copy);
            x.map.put(key, copy.getModuleId());
            x.created++;
            x.wroteSettings = true;
            return copy;
        }
        CourseModule c = copy;
        Aspects w = write(x, lock, c.getDateUpdated(), m.getModuleId(), MODULE, m.getModuleTitle());
        if (w.content) moduleContent(m, c);
        if (w.availability) moduleAvailability(m, c);
        if (w.settings) moduleSettings(m, c);
        x.wroteSettings = w.settings;
        if (w.any()) { modules.saveAndFlush(c); x.updated++; }
        return c;
    }

    private static void moduleContent(CourseModule from, CourseModule to) {
        to.setModuleTitle(from.getModuleTitle());
        to.setModuleDescription(from.getModuleDescription());
    }

    private static void moduleAvailability(CourseModule from, CourseModule to) {
        to.setDateStart(from.getDateStart());
        to.setDateEnd(from.getDateEnd());
        to.setDateUnlock(from.getDateUnlock());
        to.setIsPublished(from.getIsPublished());
    }

    private static void moduleSettings(CourseModule from, CourseModule to) {
        to.setModuleType(from.getModuleType());
        to.setIsLocked(from.getIsLocked());
        to.setRequireSequentialProgress(from.getRequireSequentialProgress());
        to.setDisplayOrder(from.getDisplayOrder());
    }

    /** Prerequisites and completion requirements, translated to the course's copies (items without a copy are left out). */
    private void syncModuleRules(Ctx x, CourseModule m) {
        UUID copyId = x.map.get(BlueprintCopyMap.key(MODULE, m.getModuleId()));
        if (copyId == null) return;
        modulePrerequisites.deleteByModuleId(copyId);
        for (ModulePrerequisite p : modulePrerequisites.findByModuleId(m.getModuleId())) {
            UUID pre = x.map.get(BlueprintCopyMap.key(MODULE, p.getPrerequisiteModuleId()));
            if (pre == null) continue;
            ModulePrerequisite n = new ModulePrerequisite();
            n.setModuleId(copyId);
            n.setPrerequisiteModuleId(pre);
            modulePrerequisites.save(n);
        }
        requirements.findByModuleIdOrderByDisplayOrderAsc(copyId).forEach(requirements::delete);
        requirements.flush();
        for (ModuleCompletionRequirement q : requirements.findByModuleIdOrderByDisplayOrderAsc(m.getModuleId())) {
            UUID content = q.getContentId() == null ? null : x.map.get(BlueprintCopyMap.key(CONTENT, q.getContentId()));
            UUID assignment = q.getAssignmentId() == null ? null : x.map.get(BlueprintCopyMap.key(ASSIGNMENT, q.getAssignmentId()));
            if (content == null && assignment == null) continue;          // quiz and discussion requirements come with those features
            ModuleCompletionRequirement n = new ModuleCompletionRequirement();
            n.setModuleId(copyId);
            n.setContentId(content);
            n.setAssignmentId(assignment);
            n.setRequirementType(q.getRequirementType());
            n.setMinScore(q.getMinScore());
            n.setDisplayOrder(q.getDisplayOrder());
            requirements.save(n);
        }
        requirements.flush();
    }

    // ---------------------------------------------------------------- content

    private void syncContent(Ctx x, Content c) {
        String key = BlueprintCopyMap.key(CONTENT, c.getContentId());
        Content copy = existing(x, key, contents::findById, Content::getCourseId);
        BlueprintContentLock lock = x.locks.get(key);
        if (copy == null) {
            if (x.map.containsKey(key) && lock == null) { exception(x, CONTENT, c.getContentId(), c.getContentTitle(), "DELETED_IN_COURSE"); x.map.remove(key); x.kept++; return; }
            Content n = new Content();
            n.setCourseId(x.child.getCourseId());
            n.setIsFrontPage(Boolean.TRUE.equals(c.getIsFrontPage()) && contents.findFirstByCourseIdAndIsFrontPageTrue(x.child.getCourseId()).isEmpty());
            contentContent(c, n);
            contentAvailability(c, n);
            contentSettings(x, c, n);
            n = contents.saveAndFlush(n);
            copyVideo(c.getContentId(), n.getContentId());
            copyBlocks(c.getContentId(), n.getContentId());
            x.map.put(key, n.getContentId());
            x.created++;
            return;
        }
        Aspects w = write(x, lock, copy.getDateUpdated(), c.getContentId(), CONTENT, c.getContentTitle());
        if (w.content) { contentContent(c, copy); copyVideo(c.getContentId(), copy.getContentId()); copyBlocks(c.getContentId(), copy.getContentId()); }
        if (w.availability) {
            if (Boolean.TRUE.equals(copy.getIsFrontPage())) copy.setIsPublished(true);     // a front page is always published
            else contentAvailability(c, copy);
        }
        if (w.settings) contentSettings(x, c, copy);
        if (w.any()) { contents.saveAndFlush(copy); x.updated++; }
    }

    private static void contentContent(Content from, Content to) {
        to.setContentTitle(from.getContentTitle());
        to.setContentDescription(from.getContentDescription());
        to.setContentBody(from.getContentBody());
        to.setContentHtml(from.getContentHtml() == null ? null : from.getContentHtml().deepCopy());
    }

    private static void contentAvailability(Content from, Content to) {
        to.setDateStart(from.getDateStart());
        to.setDateEnd(from.getDateEnd());
        to.setIsPublished(from.getIsPublished());
        to.setDatePublished(from.getDatePublished());
    }

    private static void contentSettings(Ctx x, Content from, Content to) {
        to.setTypeId(from.getTypeId());
        to.setEditingRoles(from.getEditingRoles());
        to.setAccessLevel(from.getAccessLevel());
        to.setEstimatedDurationMinutes(from.getEstimatedDurationMinutes());
        to.setIsLocked(from.getIsLocked());
        to.setDisplayOrder(from.getDisplayOrder());
        to.setModuleId(from.getModuleId() == null ? null : x.map.get(BlueprintCopyMap.key(MODULE, from.getModuleId())));
    }

    /** The page's blocks are part of its content: the copy gets exactly the blueprint's blocks. */
    private void copyBlocks(UUID fromContentId, UUID toContentId) {
        blocks.findByContentIdOrderByDisplayOrderAscBlockNumberAsc(toContentId).forEach(blocks::delete);
        blocks.flush();
        for (ContentBlock b : blocks.findByContentIdOrderByDisplayOrderAscBlockNumberAsc(fromContentId)) {
            ContentBlock n = new ContentBlock();
            n.setContentId(toContentId);
            n.setBlockNumber(b.getBlockNumber());
            n.setBlockType(b.getBlockType());
            n.setBlockTitle(b.getBlockTitle());
            n.setBlockContent(b.getBlockContent());
            n.setBlockContentHtml(b.getBlockContentHtml() == null ? null : b.getBlockContentHtml().deepCopy());
            n.setDisplayOrder(b.getDisplayOrder());
            n.setMetadata(b.getMetadata() == null ? null : b.getMetadata().deepCopy());
            blocks.save(n);
        }
        blocks.flush();
    }

    private void copyVideo(UUID fromContentId, UUID toContentId) {
        VideoLecture v = videos.findFirstByContentId(fromContentId).orElse(null);
        VideoLecture t = videos.findFirstByContentId(toContentId).orElse(null);
        if (v == null) { if (t != null) videos.delete(t); return; }
        if (t == null) { t = new VideoLecture(); t.setContentId(toContentId); }
        t.setVideoUrl(v.getVideoUrl());
        t.setVideoProvider(v.getVideoProvider());
        t.setVideoDurationSeconds(v.getVideoDurationSeconds());
        t.setThumbnailUrl(v.getThumbnailUrl());
        t.setTranscript(v.getTranscript());
        t.setTranscriptUrl(v.getTranscriptUrl());
        t.setCaptionsUrl(v.getCaptionsUrl());
        t.setCaptionsAvailable(v.getCaptionsAvailable());
        t.setQualityAvailable(v.getQualityAvailable());
        videos.saveAndFlush(t);
    }

    // ---------------------------------------------------------------- assignments

    private void syncAssignment(Ctx x, Assignment s) {
        String key = BlueprintCopyMap.key(ASSIGNMENT, s.getAssignmentId());
        Assignment copy = existing(x, key, assignments::findById, Assignment::getCourseId);
        BlueprintContentLock lock = x.locks.get(key);
        if (copy == null) {
            if (x.map.containsKey(key) && lock == null) { exception(x, ASSIGNMENT, s.getAssignmentId(), s.getAssignmentTitle(), "DELETED_IN_COURSE"); x.map.remove(key); x.kept++; return; }
            Assignment n = new Assignment();
            n.setCourseId(x.child.getCourseId());
            assignmentContent(s, n);
            assignmentPoints(s, n);
            assignmentDue(s, n);
            assignmentAvailability(s, n);
            assignmentSettings(x, s, n);
            n = assignments.saveAndFlush(n);
            x.map.put(key, n.getAssignmentId());
            x.created++;
            if (Boolean.TRUE.equals(s.getAllowGroupSubmission())) exception(x, ASSIGNMENT, s.getAssignmentId(), s.getAssignmentTitle(), "CHOOSE_GROUP_SET");
            return;
        }
        Aspects w = write(x, lock, copy.getDateUpdated(), s.getAssignmentId(), ASSIGNMENT, s.getAssignmentTitle());
        if (w.content) assignmentContent(s, copy);
        if (w.points) assignmentPoints(s, copy);
        if (w.due) assignmentDue(s, copy);
        if (w.availability) {
            boolean unpublish = Boolean.TRUE.equals(copy.getIsPublished()) && !Boolean.TRUE.equals(s.getIsPublished());
            if (unpublish && submissions.existsByAssignmentIdAndSubmissionStatusNot(copy.getAssignmentId(), AssignmentSubmissionStatus.DRAFT)) {
                exception(x, ASSIGNMENT, s.getAssignmentId(), s.getAssignmentTitle(), "HAS_SUBMISSIONS");
                copy.setDateAvailable(s.getDateAvailable());
            } else {
                assignmentAvailability(s, copy);
            }
        }
        if (w.settings) assignmentSettings(x, s, copy);
        if (w.any()) { assignments.saveAndFlush(copy); x.updated++; }
    }

    private static void assignmentContent(Assignment from, Assignment to) {
        to.setAssignmentTitle(from.getAssignmentTitle());
        to.setAssignmentDescription(from.getAssignmentDescription());
        to.setAssignmentInstructions(from.getAssignmentInstructions());
    }

    private static void assignmentPoints(Assignment from, Assignment to) {
        to.setPointsPossible(from.getPointsPossible());
        to.setGradingType(from.getGradingType());
        to.setOmitFromFinalGrade(from.getOmitFromFinalGrade());
    }

    private static void assignmentDue(Assignment from, Assignment to) {
        to.setDateDue(from.getDateDue());
        to.setDateClose(from.getDateClose());
    }

    private static void assignmentAvailability(Assignment from, Assignment to) {
        to.setDateAvailable(from.getDateAvailable());
        to.setIsPublished(from.getIsPublished());
        to.setAssignmentStatus(from.getAssignmentStatus());
    }

    private static void assignmentSettings(Ctx x, Assignment from, Assignment to) {
        to.setAssignmentType(from.getAssignmentType());
        to.setDisplayOrder(from.getDisplayOrder());
        to.setModuleId(from.getModuleId() == null ? null : x.map.get(BlueprintCopyMap.key(MODULE, from.getModuleId())));
        to.setRubricTemplateId(from.getRubricTemplateId());
        to.setIsAnonymousGrading(from.getIsAnonymousGrading());
        to.setIsModeratedGrading(from.getIsModeratedGrading());
        to.setGraderCount(from.getGraderCount());
        to.setGradersAnonymousToGraders(from.getGradersAnonymousToGraders());
        to.setAllowSubmissions(from.getAllowSubmissions());
        to.setSubmissionType(from.getSubmissionType());
        to.setAllowedSubmissionTypes(from.getAllowedSubmissionTypes() == null ? null : from.getAllowedSubmissionTypes().clone());
        to.setAllowedFileExtensions(from.getAllowedFileExtensions() == null ? null : from.getAllowedFileExtensions().clone());
        to.setExternalToolId(from.getExternalToolId());
        to.setMaxSubmissions(from.getMaxSubmissions());
        to.setLateSubmissionAllowed(from.getLateSubmissionAllowed());
        to.setLateSubmissionPenaltyPercent(from.getLateSubmissionPenaltyPercent());
        to.setGradeGroupIndividually(from.getGradeGroupIndividually());
        to.setGroupSize(from.getGroupSize());
        to.setPeerReviewRequired(from.getPeerReviewRequired());
        to.setPeerReviewCount(from.getPeerReviewCount());
        to.setPlagiarismDetectionEnabled(from.getPlagiarismDetectionEnabled());
        to.setPlagiarismDetectionTool(from.getPlagiarismDetectionTool());
        if (to.getGroupSetId() == null) to.setAllowGroupSubmission(false);   // group sets belong to each course: the course chooses one
    }

    // ---------------------------------------------------------------- deletions and settings

    /** The blueprint deleted the item: delete the copy unless the course changed it (or students handed in work). */
    private void removeCopy(Ctx x, String key) {
        UUID copyId = x.map.remove(key);
        String type = key.substring(0, key.indexOf(':'));
        UUID bpId = UUID.fromString(key.substring(key.indexOf(':') + 1));
        switch (type) {
            case MODULE -> modules.findById(copyId).filter(m -> m.getCourseId().equals(x.child.getCourseId())).ifPresent(m -> {
                if (x.changed(m.getDateUpdated())) { exception(x, type, bpId, m.getModuleTitle(), "KEPT_CHANGED_IN_COURSE"); x.kept++; }
                else { modules.delete(m); x.deleted++; }
            });
            case CONTENT -> contents.findById(copyId).filter(c -> c.getCourseId().equals(x.child.getCourseId())).ifPresent(c -> {
                if (x.changed(c.getDateUpdated()) || Boolean.TRUE.equals(c.getIsFrontPage())) {
                    exception(x, type, bpId, c.getContentTitle(), Boolean.TRUE.equals(c.getIsFrontPage()) ? "KEPT_FRONT_PAGE" : "KEPT_CHANGED_IN_COURSE");
                    x.kept++;
                } else { contents.delete(c); x.deleted++; }
            });
            case ASSIGNMENT -> assignments.findById(copyId).filter(a -> a.getCourseId().equals(x.child.getCourseId())).ifPresent(a -> {
                if (submissions.existsByAssignmentIdAndSubmissionStatusNot(a.getAssignmentId(), AssignmentSubmissionStatus.DRAFT)) {
                    exception(x, type, bpId, a.getAssignmentTitle(), "KEPT_HAS_SUBMISSIONS"); x.kept++;
                } else if (x.changed(a.getDateUpdated())) {
                    exception(x, type, bpId, a.getAssignmentTitle(), "KEPT_CHANGED_IN_COURSE"); x.kept++;
                } else { assignments.delete(a); x.deleted++; }
            });
            default -> { }
        }
    }

    private void syncSettings(Ctx x, Course bp) {
        Course c = courses.findById(x.child.getCourseId()).orElseThrow();
        c.setCourseDescription(bp.getCourseDescription());
        c.setSyllabusHtml(bp.getSyllabusHtml());
        c.setHomePageType(bp.getHomePageType());
        c.setCourseImageUrl(bp.getCourseImageUrl());
        c.setIsPublicSyllabus(bp.getIsPublicSyllabus());
        c.setContentLicense(bp.getContentLicense());
        c.setGradeCalculationMethod(bp.getGradeCalculationMethod());
        c.setDefaultPostPolicy(bp.getDefaultPostPolicy());
        c.setTimeZone(bp.getTimeZone());
        courses.saveAndFlush(c);
    }

    // ================================================================ helpers

    /** Which aspects of an existing copy to overwrite: locked ones always, the rest only when the course left it unchanged. */
    private Aspects write(Ctx x, BlueprintContentLock lock, OffsetDateTime copyUpdated, UUID bpId, String type, String title) {
        boolean changed = x.changed(copyUpdated);
        Aspects w = new Aspects();
        w.content = !changed || (lock != null && BlueprintContentLockServiceImplementation.covers(lock, BlueprintLockAspect.CONTENT));
        w.points = !changed || (lock != null && BlueprintContentLockServiceImplementation.covers(lock, BlueprintLockAspect.POINTS));
        w.due = !changed || (lock != null && BlueprintContentLockServiceImplementation.covers(lock, BlueprintLockAspect.DUE_DATES));
        w.availability = !changed || (lock != null && BlueprintContentLockServiceImplementation.covers(lock, BlueprintLockAspect.AVAILABILITY));
        w.settings = !changed || (lock != null && BlueprintContentLockServiceImplementation.covers(lock, BlueprintLockAspect.SETTINGS));
        boolean assignment = ASSIGNMENT.equals(type);
        boolean skipped = !w.content || !w.availability || !w.settings || (assignment && (!w.points || !w.due));
        if (skipped) { exception(x, type, bpId, title, "CHANGED_IN_COURSE"); x.kept++; }
        return w;
    }

    private <T> T existing(Ctx x, String key, java.util.function.Function<UUID, Optional<T>> find,
                           java.util.function.Function<T, UUID> courseOf) {
        UUID id = x.map.get(key);
        if (id == null) return null;
        return find.apply(id).filter(e -> courseOf.apply(e).equals(x.child.getCourseId())).orElse(null);
    }

    private void exception(Ctx x, String type, UUID bpId, String title, String reason) {
        ObjectNode n = x.exceptions.addObject();
        n.put("objectType", type);
        n.put("objectId", bpId.toString());
        n.put("title", title);
        n.put("reason", reason);
    }

    private Map<String, BlueprintContentLock> lockIndex(UUID blueprintCourseId) {
        Map<String, BlueprintContentLock> out = new HashMap<>();
        for (BlueprintContentLock l : locks.findByBlueprintCourseIdOrderByDateCreatedAsc(blueprintCourseId)) {
            out.put(BlueprintCopyMap.key(l.getObjectType().name(), l.getObjectId()), l);
        }
        return out;
    }

    private Course blueprint(UUID id) {
        Course c = courses.findById(id).orElseThrow(() -> ApiException.notFound("Course"));
        if (c.getCourseKind() != CourseKind.BLUEPRINT) throw ApiException.badRequest("NOT_A_BLUEPRINT", "The course is not a blueprint course");
        return c;
    }

    /** Runs a check in a transaction of the signed-in caller (row-level security as the user). */
    private <T> T inRequest(java.util.function.Supplier<T> work) {
        return tx.execute(s -> work.get());
    }

    private BlueprintSyncRunDto toDto(BlueprintSyncRun r) {
        JsonNode changes = r.getChanges();
        if (changes != null && changes.has("copies")) {
            changes = changes.deepCopy();
            ((ObjectNode) changes).remove("copies");                      // internal bookkeeping
        }
        return new BlueprintSyncRunDto(r.getSyncRunId(), r.getBlueprintCourseId(), r.getInitiatedBy(), r.getSyncComment(),
                r.getIncludeCourseSettings(), r.getNotifyUsers(), r.getRunStatus(), changes, r.getCoursesSynced(), r.getExceptions(),
                r.getDateStart(), r.getDateEnd(), r.getDateCreated());
    }

    /** State of one course's sync. */
    private static final class Ctx {
        final Course child;
        final OffsetDateTime lastSynced;
        final Map<String, UUID> map;
        final ArrayNode exceptions;
        final Map<String, BlueprintContentLock> locks;
        int created, updated, deleted, kept;
        boolean wroteSettings;

        Ctx(Course child, OffsetDateTime lastSynced, Map<String, UUID> map, ArrayNode exceptions, Map<String, BlueprintContentLock> locks) {
            this.child = child;
            this.lastSynced = lastSynced;
            this.map = map;
            this.exceptions = exceptions;
            this.locks = locks;
        }

        /** Did the course change the item after the last sync? (Never synced: no.) */
        boolean changed(OffsetDateTime updated) {
            return lastSynced != null && updated != null && updated.isAfter(lastSynced);
        }
    }

    private static final class Aspects {
        boolean content, points, due, availability, settings;

        boolean any() { return content || points || due || availability || settings; }
    }
}
