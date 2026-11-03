"""End-to-end test for the rest of learning content (domain 05): videos, content blocks, interactive launch details,
content permissions (RESTRICTED items, denials), the module prerequisite/requirement endpoints, the resource library,
release conditions, mastery paths, course pacing, and blueprint copies of content blocks.

Needs a running backend whose database has the bootstrap platform owner (owner@lms-company.com / Owner-Pass-2026!).
Creates its own institutions (LCX and LCY).
    python scripts/api_test_learning.py
"""
import json, os, sys, urllib.request
from datetime import date, timedelta

BASE = os.environ.get('LMS_API', 'http://localhost:8080/api/v1')
results = []
today = date.today()
D = lambda n: (today + timedelta(days=n)).isoformat()
T_ = lambda n: (today + timedelta(days=n)).isoformat() + 'T00:00:00Z'


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


# ------------------------------------------------------------------ setup
OWN = call('POST', '/auth/login', {'login': 'owner@lms-company.com', 'password': 'Owner-Pass-2026!'}, expect=200, label='platform owner login')['accessToken']
call('POST', '/platform/tenants', {'tenantName': 'Lesson University', 'tenantCode': 'LCX',
     'admin': {'username': 'admin.lcx', 'email': 'admin@lcx.edu', 'password': 'Admin-Pass-2026'}}, token=OWN, expect=201, label='create tenant LCX')
call('POST', '/platform/tenants', {'tenantName': 'Lesson Elsewhere', 'tenantCode': 'LCY',
     'admin': {'username': 'admin.lcy', 'email': 'admin@lcy.edu', 'password': 'Admin-Pass-2026'}}, token=OWN, expect=201, label='create tenant LCY')
A = login('LCX', 'admin.lcx', 'Admin-Pass-2026')
X = login('LCY', 'admin.lcy', 'Admin-Pass-2026')


def mk(username, utype):
    return call('POST', '/users', {'username': username, 'email': f'{username}@lcx.edu', 'userType': utype, 'password': 'Welcome-2026-x',
                'profile': {'firstName': username.split('.')[1].title(), 'lastName': 'Test'}}, token=A, expect=201, label=f'create {username}')['user']['userId']


ada, eve = mk('prof.ada', 'INSTRUCTOR'), mk('ta.eve', 'TEACHING_ASSISTANT')
s1, s2, s3, s4 = (mk(f'stu.s{i}', 'STUDENT') for i in range(1, 5))
ADA, EVE = login('LCX', 'prof.ada'), login('LCX', 'ta.eve')
S1, S2, S3, S4 = (login('LCX', f'stu.s{i}') for i in range(1, 5))
C = call('POST', '/courses', {'courseCode': 'LRN100', 'courseTitle': 'Learning Design', 'instructorUserId': ada}, token=A, expect=201, label='course')['courseId']
sec1 = call('GET', f'/courses/{C}/sections', token=A)[0]['sectionId']
sec2 = call('POST', f'/courses/{C}/sections', {'sectionNumber': '02'}, token=A, expect=201, label='section 02')['sectionId']
call('POST', f'/sections/{sec2}/staff', {'userId': ada, 'role': 'PRIMARY'}, token=A, expect=201, label='ada in 02')
call('POST', f'/sections/{sec1}/staff', {'userId': eve, 'role': 'TEACHING_ASSISTANT'}, token=A, expect=201, label='TA')
call('POST', f'/courses/{C}/publish', token=ADA, expect=200, label='publish')
for sid, sec in ((s1, sec1), (s2, sec1), (s3, sec1), (s4, sec2)):
    call('POST', f'/sections/{sec}/enrollments', {'userId': sid}, token=ADA, expect=201, label='enroll')


def pub(c, course=None):
    call('POST', f"/content/{c['contentId']}/publish", token=ADA, expect=200, label='publish item')
    return c


def page(title, **extra):
    return pub(call('POST', f'/courses/{C}/content', {'typeCode': 'PAGE', 'contentTitle': title, 'contentBody': f'<p>{title}</p>', **extra},
                    token=ADA, expect=201, label=f'page {title}'))


def assignment(title, points, **extra):
    a = call('POST', f'/courses/{C}/assignments', {'assignmentTitle': title, 'assignmentType': 'TEXT_ENTRY', 'pointsPossible': points, **extra},
             token=ADA, expect=201, label=f'assignment {title}')
    call('POST', f"/assignments/{a['assignmentId']}/publish", token=ADA, expect=200, label='publish assignment')
    return a


def lock_of(cid, tok):
    return call('GET', f'/content/{cid}', token=tok).get('lockReason')


def grade(a, student_tok, pts):
    sub = call('POST', f"/assignments/{a['assignmentId']}/submissions", {'submissionText': '<p>answer</p>'}, token=student_tok, expect=201, label='submit')
    call('POST', f"/submissions/{sub['submissionId']}/grade", {'gradeEarned': pts}, token=ADA, expect=200, label=f'grade {pts}')
    return sub


# ------------------------------------------------------------------ video_lecture
vid = call('POST', f'/courses/{C}/content', {'typeCode': 'VIDEO', 'contentTitle': 'Welcome Video', 'video': {'videoUrl': 'https://video.example/1'}},
           token=ADA, expect=201, label='video item')
pub(vid)
V = vid['contentId']
check(call('GET', f'/content/{V}/video', token=S1, expect=200, label='student reads the video')['videoUrl'] == 'https://video.example/1', 'video')
call('PUT', f'/content/{V}/video', {'captionsUrl': 'https://video.example/1.vtt', 'captionsAvailable': True}, token=S1, expect=403, label='students cannot edit')
vv = call('PUT', f'/content/{V}/video', {'captionsUrl': 'https://video.example/1.vtt', 'captionsAvailable': True}, token=ADA, expect=200, label='add captions')
check(vv['captionsAvailable'] and vv['videoUrl'] == 'https://video.example/1', 'captions added, URL kept')
call('DELETE', f'/content/{V}/video', token=ADA, expect=204, label='remove the video')
call('GET', f'/content/{V}/video', token=S1, expect=404, label='no video now')
call('PUT', f'/content/{V}/video', {'captionsAvailable': True}, token=ADA, expect=400, label='a new video needs a URL')

# ------------------------------------------------------------------ content_block
P1 = page('Reading One')['contentId']
call('POST', f'/content/{P1}/blocks', {'blockTitle': 'No type'}, token=ADA, expect=400, label='block type required')
b1 = call('POST', f'/content/{P1}/blocks', {'blockType': 'TEXT', 'blockTitle': 'Intro', 'blockContent': '<p>hello</p>'}, token=ADA, expect=201, label='text block')
b2 = call('POST', f'/content/{P1}/blocks', {'blockType': 'CODE', 'blockContent': 'print(1)', 'metadata': {'language': 'python'}}, token=ADA, expect=201, label='code block')
check(b1['blockNumber'] == 1 and b2['blockNumber'] == 2 and b2['metadata']['language'] == 'python', 'numbered blocks')
call('POST', f'/content/{P1}/blocks', {'blockType': 'TEXT'}, token=S1, expect=403, label='students cannot add blocks')
call('PUT', f'/content/{P1}/blocks/order', {'orderedIds': [b2['blockId']]}, token=ADA, expect=400, label='order must list every block')
ro = call('PUT', f'/content/{P1}/blocks/order', {'orderedIds': [b2['blockId'], b1['blockId']]}, token=ADA, expect=200, label='reorder blocks')
check([b['blockId'] for b in ro] == [b2['blockId'], b1['blockId']], 'reordered')
call('PATCH', f"/content-blocks/{b1['blockId']}", {'blockTitle': 'Introduction'}, token=ADA, expect=200, label='edit a block')
bl = call('GET', f'/content/{P1}/blocks', token=S1, expect=200, label='student reads blocks')
check([b.get('blockTitle') for b in bl] == [None, 'Introduction'], 'student sees the ordered blocks')
call('DELETE', f"/content-blocks/{b2['blockId']}", token=ADA, expect=204, label='delete a block')
call('GET', f'/content/{P1}/blocks', token=X, expect=404, label='other institution')

# ------------------------------------------------------------------ interactive_content
IC = call('POST', f'/courses/{C}/content', {'typeCode': 'INTERACTIVE', 'contentTitle': 'Circuit Simulation'}, token=ADA, expect=201, label='interactive item')
IC = pub(IC)['contentId']
call('PUT', f'/content/{IC}/interactive', {'interactionType': 'SIMULATION', 'launchMethod': 'LTI'}, token=ADA, expect=400, label='LTI needs a tool id')
call('PUT', f'/content/{IC}/interactive', {'launchMethod': 'EMBED', 'interactionUrl': 'http://sim.example'}, token=ADA, expect=400, label='embeds need https')
call('PUT', f'/content/{IC}/interactive', {'launchMethod': 'SCORM'}, token=ADA, expect=400, label='SCORM needs a package')
call('PUT', f'/content/{P1}/interactive', {'launchMethod': 'EMBED', 'interactionUrl': 'https://sim.example'}, token=ADA, expect=400, label='pages have no launch details')
ic = call('PUT', f'/content/{IC}/interactive', {'interactionType': 'SIMULATION', 'launchMethod': 'EMBED', 'interactionUrl': 'https://sim.example/circuit',
          'maxAttempts': 3, 'timeLimitMinutes': 20}, token=ADA, expect=200, label='embed launch details')
check(ic['allowsMultipleAttempts'] and ic['maxAttempts'] == 3, 'saved with defaults')
check(call('GET', f'/content/{IC}/interactive', token=S1, expect=200, label='student reads them')['interactionUrl'].endswith('/circuit'), 'launch URL')

# ------------------------------------------------------------------ content_permission
R1 = page('Restricted Notes', accessLevel='RESTRICTED')['contentId']
call('GET', f'/content/{R1}', token=S1, expect=404, label='RESTRICTED hidden without a grant')
call('POST', f'/content/{R1}/permissions', {'userId': s1, 'roleId': s1, 'permissionType': 'VIEW'}, token=ADA, expect=400, label='user or role, not both')
call('POST', f'/content/{R1}/permissions', {'userId': s1, 'permissionType': 'VIEW'}, token=S1, expect=403, label='students cannot grant')
g1 = call('POST', f'/content/{R1}/permissions', {'userId': s1, 'permissionType': 'VIEW'}, token=ADA, expect=201, label='grant VIEW to s1')
call('GET', f'/content/{R1}', token=S1, expect=200, label='s1 now sees it')
call('GET', f'/content/{R1}', token=S2, expect=404, label='s2 still does not')
check(call('GET', f'/content/{R1}/permissions/mine', token=S1, expect=200, label='s1 permissions') == ['VIEW'], 'only VIEW on a restricted item')
role = call('POST', '/roles', {'roleName': 'Peer Tutors', 'permissions': []}, token=A, expect=201, label='role')
call('POST', f'/users/{s2}/roles', {'roleId': role['roleId']}, token=A, expect=201, label='s2 is a peer tutor')
call('POST', f'/content/{R1}/permissions', {'roleId': role['roleId'], 'permissionType': 'VIEW'}, token=ADA, expect=201, label='grant VIEW to the role')
call('GET', f'/content/{R1}', token=S2, expect=200, label='role members see it')
call('POST', f'/content/{R1}/permissions', {'userId': s2, 'permissionType': 'VIEW', 'isGranted': False}, token=ADA, expect=201, label='deny s2 personally')
call('GET', f'/content/{R1}', token=S2, expect=404, label="a user's own denial wins over the role")
check(set(call('GET', f'/content/{P1}/permissions/mine', token=S3, expect=200, label='defaults on a normal page')) == {'VIEW', 'DOWNLOAD', 'PRINT', 'COMMENT'}, 'student defaults')
call('POST', f'/content/{P1}/permissions', {'userId': s3, 'permissionType': 'VIEW', 'isGranted': False}, token=ADA, expect=201, label='hide a page from s3')
call('GET', f'/content/{P1}', token=S3, expect=404, label='denied student cannot see the page')
check(all(c['contentId'] != P1 for c in call('GET', f'/courses/{C}/content', token=S3)), 'and it is left out of the list')
pl = call('GET', f'/content/{R1}/permissions', token=ADA, expect=200, label='teacher lists permissions')
check(len(pl) == 3, 'three permission rows')
call('DELETE', f"/content/{R1}/permissions/{g1['permissionId']}", token=ADA, expect=204, label='revoke s1')
call('GET', f'/content/{R1}', token=S1, expect=404, label='hidden again')
call('POST', f'/content/{R1}/permissions', {'userId': s1, 'permissionType': 'VIEW', 'dateExpires': '2020-01-01T00:00:00Z'}, token=ADA, expect=400, label='expiry in the future')
check(len(call('GET', f'/content/{R1}/permissions/mine', token=ADA)) == 8, 'staff hold every permission')

# ------------------------------------------------------------------ module prerequisites / requirements endpoints
m1 = call('POST', f'/courses/{C}/modules', {'moduleTitle': 'Week One'}, token=ADA, expect=201, label='module 1')
m2 = call('POST', f'/courses/{C}/modules', {'moduleTitle': 'Week Two'}, token=ADA, expect=201, label='module 2')
call('POST', f"/modules/{m1['moduleId']}/publish", token=ADA, expect=200, label='publish module 1')
call('POST', f"/modules/{m2['moduleId']}/publish", token=ADA, expect=200, label='publish module 2')
w1 = page('Week One Reading', moduleId=m1['moduleId'])
hw = assignment('Week One Homework', 10, moduleId=m1['moduleId'])
w2 = page('Week Two Reading', moduleId=m2['moduleId'])
call('PUT', f"/modules/{m2['moduleId']}/prerequisites", {'prerequisiteModuleIds': [m1['moduleId']]}, token=ADA, expect=200, label='week 2 after week 1')
check(call('GET', f"/modules/{m2['moduleId']}/prerequisites", token=S1, expect=200, label='list prerequisites')[0]['prerequisiteModuleTitle'] == 'Week One', 'prerequisite listed')
call('POST', f"/modules/{m1['moduleId']}/requirements", {'contentId': w1['contentId'], 'requirementType': 'VIEW'}, token=ADA, expect=201, label='requirement')
check(len(call('GET', f"/modules/{m1['moduleId']}/requirements", token=S1, expect=200, label='list requirements')) == 1, 'requirement listed')
check(len(call('GET', f"/modules/{m1['moduleId']}/progress", token=EVE, expect=200, label='TA sees module progress')) == 4, 'progress for 4 students')
call('GET', f"/modules/{m1['moduleId']}/progress", token=S1, expect=403, label='students cannot')

# ------------------------------------------------------------------ system_resource_library
r1 = call('POST', '/resource-library', {'resourceName': 'Rubric Template', 'resourceType': 'TEMPLATE', 'resourceUrl': 'https://files.example/rubric.docx',
          'tags': ['rubric', 'writing']}, token=ADA, expect=201, label='teacher suggests a resource')
check(r1['scope'] == 'INSTITUTION' and not r1['isApproved'], 'waiting for approval')
call('POST', '/resource-library', {'resourceName': 'Nope', 'resourceType': 'TOOL'}, token=S1, expect=403, label='students cannot add')
call('POST', '/resource-library', {'resourceName': 'Bad tags', 'resourceType': 'TOOL', 'tags': 'x'}, token=ADA, expect=400, label='tags must be a list')
call('GET', f"/resource-library/{r1['resourceId']}", token=EVE, expect=404, label='others do not see unapproved resources')
call('PATCH', f"/resource-library/{r1['resourceId']}", {'resourceDescription': 'For essays'}, token=ADA, expect=200, label='creator edits before approval')
call('POST', f"/resource-library/{r1['resourceId']}/approve", token=ADA, expect=403, label='teachers cannot approve')
call('POST', f"/resource-library/{r1['resourceId']}/use", token=ADA, expect=409, label='unapproved resources cannot be used')
ap = call('POST', f"/resource-library/{r1['resourceId']}/approve", token=A, expect=200, label='admin approves')
check(ap['isApproved'] and ap['approvedBy'], 'approved')
call('PATCH', f"/resource-library/{r1['resourceId']}", {'resourceDescription': 'changed'}, token=ADA, expect=403, label='creator cannot edit after approval')
check(any(r['resourceId'] == r1['resourceId'] for r in call('GET', '/resource-library?q=essay', token=EVE, expect=200, label='staff search')), 'search by description')
check(all(r['resourceId'] != r1['resourceId'] for r in call('GET', '/resource-library', token=S1)), 'not public: students do not see it')
call('PATCH', f"/resource-library/{r1['resourceId']}", {'isPublic': True, 'rating': 4.5}, token=A, expect=200, label='manager makes it public and rates it')
check(any(r['resourceId'] == r1['resourceId'] for r in call('GET', '/resource-library', token=S1)), 'students see public resources')
call('POST', f"/resource-library/{r1['resourceId']}/use", token=ADA, expect=200, label='use')
dl = call('POST', f"/resource-library/{r1['resourceId']}/download", token=S1, expect=200, label='download')
check(dl['usageCount'] == 1 and dl['downloadCount'] == 1, 'counted')
pr = call('POST', '/resource-library', {'resourceName': 'Accessibility Checker', 'resourceType': 'TOOL', 'isPublic': True}, token=OWN, expect=201,
          label='platform resource')
check(pr['scope'] == 'PLATFORM' and pr['isApproved'], 'platform admins publish directly')
plat = next(r for r in call('GET', '/resource-library?type=TOOL', token=EVE, expect=200, label='institution sees platform tools') if r['resourceId'] == pr['resourceId'])
call('POST', f"/resource-library/{pr['resourceId']}/use", token=EVE, expect=200, label='use a platform resource')
call('POST', f"/resource-library/{pr['resourceId']}/download", token=EVE, expect=409, label='nothing to download')
call('PATCH', f"/resource-library/{pr['resourceId']}", {'rating': 5}, token=A, expect=403, label='institutions cannot change platform resources')
check(call('GET', f"/resource-library/{pr['resourceId']}", token=OWN)['usageCount'] == 1, 'platform counter updated')
call('GET', f"/resource-library/{r1['resourceId']}", token=X, expect=404, label='other institution cannot see ours')

# ------------------------------------------------------------------ release_condition
A1 = assignment('Diagnostic', 10)
gate = page('Opens Later')['contentId']
call('POST', f'/courses/{C}/release-conditions', {'targetType': 'CONTENT', 'targetId': gate, 'conditionType': 'DATE', 'dateFrom': T_(5)}, token=S1, expect=403, label='students cannot add conditions')
call('POST', f'/courses/{C}/release-conditions', {'targetType': 'CONTENT', 'targetId': gate, 'conditionType': 'DATE'}, token=ADA, expect=400, label='DATE needs a date')
call('POST', f'/courses/{C}/release-conditions', {'targetType': 'FILE', 'targetId': gate, 'conditionType': 'DATE', 'dateFrom': T_(1)}, token=ADA, expect=400, label='file targets not yet')
call('POST', f'/courses/{C}/release-conditions', {'targetType': 'CONTENT', 'targetId': A1['assignmentId'], 'conditionType': 'DATE', 'dateFrom': T_(1)},
     token=ADA, expect=400, label='target must match its type')
rc = call('POST', f'/courses/{C}/release-conditions', {'targetType': 'CONTENT', 'targetId': gate, 'conditionType': 'DATE', 'dateFrom': T_(5)},
          token=ADA, expect=201, label='date condition')
check((lock_of(gate, S1) or '').startswith('Available from'), 'locked until the date')
check(lock_of(gate, ADA) is None, 'staff are never held back')
call('DELETE', f"/courses/{C}/release-conditions/{rc['conditionId']}", token=ADA, expect=204, label='remove it')
check(lock_of(gate, S1) is None, 'open again')
sec_only = page('Section Two Notes')['contentId']
call('POST', f'/courses/{C}/release-conditions', {'targetType': 'CONTENT', 'targetId': sec_only, 'conditionType': 'MEMBER', 'memberSectionId': sec2,
     'memberUserId': s1}, token=ADA, expect=400, label='one member target')
call('POST', f'/courses/{C}/release-conditions', {'targetType': 'CONTENT', 'targetId': sec_only, 'conditionType': 'MEMBER', 'memberSectionId': sec2},
     token=ADA, expect=201, label='section-only condition')
check(lock_of(sec_only, S1) == 'Not available to you' and lock_of(sec_only, S4) is None, 'only section 02 gets it')
after_submit = page('Answer Key')['contentId']
call('POST', f'/courses/{C}/release-conditions', {'targetType': 'CONTENT', 'targetId': after_submit, 'conditionType': 'SUBMITTED', 'sourceType': 'CONTENT',
     'sourceId': P1}, token=ADA, expect=400, label='SUBMITTED needs an assignment')
call('POST', f'/courses/{C}/release-conditions', {'targetType': 'CONTENT', 'targetId': after_submit, 'conditionType': 'SUBMITTED', 'sourceType': 'ASSESSMENT',
     'sourceId': A1['assignmentId']}, token=ADA, expect=400, label='quiz sources come later')
call('POST', f'/courses/{C}/release-conditions', {'targetType': 'CONTENT', 'targetId': after_submit, 'conditionType': 'SUBMITTED', 'sourceType': 'ASSIGNMENT',
     'sourceId': A1['assignmentId']}, token=ADA, expect=201, label='open after submitting')
check(lock_of(after_submit, S1) == 'Submit "Diagnostic" first', 'tells the student what to do')
high = page('Extension Work')['contentId']
call('POST', f'/courses/{C}/release-conditions', {'targetType': 'CONTENT', 'targetId': high, 'conditionType': 'PERFORMANCE', 'sourceType': 'ASSIGNMENT',
     'sourceId': A1['assignmentId']}, token=ADA, expect=400, label='PERFORMANCE needs a score')
call('POST', f'/courses/{C}/release-conditions', {'targetType': 'CONTENT', 'targetId': high, 'conditionType': 'PERFORMANCE', 'sourceType': 'ASSIGNMENT',
     'sourceId': A1['assignmentId'], 'minScorePercent': 80}, token=ADA, expect=201, label='open at 80%')
check('not graded yet' in (lock_of(high, S1) or ''), 'not graded yet')
sub = grade(A1, S1, 6)
check(lock_of(after_submit, S1) is None, 'submitted: answer key opens')
check(lock_of(high, S1) == 'Score at least 80% on "Diagnostic"', '60% is not enough')
call('POST', f"/submissions/{sub['submissionId']}/grade", {'gradeEarned': 9}, token=ADA, expect=200, label='regrade to 9')
check(lock_of(high, S1) is None, '90% opens it')
seen = page('After Reading')['contentId']
call('POST', f'/courses/{C}/release-conditions', {'targetType': 'CONTENT', 'targetId': seen, 'conditionType': 'VIEWED', 'sourceType': 'CONTENT', 'sourceId': seen},
     token=ADA, expect=400, label='an item cannot depend on itself')
call('POST', f'/courses/{C}/release-conditions', {'targetType': 'CONTENT', 'targetId': seen, 'conditionType': 'VIEWED', 'sourceType': 'CONTENT',
     'sourceId': w2['contentId']}, token=ADA, expect=201, label='open after viewing Week Two Reading')
either = page('Either Way')['contentId']
call('POST', f'/courses/{C}/release-conditions', {'targetType': 'CONTENT', 'targetId': either, 'conditionType': 'MEMBER', 'memberUserId': s2, 'conditionGroup': 1},
     token=ADA, expect=201, label='group 1: s2')
call('POST', f'/courses/{C}/release-conditions', {'targetType': 'CONTENT', 'targetId': either, 'conditionType': 'COMPLETED', 'sourceType': 'CONTENT',
     'sourceId': P1, 'conditionGroup': 2}, token=ADA, expect=201, label='group 2: marked Reading One done')
check(lock_of(either, S2) is None and 'another set of conditions' in (lock_of(either, S1) or ''), 'any one group releases it')
call('POST', f'/content/{P1}/done', token=S1, expect=200, label='s1 marks Reading One done')
check(lock_of(either, S1) is None, 'second group met')
check(len(call('GET', f'/courses/{C}/release-conditions', token=EVE, expect=200, label='TA lists conditions')) == 6, 'six conditions')
call('GET', f'/courses/{C}/release-conditions', token=S1, expect=403, label='students cannot list')
# module-level and assignment targets
call('PUT', f"/modules/{m2['moduleId']}/prerequisites", {'prerequisiteModuleIds': []}, token=ADA, expect=200, label='drop the prerequisite')
call('POST', f'/courses/{C}/release-conditions', {'targetType': 'MODULE', 'targetId': m2['moduleId'], 'conditionType': 'DATE', 'dateFrom': T_(3)},
     token=ADA, expect=201, label='week 2 opens in 3 days')
mv = next(m for m in call('GET', f'/courses/{C}/modules', token=S1) if m['module']['moduleId'] == m2['moduleId'])
check(mv['progressStatus'] == 'LOCKED' and mv['lockReason'].startswith('Available from') and all(i['locked'] for i in mv['items']), 'module locked with its items')
check((lock_of(w2['contentId'], S1) or '').startswith('This module is not available yet'), 'item of a gated module')
hw2 = assignment('Reflection', 5)
call('POST', f'/courses/{C}/release-conditions', {'targetType': 'ASSIGNMENT', 'targetId': hw2['assignmentId'], 'conditionType': 'VIEWED', 'sourceType': 'CONTENT',
     'sourceId': P1}, token=ADA, expect=201, label='assignment opens after viewing Reading One')
st = call('GET', f"/assignments/{hw2['assignmentId']}", token=S4)['myStatus']
check(not st['canSubmit'] and st['lockReason'] == 'View "Reading One" first', 'assignment locked for s4')
call('POST', f"/assignments/{hw2['assignmentId']}/submissions", {'submissionText': 'x'}, token=S4, expect=409, label='cannot submit yet')
call('GET', f'/content/{P1}', token=S4, expect=200, label='s4 views Reading One')
check(call('GET', f"/assignments/{hw2['assignmentId']}", token=S4)['myStatus']['canSubmit'], 'now open')

# ------------------------------------------------------------------ mastery paths
quiz = assignment('Unit Check', 20)
call('POST', f'/courses/{C}/mastery-paths', {'triggerAssignmentId': quiz['assignmentId']}, token=S1, expect=403, label='students cannot')
mp = call('POST', f'/courses/{C}/mastery-paths', {'triggerAssignmentId': quiz['assignmentId']}, token=ADA, expect=201, label='mastery path on Unit Check')
call('POST', f'/courses/{C}/mastery-paths', {'triggerAssignmentId': quiz['assignmentId']}, token=ADA, expect=409, label='one path per assignment')
nopts = call('POST', f'/courses/{C}/assignments', {'assignmentTitle': 'Ungraded Thing', 'assignmentType': 'TEXT_ENTRY'}, token=ADA, expect=201, label='no points')
call('POST', f'/courses/{C}/mastery-paths', {'triggerAssignmentId': nopts['assignmentId']}, token=ADA, expect=400, label='paths need points')
R = mp['ruleId']
low = call('POST', f'/mastery-paths/{R}/ranges', {'lowerBoundPercent': 0, 'upperBoundPercent': 50}, token=ADA, expect=201, label='range 0-50')
mid = call('POST', f'/mastery-paths/{R}/ranges', {'lowerBoundPercent': 50, 'upperBoundPercent': 80}, token=ADA, expect=201, label='range 50-80 (touching)')
top = call('POST', f'/mastery-paths/{R}/ranges', {'lowerBoundPercent': 80, 'upperBoundPercent': 100}, token=ADA, expect=201, label='range 80-100')
call('POST', f'/mastery-paths/{R}/ranges', {'lowerBoundPercent': 40, 'upperBoundPercent': 60}, token=ADA, expect=409, label='overlap refused')
call('POST', f'/mastery-paths/{R}/ranges', {'lowerBoundPercent': 60, 'upperBoundPercent': 60}, token=ADA, expect=400, label='empty range refused')
call('PATCH', f"/mastery-path-ranges/{mid['rangeId']}", {'upperBoundPercent': 85}, token=ADA, expect=409, label='moving into another range refused')
remedial = page('Review Basics')['contentId']
practice = assignment('Practice Set', 10)
challenge = page('Challenge Problems')['contentId']
call('POST', f"/mastery-path-ranges/{low['rangeId']}/items", {'assignmentId': quiz['assignmentId']}, token=ADA, expect=400, label='trigger cannot be on its path')
call('POST', f"/mastery-path-ranges/{low['rangeId']}/items", {'contentId': remedial, 'assignmentId': practice['assignmentId']}, token=ADA, expect=400, label='one item at a time')
call('POST', f"/mastery-path-ranges/{low['rangeId']}/items", {'contentId': remedial}, token=ADA, expect=201, label='remedial for 0-50')
call('POST', f"/mastery-path-ranges/{low['rangeId']}/items", {'contentId': remedial}, token=ADA, expect=409, label='duplicate item')
call('POST', f"/mastery-path-ranges/{mid['rangeId']}/items", {'assignmentId': practice['assignmentId']}, token=ADA, expect=201, label='practice for 50-80')
call('POST', f"/mastery-path-ranges/{top['rangeId']}/items", {'contentId': challenge}, token=ADA, expect=201, label='challenge for 80-100')
full = call('GET', f'/mastery-paths/{R}', token=EVE, expect=200, label='TA reads the path')
check([len(g['items']) for g in full['ranges']] == [1, 1, 1] and full['triggerTitle'] == 'Unit Check', 'three ranges, one item each')
check('Assigned after your grade on "Unit Check"' in (lock_of(remedial, S1) or ''), 'locked until graded')
grade(quiz, S1, 9)                                    # 45%
check(lock_of(remedial, S1) is None and 'Not part of your path' in (lock_of(challenge, S1) or ''), '45%: remedial only')
pst = call('GET', f"/assignments/{practice['assignmentId']}", token=S1)['myStatus']
check(not pst['canSubmit'] and 'Not part of your path' in pst['lockReason'], 'practice locked for s1')
mine = call('GET', f'/mastery-paths/{R}/mine', token=S1, expect=200, label="s1's path")
check(float(mine['scorePercent']) == 45 and mine['rangeId'] == low['rangeId'] and [i['title'] for i in mine['releasedItems']] == ['Review Basics'], 'path summary')
grade(quiz, S2, 20)                                   # 100%: top range includes its upper bound
check(lock_of(challenge, S2) is None and lock_of(remedial, S2) is not None, '100% lands in the top range')
grade(quiz, S3, 13)                                   # 65%
check(call('GET', f"/assignments/{practice['assignmentId']}", token=S3)['myStatus']['canSubmit'], '65%: practice opens')
check(lock_of(remedial, ADA) is None, 'staff see everything')
call('GET', f'/courses/{C}/mastery-paths', token=S1, expect=403, label='students cannot list paths')

# ------------------------------------------------------------------ course pacing
call('POST', f'/courses/{C}/paces', {'excludeWeekends': True}, token=S1, expect=403, label='students cannot pace')
call('POST', f'/courses/{C}/paces', {'blackoutDates': 'tomorrow'}, token=ADA, expect=400, label='blackout dates must be a list of dates')
call('POST', f'/courses/{C}/paces', {'sectionId': sec2, 'userId': s4}, token=ADA, expect=400, label='one scope only')
pace = call('POST', f'/courses/{C}/paces', {'excludeWeekends': True, 'blackoutDates': [D(2)]}, token=ADA, expect=201, label='course pace')
check(pace['scope'] == 'COURSE' and pace['paceStatus'] == 'DRAFT' and [i['title'] for i in pace['items']] == ['Week One Reading', 'Week One Homework', 'Week Two Reading'],
      'module items in order')
call('POST', f'/courses/{C}/paces', {}, token=ADA, expect=409, label='one pace per scope')
items = {i['title']: i for i in pace['items']}
call('PATCH', f"/pace-items/{items['Week One Homework']['paceItemId']}", {'durationDays': 3}, token=ADA, expect=200, label='homework gets 3 days')
call('PATCH', f"/pace-items/{items['Week One Reading']['paceItemId']}", {'durationDays': -1}, token=ADA, expect=400, label='no negative durations')
sch = call('GET', f"/paces/{pace['paceId']}/schedule", token=ADA, expect=200, label='preview from today')
dues = [date.fromisoformat(s['dueDate']) for s in sch['items']]
check(all(d.weekday() < 5 for d in dues) and date.fromisoformat(D(2)) not in dues and dues == sorted(dues), 'no weekends, no blackout day, in order')
call('GET', f'/courses/{C}/pace/mine', token=S1, expect=404, label='no published pace yet')
call('POST', f"/paces/{pace['paceId']}/publish", token=ADA, expect=200, label='publish the course pace')
mp1 = call('GET', f'/courses/{C}/pace/mine', token=S1, expect=200, label="s1's paced schedule")
hw_due = next(s['dueDate'] for s in mp1['items'] if s['itemId'] == hw['assignmentId'])
check(mp1['scope'] == 'COURSE', 'course pace applies')
check(call('GET', f"/assignments/{hw['assignmentId']}", token=S1)['myStatus']['dateDue'][:10] == hw_due, 'the paced date becomes the due date')
spc = call('POST', f'/courses/{C}/paces', {'sectionId': sec2, 'excludeWeekends': False}, token=ADA, expect=201, label='section 02 pace')
call('POST', f"/paces/{spc['paceId']}/publish", token=ADA, expect=200, label='publish it')
check(call('GET', f'/courses/{C}/pace/mine', token=S4, expect=200, label="s4's schedule")['scope'] == 'SECTION', 'section pace wins for section 02')
check(call('GET', f'/courses/{C}/pace/mine', token=S1)['scope'] == 'COURSE', 'others keep the course pace')
stu = call('POST', f'/courses/{C}/paces', {'userId': s1, 'dateEnd': T_(1)}, token=ADA, expect=201, label='s1 gets a compressed pace')
for i in stu['items']:
    call('PATCH', f"/pace-items/{i['paceItemId']}", {'durationDays': 5}, token=ADA, expect=200, label='long durations')
call('POST', f"/paces/{stu['paceId']}/publish", token=ADA, expect=200, label='publish s1 pace')
cs = call('GET', f'/courses/{C}/pace/mine', token=S1)
check(cs['scope'] == 'STUDENT' and cs['compressed'] and date.fromisoformat(cs['endDate']) <= date.fromisoformat(D(4)), 'student pace wins and is compressed')
call('DELETE', f"/paces/{stu['paceId']}", token=ADA, expect=409, label='published paces are archived, not deleted')
call('POST', f"/paces/{stu['paceId']}/archive", token=ADA, expect=200, label='archive s1 pace')
check(call('GET', f'/courses/{C}/pace/mine', token=S1)['scope'] == 'COURSE', 'back to the course pace')
w3 = page('Week One Extra', moduleId=m1['moduleId'])
rf = call('POST', f"/paces/{pace['paceId']}/refresh", token=ADA, expect=200, label='refresh picks up a new item')
check('Week One Extra' in [i['title'] for i in rf['items']], 'new module item added')
call('GET', f'/courses/{C}/paces', token=S1, expect=403, label='students cannot list paces')
check(len(call('GET', f'/courses/{C}/paces', token=EVE, expect=200, label='TA lists paces')) == 3, 'three paces')
dr = call('POST', f'/courses/{C}/paces', {'userId': s3}, token=ADA, expect=201, label='draft pace')
call('DELETE', f"/paces/{dr['paceId']}", token=ADA, expect=204, label='delete a draft')

# ------------------------------------------------------------------ blueprint copies of content blocks
BP = call('POST', '/courses', {'courseCode': 'LBP100', 'courseTitle': 'Design Blueprint', 'instructorUserId': ada, 'courseKind': 'BLUEPRINT'},
          token=A, expect=201, label='blueprint')['courseId']
CH = call('POST', '/courses', {'courseCode': 'LCH100', 'courseTitle': 'Design (A)', 'instructorUserId': ada}, token=A, expect=201, label='associated course')['courseId']
bpg = pub(call('POST', f'/courses/{BP}/content', {'typeCode': 'PAGE', 'contentTitle': 'Blueprint Page'}, token=ADA, expect=201, label='blueprint page'))['contentId']
call('POST', f'/content/{bpg}/blocks', {'blockType': 'TEXT', 'blockContent': 'one'}, token=ADA, expect=201, label='blueprint block 1')
call('POST', f'/content/{bpg}/blocks', {'blockType': 'TABLE', 'blockContent': 'two'}, token=ADA, expect=201, label='blueprint block 2')
call('POST', f'/courses/{BP}/blueprint/associations', {'courseIds': [CH]}, token=A, expect=201, label='associate')
call('PUT', f'/courses/{BP}/blueprint/locks', {'objectType': 'CONTENT', 'objectId': bpg}, token=ADA, expect=200, label='lock the page')
call('POST', f'/courses/{BP}/blueprint/syncs', {}, token=ADA, expect=201, label='sync')
copy = next(c for c in call('GET', f'/courses/{CH}/content', token=ADA) if c['contentTitle'] == 'Blueprint Page')['contentId']
cb = call('GET', f'/content/{copy}/blocks', token=ADA, expect=200, label='copied blocks')
check([b['blockContent'] for b in cb] == ['one', 'two'], 'blocks copied in order')
call('POST', f'/content/{copy}/blocks', {'blockType': 'TEXT'}, token=ADA, expect=409, label='locked page: blocks cannot change in the course')

passed = sum(1 for r in results if r[0])
print(f'{passed}/{len(results)} checks passed')
for ok, label, status, exp in results:
    print(('PASS' if ok else 'FAIL'), f'{status!s:>3}', label)
sys.exit(0 if passed == len(results) else 1)
