"""End-to-end test for content items, modules, prerequisites, requirements and student progress.

Needs a running backend whose database has the bootstrap platform owner (owner@lms-company.com / Owner-Pass-2026!).
Creates its own institutions (codes LRN and LRX), so it can run after the other API tests on the same database.
    python scripts/api_test_content.py
"""
import json, os, sys, urllib.request
from datetime import datetime, timedelta, timezone

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


def by_title(modules):
    return {m['module']['moduleTitle']: m for m in modules}


# ------------------------------------------------------------------ setup
T = call('POST', '/auth/login', {'login': 'owner@lms-company.com', 'password': 'Owner-Pass-2026!'}, expect=200, label='platform owner login')['accessToken']
call('POST', '/platform/tenants', {'tenantName': 'Learning Uni', 'tenantCode': 'LRN',
     'admin': {'username': 'admin.lrn', 'email': 'admin@lrn.edu', 'password': 'Admin-Pass-2026'}}, token=T, expect=201, label='create tenant LRN')
call('POST', '/platform/tenants', {'tenantName': 'Elsewhere', 'tenantCode': 'LRX',
     'admin': {'username': 'admin.lrx', 'email': 'admin@lrx.edu', 'password': 'Admin-Pass-2026'}}, token=T, expect=201, label='create tenant LRX')
A = login('LRN', 'admin.lrn', 'Admin-Pass-2026')
X = login('LRX', 'admin.lrx', 'Admin-Pass-2026')


def mk(username, utype):
    return call('POST', '/users', {'username': username, 'email': f'{username}@lrn.edu', 'userType': utype, 'password': 'Welcome-2026-x',
                'profile': {'firstName': username.split('.')[1].title(), 'lastName': 'Test'}}, token=A, expect=201, label=f'create {username}')['user']['userId']


ada, eve, s1, s2 = mk('prof.ada', 'INSTRUCTOR'), mk('ta.eve', 'TEACHING_ASSISTANT'), mk('stu.one', 'STUDENT'), mk('stu.two', 'STUDENT')
c = call('POST', '/courses', {'courseCode': 'CS101', 'courseTitle': 'Intro to Programming', 'instructorUserId': ada}, token=A, expect=201, label='create course')
C = c['courseId']
sec1 = call('GET', f'/courses/{C}/sections', token=A)[0]['sectionId']
sec2 = call('POST', f'/courses/{C}/sections', {'sectionNumber': '02'}, token=A, expect=201, label='create section 02')['sectionId']
call('POST', f'/sections/{sec1}/staff', {'userId': eve, 'role': 'TEACHING_ASSISTANT'}, token=A, expect=201, label='add TA')
call('POST', f'/sections/{sec1}/enrollments', {'userId': s1}, token=A, expect=201, label='enroll student one in 01')
call('POST', f'/sections/{sec2}/enrollments', {'userId': s2}, token=A, expect=201, label='enroll student two in 02')
call('POST', f'/courses/{C}/publish', token=A, expect=200, label='publish course')
ADA, EVE, S1, S2 = login('LRN', 'prof.ada'), login('LRN', 'ta.eve'), login('LRN', 'stu.one'), login('LRN', 'stu.two')

# ------------------------------------------------------------------ content types
ts = call('GET', '/content-types', token=ADA, expect=200, label='list content types (standard ones created)')
check({'PAGE', 'VIDEO', 'FILE', 'LINK'} <= {t['typeCode'] for t in ts}, 'standard types exist')
call('POST', '/content-types', {'typeName': 'Case Study', 'typeCode': 'CASE_STUDY'}, token=ADA, expect=403, label='teacher cannot add content types')
call('POST', '/content-types', {'typeName': 'Case Study', 'typeCode': 'case_study'}, token=A, expect=201, label='admin adds a content type')
call('POST', '/content-types', {'typeName': 'Page again', 'typeCode': 'PAGE'}, token=A, expect=409, label='duplicate type code rejected')

# ------------------------------------------------------------------ modules
M1 = call('POST', f'/courses/{C}/modules', {'moduleTitle': 'Week 1 Getting Started', 'moduleType': 'WEEK'}, token=ADA, expect=201, label='teacher creates module 1')
check(M1['isPublished'] is False and M1['moduleNumber'] == 1 and M1['requireSequentialProgress'] is False, 'module 1 defaults')
M2 = call('POST', f'/courses/{C}/modules', {'moduleTitle': 'Week 2 Variables', 'requireSequentialProgress': True}, token=ADA, expect=201, label='create module 2 (sequential)')
M3 = call('POST', f'/courses/{C}/modules', {'moduleTitle': 'Week 3 Section 02 Lab', 'sectionId': sec2}, token=ADA, expect=201, label='create module 3 (section 02 only)')
future = (datetime.now(timezone.utc) + timedelta(days=30)).isoformat()
M4 = call('POST', f'/courses/{C}/modules', {'moduleTitle': 'Week 4 Coming Soon', 'dateUnlock': future}, token=ADA, expect=201, label='create module 4 (unlocks later)')
call('POST', f'/courses/{C}/modules', {'moduleTitle': 'Nope'}, token=EVE, expect=403, label='TA cannot create modules')
call('POST', f'/courses/{C}/modules', {'moduleTitle': 'Bad', 'sectionId': '00000000-0000-0000-0000-000000000000'},
     token=ADA, expect=400, label='module section must belong to the course')


def page(title, module=None, **extra):
    body = {'typeCode': 'PAGE', 'contentTitle': title, 'contentBody': f'<p>{title} body</p>', **extra}
    if module: body['moduleId'] = module['moduleId']
    return call('POST', f'/courses/{C}/content', body, token=ADA, expect=201, label=f'create page {title}')


welcome = page('Welcome Page', M1, isFrontPage=True)
check(welcome['isFrontPage'] and welcome['isPublished'] and welcome['typeCode'] == 'PAGE' and welcome['editingRoles'] == 'TEACHERS', 'front page is published, DB defaults returned')
syllabus = page('Syllabus Overview', M1)
secret = page('Answer Key', M1, accessLevel='INSTRUCTOR_ONLY')
variables = page('Variables Intro', M2)
lecture = call('POST', f'/courses/{C}/content', {'typeCode': 'VIDEO', 'contentTitle': 'Lecture Video', 'moduleId': M2['moduleId'],
               'video': {'videoUrl': 'https://videos.example.edu/cs101/w2.mp4', 'videoProvider': 'CUSTOM', 'videoDurationSeconds': 900, 'captionsAvailable': True}},
               token=ADA, expect=201, label='create video item')
check(lecture['video']['videoDurationSeconds'] == 900 and lecture['displayOrder'] == 2, 'video details stored, placed second')
practice = page('Practice Problems', M2, editingRoles='TEACHERS_STUDENTS')
lab = page('Lab Instructions', M3)
soon = page('Week 4 Reading', M4)
call('POST', f'/courses/{C}/content', {'typeCode': 'VIDEO', 'contentTitle': 'No video'}, token=ADA, expect=400, label='VIDEO item needs video details')
call('POST', f'/courses/{C}/content', {'typeCode': 'HOLOGRAM', 'contentTitle': 'Unknown'}, token=ADA, expect=400, label='unknown content type')
call('POST', f'/courses/{C}/content', {'typeCode': 'PAGE', 'contentTitle': 'Wrong module', 'moduleId': '00000000-0000-0000-0000-000000000000'},
     token=ADA, expect=400, label='item module must belong to the course')

# requirements + prerequisites
call('POST', f"/modules/{M1['moduleId']}/requirements", {'contentId': welcome['contentId'], 'requirementType': 'VIEW'}, token=ADA, expect=201, label='M1: view Welcome')
call('POST', f"/modules/{M1['moduleId']}/requirements", {'contentId': syllabus['contentId'], 'requirementType': 'MARK_DONE'}, token=ADA, expect=201, label='M1: mark Syllabus done')
call('POST', f"/modules/{M2['moduleId']}/requirements", {'contentId': variables['contentId'], 'requirementType': 'VIEW'}, token=ADA, expect=201, label='M2: view Variables')
r = call('POST', f"/modules/{M2['moduleId']}/requirements", {'contentId': lecture['contentId'], 'requirementType': 'MARK_DONE'}, token=ADA, expect=201, label='M2: mark Lecture done')
check(len(r['requirements']) == 2, 'M2 has 2 requirements')
call('POST', f"/modules/{M2['moduleId']}/requirements", {'contentId': welcome['contentId'], 'requirementType': 'VIEW'}, token=ADA, expect=400, label='requirement item must be in the module')
call('POST', f"/modules/{M2['moduleId']}/requirements", {'contentId': variables['contentId'], 'requirementType': 'SUBMIT'}, token=ADA, expect=400, label='SUBMIT requirement not supported yet')
p2 = call('PUT', f"/modules/{M2['moduleId']}/prerequisites", {'prerequisiteModuleIds': [M1['moduleId']]}, token=ADA, expect=200, label='M2 requires M1')
check(p2['prerequisiteModuleIds'] == [M1['moduleId']], 'prerequisite saved')
call('PUT', f"/modules/{M1['moduleId']}/prerequisites", {'prerequisiteModuleIds': [M2['moduleId']]}, token=ADA, expect=400, label='prerequisite must be an earlier module')

# publish
call('POST', f"/modules/{M1['moduleId']}/publish?includeItems=true", token=ADA, expect=200, label='publish M1 with items')
call('POST', f"/modules/{M2['moduleId']}/publish?includeItems=true", token=ADA, expect=200, label='publish M2 with items')
call('POST', f"/modules/{M3['moduleId']}/publish?includeItems=true", token=ADA, expect=200, label='publish M3 with items')
call('POST', f"/modules/{M4['moduleId']}/publish?includeItems=true", token=ADA, expect=200, label='publish M4 with items')
draft = page('Draft Notes', M2)                      # created after publishing: stays unpublished

# ------------------------------------------------------------------ what each person sees
tv = by_title(call('GET', f'/courses/{C}/modules', token=ADA, expect=200, label='teacher module view'))
check(len(tv) == 4 and tv['Week 2 Variables'].get('progressStatus') is None, 'teacher sees 4 modules, no progress')
check(len(tv['Week 2 Variables']['items']) == 4 and len(tv['Week 1 Getting Started']['items']) == 3, 'teacher sees drafts and instructor-only items')
sv = by_title(call('GET', f'/courses/{C}/modules', token=S1, expect=200, label='student one module view'))
check(set(sv) == {'Week 1 Getting Started', 'Week 2 Variables', 'Week 4 Coming Soon'}, 'section-02 module hidden from student one')
check(sv['Week 1 Getting Started']['progressStatus'] == 'UNLOCKED', 'M1 unlocked')
check(sv['Week 2 Variables']['progressStatus'] == 'LOCKED' and 'Week 1' in sv['Week 2 Variables'].get('lockReason', ''), 'M2 locked until M1 is complete')
check(sv['Week 4 Coming Soon']['progressStatus'] == 'LOCKED' and sv['Week 4 Coming Soon'].get('lockReason', '').startswith('Unlocks on'), 'M4 locked until its date')
check([i['contentTitle'] for i in sv['Week 1 Getting Started']['items']] == ['Welcome Page', 'Syllabus Overview'], 'student does not see instructor-only item')
check(all(i['contentTitle'] != 'Draft Notes' for i in sv['Week 2 Variables']['items']), 'student does not see unpublished item')
s2v = by_title(call('GET', f'/courses/{C}/modules', token=S2, expect=200, label='student two module view'))
check('Week 3 Section 02 Lab' in s2v, 'section-02 student sees section module')
call('GET', f"/content/{secret['contentId']}", token=S1, expect=404, label='instructor-only item hidden')
call('GET', f"/content/{draft['contentId']}", token=S1, expect=404, label='unpublished item hidden')
call('GET', f"/content/{lab['contentId']}", token=S1, expect=404, label='other section module item hidden')
lk = call('GET', f"/content/{variables['contentId']}", token=S1, expect=200, label='student opens item of locked module')
check(lk.get('lockReason') and lk.get('contentBody') is None, 'locked item comes without body')
call('POST', f"/content/{variables['contentId']}/done", token=S1, expect=409, label='cannot complete a locked item')
sl = call('GET', f'/courses/{C}/content', token=S1, expect=200, label='student content list')
check({x['contentTitle'] for x in sl} == {'Welcome Page', 'Syllabus Overview', 'Variables Intro', 'Lecture Video', 'Practice Problems', 'Week 4 Reading'}, 'student list = visible items')
check(len(call('GET', f'/courses/{C}/content?type=VIDEO', token=ADA, expect=200, label='teacher filters by type')) == 1, 'type filter')
fp = call('GET', f'/courses/{C}/front-page', token=S1, expect=200, label='student opens front page')
check(fp['contentTitle'] == 'Welcome Page' and fp.get('contentBody'), 'front page body visible')

# ------------------------------------------------------------------ progress through modules
sv = by_title(call('GET', f'/courses/{C}/modules', token=S1, expect=200, label='after viewing Welcome'))
check(sv['Week 1 Getting Started']['progressStatus'] == 'STARTED', 'M1 started after first view')
pr = call('POST', f"/content/{syllabus['contentId']}/done", token=S1, expect=200, label='student marks Syllabus done')
check(pr['status'] == 'COMPLETED' and pr['dateCompleted'], 'content progress completed')
call('POST', f"/content/{syllabus['contentId']}/done", token=ADA, expect=403, label='teacher cannot mark done')
sv = by_title(call('GET', f'/courses/{C}/modules', token=S1, expect=200, label='after finishing M1'))
check(sv['Week 1 Getting Started']['progressStatus'] == 'COMPLETED', 'M1 completed')
w2 = sv['Week 2 Variables']
check(w2['progressStatus'] == 'UNLOCKED', 'M2 unlocked once M1 is complete')
locks = {i['contentTitle']: i['locked'] for i in w2['items']}
check(locks == {'Variables Intro': False, 'Lecture Video': True, 'Practice Problems': True}, 'sequential: only the first step is open')
lv = call('GET', f"/content/{lecture['contentId']}", token=S1, expect=200, label='student opens step 2 too early')
check(lv.get('lockReason') and 'Variables Intro' in lv['lockReason'] and lv.get('video') is None, 'step 2 locked until step 1')
call('GET', f"/content/{variables['contentId']}", token=S1, expect=200, label='student views Variables Intro')
lv = call('GET', f"/content/{lecture['contentId']}", token=S1, expect=200, label='student opens Lecture Video')
check(lv.get('lockReason') is None and lv['video']['videoUrl'].endswith('w2.mp4'), 'step 2 open after step 1')
locks = {i['contentTitle']: i['locked'] for i in by_title(call('GET', f'/courses/{C}/modules', token=S1))['Week 2 Variables']['items']}
check(locks['Practice Problems'] is True, 'step 3 still locked until the lecture is marked done')
call('POST', f"/content/{lecture['contentId']}/done", token=S1, expect=200, label='mark Lecture done')
w2 = by_title(call('GET', f'/courses/{C}/modules', token=S1, expect=200, label='after finishing M2'))['Week 2 Variables']
check(w2['progressStatus'] == 'COMPLETED' and not any(i.get('locked') for i in w2['items']), 'M2 completed, everything open')
check({i['contentTitle']: i['completed'] for i in w2['items']} == {'Variables Intro': True, 'Lecture Video': True, 'Practice Problems': False}, 'per-item completion')
call('DELETE', f"/content/{lecture['contentId']}/done", token=S1, expect=200, label='undo mark done')
check(by_title(call('GET', f'/courses/{C}/modules', token=S1))['Week 2 Variables']['progressStatus'] == 'STARTED', 'undo puts M2 back to STARTED')
call('POST', f"/content/{lecture['contentId']}/done", token=S1, expect=200, label='mark done again')
mp = call('GET', f"/content/{variables['contentId']}/progress", token=S1, expect=200, label='my progress on an item')
check(mp['viewCount'] >= 1 and mp['status'] == 'IN_PROGRESS', 'view recorded')

prog = call('GET', f"/modules/{M1['moduleId']}/progress", token=EVE, expect=200, label='TA sees module progress')
pm = {p['username']: p for p in prog}
check(pm['stu.one']['progressStatus'] == 'COMPLETED' and pm['stu.one']['requirementsMet'] == 2 and pm['stu.one']['requirementsTotal'] == 2 and pm['stu.one']['dateCompleted'],
      'student one completed M1 (2/2)')
check(pm['stu.two']['progressStatus'] == 'UNLOCKED' and pm['stu.two']['requirementsMet'] == 0, 'student two not started')
call('GET', f"/modules/{M1['moduleId']}/progress", token=S1, expect=403, label='student cannot see class progress')

# ------------------------------------------------------------------ editing rules
call('PATCH', f"/content/{practice['contentId']}", {'contentBody': '<p>My answer: x = 5</p>'}, token=S1, expect=200, label='student edits a student-editable page')
call('PATCH', f"/content/{practice['contentId']}", {'accessLevel': 'PUBLIC'}, token=S1, expect=403, label='student cannot change page settings')
call('PATCH', f"/content/{syllabus['contentId']}", {'contentBody': 'hacked'}, token=S1, expect=403, label='student cannot edit a teacher-only page')
call('PATCH', f"/content/{syllabus['contentId']}", {'contentTitle': 'Syllabus and Policies'}, token=ADA, expect=200, label='teacher edits title')

# front page rules
call('POST', f"/content/{welcome['contentId']}/unpublish", token=ADA, expect=409, label='front page cannot be unpublished')
call('DELETE', f"/content/{welcome['contentId']}", token=ADA, expect=409, label='front page cannot be deleted')
call('PATCH', f"/content/{syllabus['contentId']}", {'isFrontPage': True}, token=ADA, expect=200, label='make Syllabus the front page')
check(call('GET', f'/courses/{C}/front-page', token=ADA)['contentId'] == syllabus['contentId'], 'front page switched')
check(call('GET', f"/content/{welcome['contentId']}", token=ADA)['isFrontPage'] is False, 'old front page cleared')

# moving and ordering
mv = call('POST', f"/content/{practice['contentId']}/move", {'moduleId': M1['moduleId']}, token=ADA, expect=200, label='move Practice into M1')
check(mv['moduleId'] == M1['moduleId'], 'moved')
call('POST', f"/content/{lecture['contentId']}/move", {'moduleId': None}, token=ADA, expect=200, label='move Lecture out of modules')
check(len(call('GET', f"/modules/{M2['moduleId']}", token=ADA)['module']['requirements']) == 1, "Lecture's requirement left with it")
items_m1 = [i['contentId'] for i in call('GET', f"/modules/{M1['moduleId']}", token=ADA)['items']]
call('PUT', f"/modules/{M1['moduleId']}/items/order", {'orderedIds': items_m1[:1]}, token=ADA, expect=400, label='item order must list every item')
ro = call('PUT', f"/modules/{M1['moduleId']}/items/order", {'orderedIds': list(reversed(items_m1))}, token=ADA, expect=200, label='reorder items')
check([i['contentId'] for i in ro['items']] == list(reversed(items_m1)), 'items reordered')
mids = [m['module']['moduleId'] for m in call('GET', f'/courses/{C}/modules', token=ADA)]
call('PUT', f'/courses/{C}/modules/order', {'orderedIds': mids[:2]}, token=ADA, expect=400, label='module order must list every module')
mo = call('PUT', f'/courses/{C}/modules/order', {'orderedIds': [mids[1], mids[0], mids[2], mids[3]]}, token=ADA, expect=200, label='reorder modules')
check(mo[0]['module']['moduleId'] == mids[1], 'modules reordered')
call('DELETE', f"/modules/{M4['moduleId']}", token=ADA, expect=204, label='delete module 4')
check(call('GET', f"/content/{soon['contentId']}", token=ADA, expect=200, label='item of deleted module still exists').get('moduleId') is None, 'item kept outside modules')

# isolation
call('GET', f'/courses/{C}/modules', token=X, expect=404, label='other institution cannot see modules')
call('GET', f"/content/{welcome['contentId']}", token=X, expect=404, label='other institution cannot see content')
call('POST', f'/courses/{C}/unpublish', token=ADA, expect=200, label='unpublish course')
call('GET', f"/content/{syllabus['contentId']}", token=S1, expect=404, label='student cannot open content of unpublished course')

passed = sum(1 for r in results if r[0])
print(f'{passed}/{len(results)} checks passed')
for ok, label, status, exp in results:
    print(('PASS' if ok else 'FAIL'), f'{status!s:>3}', label)
sys.exit(0 if passed == len(results) else 1)
