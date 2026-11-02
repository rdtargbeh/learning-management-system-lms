"""End-to-end test for assignments, overrides, submissions, grading, posting, peer reviews and module requirements.

Needs a running backend whose database has the bootstrap platform owner (owner@lms-company.com / Owner-Pass-2026!).
Creates its own institutions (codes ASG and ASX), so it can run after the other API tests on the same database.
    python scripts/api_test_assignments.py
"""
import json, os, sys, urllib.request
from datetime import date, datetime, timedelta, timezone

BASE = os.environ.get('LMS_API', 'http://localhost:8080/api/v1')
results = []


def call(method, path, body=None, token=None, expect=None, label=None):
    req = urllib.request.Request(BASE + path, method=method, data=None if body is None else json.dumps(body).encode(),
                                 headers={'Content-Type': 'application/json', **({'Authorization': 'Bearer ' + token} if token else {})})
    try:
        with urllib.request.urlopen(req) as r:
            status, data = r.status, r.read()
    except urllib.error.HTTPError as e:
        status, data = e.code, e.read()
    try:
        payload = json.loads(data) if data else None
    except Exception:
        payload = data.decode()[:200]
    ok = expect is None or status == expect
    results.append((ok, label or f'{method} {path}', status, expect))
    if not ok:
        print('   !!', label, status, json.dumps(payload)[:300])
    return payload


def check(cond, label):
    results.append((bool(cond), label, '-', '-'))
    if not cond:
        print('   !!', label)


def login(code, user, pw='Welcome-2026-x'):
    return call('POST', '/auth/login', {'tenantCode': code, 'login': user, 'password': pw}, expect=200, label=f'login {user}')['accessToken']


today = datetime.now(timezone.utc).date()
D = lambda n: (today + timedelta(days=n)).isoformat()
T_ = lambda days: (datetime.now(timezone.utc) + timedelta(days=days)).isoformat()

# ------------------------------------------------------------------ setup
T = call('POST', '/auth/login', {'login': 'owner@lms-company.com', 'password': 'Owner-Pass-2026!'}, expect=200, label='platform owner login')['accessToken']
call('POST', '/platform/tenants', {'tenantName': 'Assignment Uni', 'tenantCode': 'ASG',
     'admin': {'username': 'admin.asg', 'email': 'admin@asg.edu', 'password': 'Admin-Pass-2026'}}, token=T, expect=201, label='create tenant ASG')
call('POST', '/platform/tenants', {'tenantName': 'Other Place', 'tenantCode': 'ASX',
     'admin': {'username': 'admin.asx', 'email': 'admin@asx.edu', 'password': 'Admin-Pass-2026'}}, token=T, expect=201, label='create tenant ASX')
A, X = login('ASG', 'admin.asg', 'Admin-Pass-2026'), login('ASX', 'admin.asx', 'Admin-Pass-2026')


def mk(username, utype):
    return call('POST', '/users', {'username': username, 'email': f'{username}@asg.edu', 'userType': utype, 'password': 'Welcome-2026-x',
                'profile': {'firstName': username.split('.')[1].title(), 'lastName': 'Test'}}, token=A, expect=201, label=f'create {username}')['user']['userId']


ada, eve, rex = mk('prof.ada', 'INSTRUCTOR'), mk('ta.eve', 'TEACHING_ASSISTANT'), mk('rev.rex', 'STAFF')
s1, s2, s3, s4 = (mk(f'stu.s{i}', 'STUDENT') for i in range(1, 5))
c = call('POST', '/courses', {'courseCode': 'ENG101', 'courseTitle': 'Academic Writing', 'timeZone': 'America/Chicago', 'instructorUserId': ada},
         token=A, expect=201, label='create course (Chicago time zone)')
C = c['courseId']
sec1 = call('GET', f'/courses/{C}/sections', token=A)[0]['sectionId']
sec2 = call('POST', f'/courses/{C}/sections', {'sectionNumber': '02'}, token=A, expect=201, label='create section 02')['sectionId']
call('POST', f'/sections/{sec1}/staff', {'userId': eve, 'role': 'TEACHING_ASSISTANT'}, token=A, expect=201, label='add TA')
call('POST', f'/sections/{sec1}/staff', {'userId': rex, 'role': 'REVIEWER'}, token=A, expect=201, label='add reviewer')
for s in (s1, s2, s3):
    call('POST', f'/sections/{sec1}/enrollments', {'userId': s}, token=A, expect=201, label='enroll in 01')
call('POST', f'/sections/{sec2}/enrollments', {'userId': s4}, token=A, expect=201, label='enroll s4 in 02')
call('POST', f'/courses/{C}/publish', token=A, expect=200, label='publish course')
ADA, EVE, REX = login('ASG', 'prof.ada'), login('ASG', 'ta.eve'), login('ASG', 'rev.rex')
S1, S2, S3, S4 = (login('ASG', f'stu.s{i}') for i in range(1, 5))

# ------------------------------------------------------------------ authoring
a1 = call('POST', f'/courses/{C}/assignments', {'assignmentTitle': 'Essay 1: Argument', 'assignmentType': 'TEXT_ENTRY', 'pointsPossible': 10,
          'dateDue': D(3), 'maxSubmissions': 2, 'lateSubmissionAllowed': True}, token=ADA, expect=201, label='teacher creates essay assignment')
check(a1['assignmentStatus'] == 'DRAFT' and a1['isPublished'] is False and a1['submissionType'] == 'ONLINE' and a1['gradingType'] == 'POINTS', 'assignment defaults')
A1 = a1['assignmentId']
call('POST', f'/courses/{C}/assignments', {'assignmentTitle': 'Bad dates', 'assignmentType': 'ESSAY', 'dateDue': D(5), 'dateClose': D(2)},
     token=ADA, expect=400, label='close before due rejected')
call('POST', f'/courses/{C}/assignments', {'assignmentTitle': 'Nope', 'assignmentType': 'ESSAY'}, token=EVE, expect=403, label='TA cannot create assignments')
call('GET', f'/assignments/{A1}', token=S1, expect=404, label='student cannot see unpublished assignment')
call('POST', f'/assignments/{A1}/publish', token=ADA, expect=200, label='publish essay')
v = call('GET', f'/assignments/{A1}', token=S1, expect=200, label='student opens essay')
check(v['myStatus']['canSubmit'] and v['myStatus']['attemptsAllowed'] == 2 and v['myStatus']['attemptsUsed'] == 0, 'student can submit, 2 attempts')
check(v['myStatus']['dateDue'].startswith(D(3) + 'T23:59:59') and v['myStatus']['dateDue'][-6:] in ('-05:00', '-06:00'), 'due = end of day in Chicago')

# ------------------------------------------------------------------ submitting
d = call('POST', f'/assignments/{A1}/submissions', {'submissionText': '<p>Rough start</p>', 'draft': True}, token=S1, expect=201, label='student saves draft')
check(d['submissionStatus'] == 'DRAFT' and d.get('submittedAt') is None, 'draft saved')
g = call('GET', f'/assignments/{A1}/submissions', token=EVE, expect=200, label='TA grading list')
row = {x['username']: x for x in g}
check(len(g) == 4 and row['stu.s1']['attempts'] == 0 and row['stu.s1'].get('latest') is None, 'drafts are invisible to graders')
call('POST', f'/assignments/{A1}/submissions', {}, token=S1, expect=400, label='text required')
sub1 = call('POST', f'/assignments/{A1}/submissions', {'submissionText': '<p>My argument is that <b>writing</b> matters.</p>'}, token=S1, expect=201, label='student submits')
check(sub1['submissionStatus'] == 'SUBMITTED' and sub1['submissionNumber'] == 1 and sub1['wordCount'] == 6 and sub1['submittedLate'] is False, 'draft became attempt 1, 6 words')
sub1b = call('POST', f'/assignments/{A1}/submissions', {'submissionText': '<p>Revised argument.</p>'}, token=S1, expect=201, label='second attempt')
call('POST', f'/assignments/{A1}/submissions', {'submissionText': 'third'}, token=S1, expect=409, label='no attempts left')
call('POST', f'/assignments/{A1}/submissions', {'submissionText': 'x'}, token=ADA, expect=403, label='teacher cannot submit')
check(len(call('GET', f'/assignments/{A1}/submissions/mine', token=S1, expect=200, label='my attempts')) == 2, 'two attempts')

# reassign gives another try
call('POST', f"/submissions/{sub1b['submissionId']}/reassign", token=ADA, expect=200, label='teacher reassigns attempt 2')
check(call('GET', f'/assignments/{A1}', token=S1)['myStatus']['canSubmit'], 'reassigned attempt does not count')
call('POST', f'/assignments/{A1}/submissions', {'submissionText': '<p>Third try after feedback.</p>'}, token=S1, expect=201, label='resubmit after reassign')

# ------------------------------------------------------------------ dates and overrides
a2 = call('POST', f'/courses/{C}/assignments', {'assignmentTitle': 'Source list (link)', 'assignmentType': 'URL_SUBMISSION', 'pointsPossible': 5,
          'dateAvailable': D(-10), 'dateDue': D(-3), 'lateSubmissionAllowed': False}, token=ADA, expect=201, label='create past-due link assignment')
A2 = a2['assignmentId']
call('POST', f'/assignments/{A2}/publish', token=ADA, expect=200, label='publish link assignment')
st = call('GET', f'/assignments/{A2}', token=S1, expect=200, label='student sees past-due assignment')['myStatus']
check(not st['canSubmit'] and st['lockReason'] == 'The due date has passed' and st['latePolicyStatus'] == 'MISSING', 'closed and missing')
call('POST', f'/assignments/{A2}/submissions', {'submissionUrl': 'https://example.org/sources'}, token=S1, expect=409, label='late submission refused')
call('POST', f'/assignments/{A2}/overrides', {'targetType': 'STUDENT', 'targetUserId': s1, 'dateDue': T_(2), 'reason': 'Medical'}, token=ADA, expect=201, label='extension for student 1')
call('POST', f'/assignments/{A2}/overrides', {'targetType': 'SECTION', 'targetSectionId': sec2, 'dateDue': T_(1)}, token=ADA, expect=201, label='extension for section 02')
call('POST', f'/assignments/{A2}/overrides', {'targetType': 'STUDENT', 'targetUserId': ada, 'dateDue': T_(1)}, token=ADA, expect=400, label='override target must be a student')
call('POST', f'/assignments/{A2}/overrides', {'targetType': 'SECTION', 'targetUserId': s1}, token=ADA, expect=400, label='override needs the matching target')
call('POST', f'/assignments/{A2}/submissions', {'submissionUrl': 'not a link'}, token=S1, expect=400, label='invalid link rejected')
u1 = call('POST', f'/assignments/{A2}/submissions', {'submissionUrl': 'https://example.org/sources'}, token=S1, expect=201, label='student 1 submits within extension')
check(u1['submittedLate'] is False, 'not late against the extended due date')
call('POST', f'/assignments/{A2}/submissions', {'submissionUrl': 'https://example.org/s4'}, token=S4, expect=201, label='section 02 student submits')
call('POST', f'/assignments/{A2}/submissions', {'submissionUrl': 'https://example.org/s2'}, token=S2, expect=409, label='student 2 still closed')
gl = {x['username']: x for x in call('GET', f'/assignments/{A2}/submissions', token=ADA, expect=200, label='grading list for link assignment')}
check(gl['stu.s2']['missing'] and gl['stu.s2']['latePolicyStatus'] == 'MISSING' and not gl['stu.s1']['missing'], 'student 2 missing, student 1 not')
check(len(call('GET', f'/assignments/{A2}/overrides', token=ADA, expect=200, label='list overrides')) == 2, 'two overrides')

a3 = call('POST', f'/courses/{C}/assignments', {'assignmentTitle': 'Reflection (late ok)', 'assignmentType': 'ESSAY', 'pointsPossible': 20,
          'dateDue': D(-3), 'lateSubmissionAllowed': True}, token=ADA, expect=201, label='create past-due assignment that accepts late work')
A3 = a3['assignmentId']
call('POST', f'/assignments/{A3}/publish', token=ADA, expect=200, label='publish reflection')
l2 = call('POST', f'/assignments/{A3}/submissions', {'submissionText': 'Late reflection'}, token=S2, expect=201, label='late submission accepted')
check(l2['submittedLate'] is True and l2['latePolicyStatus'] == 'LATE', 'marked late')

# ------------------------------------------------------------------ grading and posting
gr = call('POST', f"/submissions/{sub1['submissionId']}/grade", {'gradeEarned': 8, 'feedbackText': 'Good thesis; tighten paragraph 2.'}, token=EVE, expect=200, label='TA grades')
check(gr['feedback']['gradeEarned'] == 8 and gr['feedback']['gradePercentage'] == 80 and gr['submissionStatus'] == 'RETURNED', 'automatic post policy: visible at once')
call('POST', f"/submissions/{sub1['submissionId']}/grade", {'gradeEarned': 5}, token=REX, expect=403, label='reviewer cannot grade')
call('POST', f"/submissions/{sub1['submissionId']}/grade", {'gradeEarned': 10}, token=S2, expect=403, label='student cannot grade')
check(d['submissionId'] == sub1['submissionId'], 'the draft row became attempt 1')
mine = call('GET', f'/assignments/{A1}/submissions/mine', token=S1, expect=200, label='student reads feedback')
check(mine[0]['feedback']['feedbackText'].startswith('Good thesis'), 'feedback visible to the student')
call('GET', f"/submissions/{sub1['submissionId']}", token=S2, expect=403, label="classmate cannot read someone's submission")

call('PATCH', f'/courses/{C}', {'defaultPostPolicy': 'MANUAL'}, token=ADA, expect=200, label='teacher switches to manual posting')
g3 = call('POST', f"/submissions/{l2['submissionId']}/grade", {'gradeEarned': 15, 'feedbackText': 'Thoughtful.'}, token=ADA, expect=200, label='grade under manual policy')
check(g3['submissionStatus'] == 'GRADED', 'grade hidden until posted')
check(call('GET', f'/assignments/{A3}/submissions/mine', token=S2)[0].get('feedback') is None, 'student does not see unposted grade')
check(call('GET', f'/assignments/{A3}', token=S2)['myStatus'].get('score') is None, 'no score shown before posting')
pg = call('POST', f'/assignments/{A3}/post-grades', token=ADA, expect=200, label='post grades')
check(pg['posted'] == 1, 'one grade posted')
check(call('GET', f'/assignments/{A3}/submissions/mine', token=S2)[0]['feedback']['gradeEarned'] == 15, 'student sees posted grade')
check(call('GET', f'/assignments/{A3}', token=S2)['myStatus']['score'] == 15, 'score on the status card')

# excuse
ex = call('PUT', f'/assignments/{A3}/students/{s3}/excuse', token=EVE, expect=200, label='excuse student 3')
check(ex['isExcused'] is True, 'excused')
check(call('GET', f'/assignments/{A3}', token=S3)['myStatus']['excused'] is True, 'student sees excused')
gl3 = {x['username']: x for x in call('GET', f'/assignments/{A3}/submissions', token=EVE)}
check(gl3['stu.s3']['missing'] is False and gl3['stu.s1']['missing'] is True, 'excused is not missing; others are')
call('DELETE', f'/assignments/{A3}/students/{s3}/excuse', token=EVE, expect=200, label='un-excuse')

# anonymous grading
an = call('POST', f'/courses/{C}/assignments', {'assignmentTitle': 'Blind essay', 'assignmentType': 'ESSAY', 'isAnonymousGrading': True}, token=ADA, expect=201, label='anonymous assignment')
call('POST', f"/assignments/{an['assignmentId']}/publish", token=ADA, expect=200, label='publish anonymous')
call('POST', f"/assignments/{an['assignmentId']}/submissions", {'submissionText': 'Hidden author'}, token=S3, expect=201, label='submit anonymous')
anl = call('GET', f"/assignments/{an['assignmentId']}/submissions", token=EVE, expect=200, label='anonymous grading list')
check(all(x.get('username') is None and x.get('displayName') is None for x in anl), 'graders see no names')

# ------------------------------------------------------------------ modules: SUBMIT and MIN_SCORE
call('PATCH', f'/courses/{C}', {'defaultPostPolicy': 'AUTOMATIC'}, token=ADA, expect=200, label='back to automatic posting')
M = call('POST', f'/courses/{C}/modules', {'moduleTitle': 'Unit 1 Writing Process', 'requireSequentialProgress': True}, token=ADA, expect=201, label='create sequential module')
w1 = call('POST', f'/courses/{C}/assignments', {'assignmentTitle': 'Outline', 'assignmentType': 'TEXT_ENTRY', 'pointsPossible': 10, 'moduleId': M['moduleId']}, token=ADA, expect=201, label='assignment in module (1st)')
pg1 = call('POST', f'/courses/{C}/content', {'typeCode': 'PAGE', 'contentTitle': 'Drafting Tips', 'contentBody': '<p>tips</p>', 'moduleId': M['moduleId']}, token=ADA, expect=201, label='page in module (2nd)')
w2 = call('POST', f'/courses/{C}/assignments', {'assignmentTitle': 'Final Draft', 'assignmentType': 'ESSAY', 'pointsPossible': 10, 'moduleId': M['moduleId']}, token=ADA, expect=201, label='assignment in module (3rd)')
check([w1['displayOrder'], pg1['displayOrder'], w2['displayOrder']] == [1, 2, 3], 'content and assignments share one order')
call('POST', f"/modules/{M['moduleId']}/requirements", {'assignmentId': w1['assignmentId'], 'requirementType': 'SUBMIT'}, token=ADA, expect=201, label='require submitting the outline')
call('POST', f"/modules/{M['moduleId']}/requirements", {'contentId': pg1['contentId'], 'requirementType': 'VIEW'}, token=ADA, expect=201, label='require viewing the tips')
call('POST', f"/modules/{M['moduleId']}/requirements", {'assignmentId': w2['assignmentId'], 'requirementType': 'MIN_SCORE'}, token=ADA, expect=400, label='MIN_SCORE needs minScore')
call('POST', f"/modules/{M['moduleId']}/requirements", {'assignmentId': w2['assignmentId'], 'requirementType': 'MIN_SCORE', 'minScore': 11}, token=ADA, expect=400, label='minScore above points rejected')
call('POST', f"/modules/{M['moduleId']}/requirements", {'assignmentId': w2['assignmentId'], 'requirementType': 'VIEW'}, token=ADA, expect=400, label='VIEW is not for assignments')
call('POST', f"/modules/{M['moduleId']}/requirements", {'contentId': pg1['contentId'], 'requirementType': 'SUBMIT'}, token=ADA, expect=400, label='SUBMIT is not for content')
call('POST', f"/modules/{M['moduleId']}/requirements", {'assignmentId': w2['assignmentId'], 'requirementType': 'MIN_SCORE', 'minScore': 7}, token=ADA, expect=201, label='require 7/10 on the final draft')
call('POST', f"/modules/{M['moduleId']}/publish?includeItems=true", token=ADA, expect=200, label='publish module with items')

mv = call('GET', f"/modules/{M['moduleId']}", token=S2, expect=200, label='student module view')
items = {i['title']: i for i in mv['items']}
check(items['Outline']['itemType'] == 'ASSIGNMENT' and items['Drafting Tips']['itemType'] == 'CONTENT', 'items carry their type')
check(items['Outline']['locked'] is False and items['Drafting Tips']['locked'] and items['Final Draft']['locked'], 'sequential: only the outline is open')
fd = call('GET', f"/assignments/{w2['assignmentId']}", token=S2, expect=200, label='student opens a locked assignment')['myStatus']
check(not fd['canSubmit'] and 'Outline' in (fd.get('lockReason') or ''), 'assignment locked until the outline is submitted')
call('POST', f"/assignments/{w2['assignmentId']}/submissions", {'submissionText': 'too early'}, token=S2, expect=409, label='cannot submit a locked assignment')
call('POST', f"/assignments/{w1['assignmentId']}/submissions", {'submissionText': 'I. Intro II. Body'}, token=S2, expect=201, label='submit outline')
items = {i['title']: i for i in call('GET', f"/modules/{M['moduleId']}", token=S2)['items']}
check(items['Outline']['completed'] and not items['Drafting Tips']['locked'] and items['Final Draft']['locked'], 'tips unlocked after submitting')
call('GET', f"/content/{pg1['contentId']}", token=S2, expect=200, label='view tips')
fsub = call('POST', f"/assignments/{w2['assignmentId']}/submissions", {'submissionText': 'Final essay text'}, token=S2, expect=201, label='submit final draft')
call('POST', f"/submissions/{fsub['submissionId']}/grade", {'gradeEarned': 6}, token=ADA, expect=200, label='grade 6/10')
mv = call('GET', f"/modules/{M['moduleId']}", token=S2)
check(mv['progressStatus'] == 'STARTED' and not {i['title']: i for i in mv['items']}['Final Draft']['completed'], 'score below 7: module not complete')
call('POST', f"/submissions/{fsub['submissionId']}/grade", {'gradeEarned': 8}, token=ADA, expect=200, label='regrade 8/10')
check(call('GET', f"/modules/{M['moduleId']}", token=S2)['progressStatus'] == 'COMPLETED', 'score reached: module completed')
prog = {p['username']: p for p in call('GET', f"/modules/{M['moduleId']}/progress", token=ADA, expect=200, label='teacher module progress')}
check(prog['stu.s2']['requirementsMet'] == 3 and prog['stu.s2']['requirementsTotal'] == 3 and prog['stu.s1']['requirementsMet'] == 0, 'progress counts assignment requirements')
call('POST', f"/assignments/{w2['assignmentId']}/move", {'moduleId': None}, token=ADA, expect=200, label='move final draft out of the module')
check(len(call('GET', f"/modules/{M['moduleId']}", token=ADA)['module']['requirements']) == 2, "its requirement went with it")

# ------------------------------------------------------------------ group assignment
gs = call('POST', f'/courses/{C}/group-sets', {'setName': 'Writing Pairs', 'maxMembersPerGroup': 2}, token=ADA, expect=201, label='group set')
grp = call('POST', f"/group-sets/{gs['groupSetId']}/groups", {'groupName': 'Pair A', 'sectionId': sec1}, token=ADA, expect=201, label='group')
for s in (s1, s2):
    call('POST', f"/groups/{grp['groupId']}/members", {'userId': s}, token=ADA, expect=201, label='add to pair')
ga = call('POST', f'/courses/{C}/assignments', {'assignmentTitle': 'Pair Project', 'assignmentType': 'TEXT_ENTRY', 'pointsPossible': 50, 'groupSetId': gs['groupSetId']},
          token=ADA, expect=201, label='group assignment')
check(ga['allowGroupSubmission'] is True and ga['groupSetId'] == gs['groupSetId'], 'group assignment flags')
call('POST', f"/assignments/{ga['assignmentId']}/publish", token=ADA, expect=200, label='publish group assignment')
call('POST', f"/assignments/{ga['assignmentId']}/submissions", {'submissionText': 'solo'}, token=S3, expect=400, label='student without a group cannot submit')
gsub = call('POST', f"/assignments/{ga['assignmentId']}/submissions", {'submissionText': 'Our joint project'}, token=S1, expect=201, label='one member submits for the pair')
s2sub = call('GET', f"/assignments/{ga['assignmentId']}/submissions/mine", token=S2, expect=200, label='partner sees the submission')
check(len(s2sub) == 1 and s2sub[0]['isGroupSubmission'] and s2sub[0]['submissionText'] == 'Our joint project', 'partner got the group submission')
call('POST', f"/submissions/{gsub['submissionId']}/grade", {'gradeEarned': 45, 'feedbackText': 'Great teamwork'}, token=ADA, expect=200, label='grade the group once')
check(call('GET', f"/assignments/{ga['assignmentId']}/submissions/mine", token=S2)[0]['feedback']['gradeEarned'] == 45, 'partner received the same grade')

# ------------------------------------------------------------------ peer reviews
pr = call('POST', f'/courses/{C}/assignments', {'assignmentTitle': 'Peer Reviewed Essay', 'assignmentType': 'ESSAY', 'peerReviewRequired': True}, token=ADA, expect=201, label='peer-review assignment')
PR = pr['assignmentId']
check(pr['peerReviewCount'] == 1, 'default one review each')
call('POST', f'/assignments/{PR}/publish', token=ADA, expect=200, label='publish peer-review assignment')
call('POST', f'/assignments/{PR}/submissions', {'submissionText': 'Essay by one'}, token=S1, expect=201, label='s1 submits')
call('POST', f'/assignments/{PR}/peer-reviews/assign', {}, token=ADA, expect=409, label='needs at least two submissions')
call('POST', f'/assignments/{PR}/submissions', {'submissionText': 'Essay by two'}, token=S2, expect=201, label='s2 submits')
call('POST', f'/assignments/{PR}/submissions', {'submissionText': 'Essay by three'}, token=S3, expect=201, label='s3 submits')
allr = call('POST', f'/assignments/{PR}/peer-reviews/assign', {'anonymous': True}, token=ADA, expect=200, label='assign anonymous peer reviews')
check(len(allr) == 3 and all(r['reviewerId'] != r['revieweeId'] for r in allr), 'three reviews, nobody reviews themselves')
check(len(call('POST', f'/assignments/{PR}/peer-reviews/assign', {}, token=ADA, expect=200, label='assign again')) == 3, 'running again adds nothing')
todo = call('GET', f'/assignments/{PR}/peer-reviews/mine', token=S1, expect=200, label='student 1 review to-do')
check(len(todo) == 1 and todo[0]['submissionText'].startswith('Essay by') and todo[0].get('revieweeName') is None, 'reviewer sees the work, not the name')
call('POST', f"/peer-reviews/{todo[0]['peerReviewId']}/submit", {'reviewText': 'Clear thesis.', 'reviewRating': 4}, token=S2, expect=403, label="cannot submit someone else's review")
call('POST', f"/peer-reviews/{todo[0]['peerReviewId']}/submit", {'reviewText': 'Clear thesis.', 'reviewRating': 6}, token=S1, expect=400, label='rating must be 1-5')
call('POST', f"/peer-reviews/{todo[0]['peerReviewId']}/submit", {'reviewText': 'Clear thesis.', 'reviewRating': 4}, token=S1, expect=200, label='submit peer review')
author = next(r for r in allr if r['peerReviewId'] == todo[0]['peerReviewId'])['revieweeId']
tok = {s1: S1, s2: S2, s3: S3}[author]
rec = call('GET', f'/assignments/{PR}/peer-reviews/received', token=tok, expect=200, label='author reads the review')
check(len(rec) == 1 and rec[0]['reviewRating'] == 4 and rec[0].get('reviewerId') is None and rec[0].get('reviewerName') is None, 'anonymous: author does not learn the reviewer')

# ------------------------------------------------------------------ comment library, lifecycle, isolation
c1 = call('POST', '/comment-library', {'commentText': 'Cite your sources (APA 7).'}, token=ADA, expect=201, label='save a comment')
call('POST', f"/comment-library/{c1['libraryItemId']}/use", token=ADA, expect=200, label='use a comment')
lib = call('GET', '/comment-library', token=ADA, expect=200, label='my comment library')
check(len(lib) == 1 and lib[0]['usageCount'] == 1, 'usage counted')
call('DELETE', f"/comment-library/{c1['libraryItemId']}", token=EVE, expect=404, label="cannot delete someone else's comment")
call('DELETE', f"/comment-library/{c1['libraryItemId']}", token=ADA, expect=204, label='delete own comment')

call('POST', f'/assignments/{A1}/unpublish', token=ADA, expect=409, label='cannot unpublish once students submitted')
call('DELETE', f'/assignments/{A1}', token=ADA, expect=409, label='cannot delete once students submitted')
empty = call('POST', f'/courses/{C}/assignments', {'assignmentTitle': 'Scratch', 'assignmentType': 'ON_PAPER'}, token=ADA, expect=201, label='scratch assignment')
check(empty['submissionType'] == 'ON_PAPER', 'on-paper assignment')
call('DELETE', f"/assignments/{empty['assignmentId']}", token=ADA, expect=204, label='delete unused assignment')
sl = call('GET', f'/courses/{C}/assignments', token=S4, expect=200, label='student assignment list')
check(all(x['assignment']['isPublished'] for x in sl) and all('myStatus' in x for x in sl), 'student list: published only, with status')
check(all(x.get('myStatus') is None for x in call('GET', f'/courses/{C}/assignments', token=ADA, expect=200, label='teacher assignment list')), 'staff list has no personal status')
call('GET', f'/assignments/{A1}', token=X, expect=404, label='other institution cannot see assignments')
call('GET', f'/assignments/{A1}/submissions', token=X, expect=404, label='other institution cannot see submissions')

passed = sum(1 for r in results if r[0])
print(f'{passed}/{len(results)} checks passed')
for ok, label, status, exp in results:
    print(('PASS' if ok else 'FAIL'), f'{status!s:>3}', label)
sys.exit(0 if passed == len(results) else 1)
