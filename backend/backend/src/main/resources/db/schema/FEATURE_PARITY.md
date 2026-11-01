# Feature parity: our LMS vs Canvas and Blackboard Ultra

Updated Oct 31, 2026 · schema v3.1 (264 tables in 30 domains)

**Legend:** ✅ already in the schema · ➕ added in v2 · 🖥️ app-layer only (no tables needed) · C = Canvas · B = Blackboard Ultra

## 1. Institution, courses and people
| Feature | C | B | Status | Where |
|---|---|---|---|---|
| Multi-institution tenancy, branding, domains | ✅ | ✅ | ✅ | tenant, tenant_branding, tenant_domain |
| Sub-accounts / institutional hierarchy | ✅ | ✅ | ✅ | org_unit (+ `branding` ➕) |
| Terms, sections, cross-listing | ✅ | ✅ | ✅ / ➕ | academic_term, course_section (`crosslisted_from_course_id` ➕) |
| Blueprint courses (lock + sync to many courses) | ✅ | — | ➕ | blueprint_association, blueprint_content_lock, blueprint_sync_run |
| Course templates, sandboxes | ✅ | ✅ | ➕ | course.course_kind |
| Organizations (non-course spaces: clubs, committees) | — | ✅ | ➕ | course.course_kind = ORGANIZATION |
| Course settings: home page, image, self-enrollment code, public syllabus, license, time zone | ✅ | ✅ | ➕ | course columns |
| Course navigation (tab order/visibility) | ✅ | ✅ | ➕ | course_navigation_item |
| Enrollments: student, TA, teacher, designer, observer, auditor | ✅ | ✅ | ✅ | enrollment, course_instructor |
| Section-limited access, last activity, total activity time | ✅ | ✅ | ➕ | enrollment columns |
| Observer pairing codes | ✅ | — | ➕ | observer_pairing_code |
| Student view / test student | ✅ | ✅ | ➕ | users.is_test_user |
| Multiple logins per person (SIS + SSO + local) | ✅ | ✅ | ➕ | user_login |
| Platform staff separated from institution users, with auto-created persona profiles | ✅ | ✅ | ➕ v3 | users (`is_system_user`, `user_type`), user_profile, platform_user_profile, student_profile, employee_profile, academic_advisor_profile |
| Audited support access / "act as" for platform staff | ✅ | ✅ | ➕ v3 | platform_support_access, audit_log.actor_type |
| Observers (parents, mentors, athletics/success advisors) linked to students with consent | ✅ | ✅ | ➕ v3 | observer_student_link |
| SSO (SAML / OIDC), MFA, lockout | ✅ | ✅ | ✅ | sso_configuration, mfa_configuration, account_lock |
| Roles and permissions at tenant / sub-account / course / section | ✅ | ✅ | ✅ / ➕ | roles, permissions, user_role (ORG_UNIT/TENANT scope ➕) |
| Groups, group sets, self-signup | ✅ | ✅ | ✅ | group_set, student_group, group_members |
| Differentiation tags (hidden student labels for "assign to") | ✅ | — | ➕ | differentiation_tag, differentiation_tag_member |
| Waitlists | — | — | ✅ | waitlist (beyond both) |

## 2. Content and learning paths
| Feature | C | B | Status | Where |
|---|---|---|---|---|
| Pages / documents, front page, page edit roles | ✅ | ✅ | ✅ / ➕ | content (`is_front_page`, `editing_roles` ➕), content_block |
| Revision history | ✅ | ✅ | ✅ | entity_version |
| Modules / learning modules, sequential progress | ✅ | ✅ | ✅ | course_module |
| Module prerequisites and completion requirements | ✅ | ✅ | ✅ | module_prerequisite, module_completion_requirement, student_module_progress |
| Release conditions (date, member, group, tag, performance, submitted, viewed; multiple criteria) | ✅ | ✅ | ➕ | release_condition |
| Mastery Paths (score ranges route to different items) | ✅ | ✅ | ➕ | mastery_path_rule, mastery_path_range, mastery_path_set_item |
| Course pacing | ✅ | — | ➕ | course_pace, course_pace_item |
| Files: folders, quotas, usage rights / copyright, hide and lock dates | ✅ | ✅ | ➕ | file_folder, storage_quota, file_storage columns |
| Video with captions and transcripts, watch progress | ✅ | ✅ | ✅ | video_lecture, video_watch_progress |
| SCORM / Common Cartridge / QTI import, course copy | ✅ | ✅ | ✅ | content_package, course_copy_job |
| Shared repository (Commons / Content Market) | ✅ | ✅ | ✅ | learning_object_repository, learning_object |
| Collaborations (Google Docs / Microsoft 365) | ✅ | ✅ | ➕ | collaboration, collaboration_member |
| Accessibility scores and alternative formats (Ally-style) | ✅ | ✅ | ➕ | accessibility_scan, alternative_format |
| E-books with annotations and reading progress | — | — | ✅ | ebook_* (beyond both) |
| Rich editor, equations, immersive reader, link validator | ✅ | ✅ | 🖥️ | — |

## 3. Assignments and grading
| Feature | C | B | Status | Where |
|---|---|---|---|---|
| Submission types: upload, text, URL, media, student annotation, external tool, on paper | ✅ | ✅ | ➕ | assignment.assignment_type, `allowed_submission_types`, `allowed_file_extensions`, `annotatable_file_id` |
| Assign to students / sections / groups / tags, per-student dates and attempts | ✅ | ✅ | ✅ / ➕ | assignment_override (TAG + checkpoints ➕) |
| Accommodations (extra time, extra attempts) | ✅ | ✅ | ✅ | accommodation |
| Group assignments, grade individually | ✅ | ✅ | ✅ / ➕ | assignment.group_set_id, `grade_group_individually` ➕ |
| Peer review, self-assessment | ✅ | ✅ | ✅ / ➕ | assignment_peer_review, rubric_assessment.assessment_purpose ➕ |
| Anonymous grading | ✅ | ✅ | ✅ | assignment.is_anonymous_grading |
| Moderated grading / parallel grading with a final grader or reconciler | ✅ | ✅ | ➕ | moderation_grader, provisional_grade, assignment `final_grader_id`, `grader_count` |
| Delegated grading (grader per group or section) | — | ✅ | ➕ | grader_assignment |
| Inline annotation feedback (SpeedGrader / DocViewer) | ✅ | ✅ | ➕ | submission_annotation |
| Feedback comment library | ✅ | ✅ | ➕ | comment_library_item |
| Audio/video feedback, submission comments | ✅ | ✅ | ✅ | comment + attachment |
| Plagiarism and AI-writing detection | ✅ | ✅ | ✅ | plagiarism_scan |
| Late / missing policy, excused, late overrides | ✅ | ✅ | ➕ | late_policy, assignment_submission columns |
| Rubrics: outcome-aligned criteria, point ranges, account-level, hide points, free-form | ✅ | ✅ | ✅ / ➕ | rubric_template, rubric_criteria, rubric_level |
| Weighted categories, drop lowest / highest / never drop | ✅ | ✅ | ✅ / ➕ | grade_category |
| Grading schemes / grade schemas | ✅ | ✅ | ✅ | grade_scale, grade_scale_item |
| Grading periods (weighted) | ✅ | ✅ | ➕ | grading_period_group, grading_period, gradebook_period_grade |
| Final grade override | ✅ | ✅ | ➕ | gradebook override columns |
| Grade posting policies, scheduled release | ✅ | ✅ | ➕ | grade_item `post_policy`, `date_scheduled_post`; course `default_post_policy` |
| Calculated columns | — | ✅ | ➕ | grade_item CALCULATED + `calculation_formula` |
| Custom gradebook (notes) columns | ✅ | ✅ | ➕ | gradebook_custom_column, gradebook_custom_column_value |
| Grade change audit log | ✅ | ✅ | ✅ | grade_history (auto trigger) |
| Grade submission to the registrar / SIS | ✅ | ✅ | ➕ | final_grade_submission |
| What-if grades, gradebook filters | ✅ | ✅ | 🖥️ | — |

## 4. Quizzes and tests
| Feature | C | B | Status | Where |
|---|---|---|---|---|
| Question types incl. categorization, hot spot, ordering, formula, stimulus, file upload, multiple dropdowns, jumbled sentence, either/or, Likert | ✅ | ✅ | ➕ | question.question_type, `parent_question_id`, `interaction_data` |
| Partial credit modes | ✅ | ✅ | ➕ | question.partial_credit_mode |
| Item banks / question pools, random draw | ✅ | ✅ | ✅ | question_bank, assessment_question_group |
| Item bank sharing (view / edit) | ✅ | ✅ | ➕ | question_bank_share |
| Timed, multiple attempts, shuffle, one question at a time | ✅ | ✅ | ✅ | assessment |
| Access codes, IP limits, lockdown browser, proctoring | ✅ | ✅ | ✅ / ➕ | test_security (`access_code_hash`, `require_lockdown_browser` ➕), proctoring_session |
| Quiz log / attempt auditing | ✅ | ✅ | ➕ | assessment_attempt_event |
| Regrade | ✅ | ✅ | ➕ | question_regrade |
| Item analysis, surveys | ✅ | ✅ | ✅ | item_analysis, assessment_type |

## 5. Communication and collaboration
| Feature | C | B | Status | Where |
|---|---|---|---|---|
| Discussions: threaded, graded, anonymous, Q&A (post first), ratings | ✅ | ✅ | ✅ / ➕ | discussion (`allow_rating`, `sort_by_rating` ➕) |
| Discussion checkpoints (separate due dates for post and replies) | ✅ | ✅ | ➕ | discussion_checkpoint |
| Group discussions | ✅ | ✅ | ➕ | discussion.group_set_id |
| Journals, wikis | — | ✅ | ➕ | discussion_type JOURNAL / WIKI |
| Unread / read tracking | ✅ | ✅ | ➕ | discussion_post_read |
| Inbox / messages, announcements (delayed, section-specific, read confirmation) | ✅ | ✅ | ✅ | message_*, announcement, announcement_view |
| Notification preferences and channels (email, SMS, push device tokens) | ✅ | ✅ | ✅ / ➕ | notification_preference, communication_channel ➕ |
| Calendar, iCal feed | ✅ | ✅ | ✅ / ➕ | calendar_event, users.calendar_feed_token_hash ➕ |
| Office hours / appointment scheduler | ✅ | ✅ | ✅ | office_hour_slot, appointment_booking |
| Web conferencing with attendance log | ✅ | ✅ | ✅ / ➕ | conference_session, conference_participant ➕ |
| Attendance | ✅ | ✅ | ✅ | attendance_session, student_attendance |
| Student planner / to-do list | ✅ | ✅ | ➕ | planner_note, planner_override |
| AI translation of discussions / inbox | ✅ | — | 🖥️ + ai_interaction log | — |

## 6. Outcomes, analytics, student success
| Feature | C | B | Status | Where |
|---|---|---|---|---|
| Outcomes / goals at institution and course level, alignments | ✅ | ✅ | ✅ | learning_outcome, educational_standard, *_alignment |
| Mastery gradebook, proficiency scales, calculation methods | ✅ | ✅ | ✅ / ➕ | outcome_result, outcome_proficiency_rating ➕, learning_outcome `calculation_method` ➕ |
| Course and student analytics, page views | ✅ | ✅ | ✅ | engagement_metric, learning_event, course_health_check |
| Early alerts / Retention Center rules | — | ✅ | ➕ | early_alert_rule, early_alert |
| Advising, intervention plans, tutoring | — | — | ✅ | advising_* (beyond both) |
| Badges / credentials (Open Badges 3.0 export) | ✅ | ✅ | ✅ / ➕ | badge (`open_badge_class` ➕), student_badge (`assertion_url` ➕) |
| ePortfolio | ✅ | ✅ | ➕ | portfolio, portfolio_section, portfolio_artifact, portfolio_share |
| Public catalog with paid self-enrollment | ✅ | — | ➕ | catalog_listing, catalog_order |
| Reports, scheduled reports, dashboards | ✅ | ✅ | ✅ | report_template, custom_report, report_schedule, dashboard_widget |

## 7. Integrations, compliance, platform
| Feature | C | B | Status | Where |
|---|---|---|---|---|
| LTI 1.3 / Advantage (grade passback, roster, deep linking) | ✅ | ✅ | ✅ | lti_registration, lti_resource_link |
| SIS sync (OneRoster, CSV), grade passback | ✅ | ✅ | ✅ | sis_integration, sis_sync_run |
| REST API keys, webhooks, xAPI | ✅ | ✅ | ✅ | api_key, webhook_integration, xapi_statement |
| FERPA/GDPR: consent, data export/erasure, retention, legal hold | ✅ | ✅ | ✅ | consent_record, data_request, retention_policy, legal_hold |
| Audit logs, feature flags | ✅ | ✅ | ✅ | audit_log, feature_flag |
| AI features usage and review log | ✅ | ✅ | ➕ | ai_interaction |

## Already beyond both platforms
Waitlists, e-books, advising and tutoring, intervention plans, knowledge base, accreditation metrics, a learning-object repository with metadata, and tenant isolation enforced in the database by row-level security.

## Next phase: ideas for going beyond
- Semantic search: add vector embeddings to `search_index` (pgvector).
- Skills and competency transcript across courses (comprehensive learner record).
- Calendar conflict detection and workload balancing for students' due dates.
- Built-in A/B testing of course designs (removed in v1; could return scoped to courses).
