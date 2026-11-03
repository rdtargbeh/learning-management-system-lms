"""End-to-end test for the rest of academic structure (domain 03): section staff, course prerequisites (enforced at
enrollment), meeting schedules and timetables, learning outcomes, course navigation, and blueprint courses
(associations, locks, sync with exceptions, deletions, course settings).

Needs a running backend whose database has the bootstrap platform owner (owner@lms-company.com / Owner-Pass-2026!).
Minimum-grade checks need final grades, which have no API yet: the test writes them with psql (LMS_PSQL, default
su postgres -c "psql -d lms_layers"). Creates its own institutions (ACD and ACX).
    python scripts/api_test_academic.py
"""
import json, os, subprocess, sys, urllib.request
from datetime import date, timedelta

BASE = os.environ.get('LMS_API', 'http://localhost:8080/api/v1')
PSQL = os.environ.get('LMS_PSQL', 'su postgres -c "psql -d lms_layers -qAt -c \\"{sql}\\""')
results = []
today = date.today()
D = lambda n: (today + timedelta(days=n)).isoformat()


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


def sql(statement):
    return subprocess.run(PSQL.format(sql=statement.replace('"', '\\"')), shell=True, capture_output=True, text=True).stdout.strip()


def alist(course_id, token, label=None):
    return [v['assignment'] for v in call('GET', f'/courses/{course_id}/assignments', token=token, expect=200, label=label or 'list assignments')]


def login(code, user, pw='Welcome-2026-x'):
    return call('POST', '/auth/login', {'tenantCode': code, 'login': user, 'password': pw}, expect=200, label=f'login {user}')['accessToken']


# ------------------------------------------------------------------ setup
T = call('POST', '/auth/login', {'login': 'owner@lms-company.com', 'password': 'Owner-Pass-2026!'}, expect=200, label='platform owner login')['accessToken']
tA = call('POST', '/platform/tenants', {'tenantName': 'Academic University', 'tenantCode': 'ACD',
          'admin': {'username': 'admin.acd', 'email': 'admin@acd.edu', 'password': 'Admin-Pass-2026'}}, token=T, expect=201, label='create tenant ACD')
call('POST', '/platform/tenants', {'tenantName': 'Academic Elsewhere', 'tenantCode': 'ACX',
     'admin': {'username': 'admin.acx', 'email': 'admin@acx.edu', 'password': 'Admin-Pass-2026'}}, token=T, expect=201, label='create tenant ACX')
TID = tA['tenant']['tenantId']
A = login('ACD', 'admin.acd', 'Admin-Pass-2026')
X = login('ACX', 'admin.acx', 'Admin-Pass-2026')


def mk(username, utype):
    return call('POST', '/users', {'username': username, 'email': f'{username}@acd.edu', 'userType': utype, 'password': 'Welcome-2026-x',
                'profile': {'firstName': username.split('.')[1].title(), 'lastName': 'Test'}}, token=A, expect=201, label=f'create {username}')['user']['userId']


ada, tom = mk('prof.ada', 'INSTRUCTOR'), mk('prof.tom', 'INSTRUCTOR')
s1, s2, s3, s4 = (mk(f'stu.s{i}', 'STUDENT') for i in range(1, 5))
ADA, TOM = login('ACD', 'prof.ada'), login('ACD', 'prof.tom')
S1, S2, S3, S4 = (login('ACD', f'stu.s{i}') for i in range(1, 5))
college = call('POST', '/org-units', {'unitName': 'College of Engineering', 'unitCode': 'ENG', 'unitType': 'COLLEGE'}, token=A, expect=201, label='college')
dept = call('POST', '/org-units', {'unitName': 'Computing', 'unitCode': 'CMP', 'unitType': 'DEPARTMENT', 'parentOrgUnitId': college['orgUnitId']},
            token=A, expect=201, label='department')
fall = call('POST', '/terms', {'termName': 'Fall', 'termCode': 'F26', 'dateStart': D(-30), 'dateEnd': D(90), 'isCurrent': True}, token=A, expect=201, label='term')


def course(code, title, teacher, **extra):
    c = call('POST', '/courses', {'courseCode': code, 'courseTitle': title, 'instructorUserId': teacher, 'termId': fall['termId'], **extra},
             token=A, expect=201, label=f'course {code}')
    sec = call('GET', f"/courses/{c['courseId']}/sections", token=A)[0]
    return c['courseId'], sec['sectionId']


C101, S101 = course('CS101', 'Programming I', ada, orgUnitId=dept['orgUnitId'])
C201, S201 = course('CS201', 'Programming II', tom, orgUnitId=dept['orgUnitId'])
C201L, S201L = course('CS201L', 'Programming II Lab', tom)
C202, S202 = course('CS202', 'Data Structures', tom)
M100, _ = course('MATH100', 'Discrete Math', tom)
for cid in (C101, C201, C201L, C202):
    call('POST', f'/courses/{cid}/publish', token=A, expect=200, label='publish')

# ------------------------------------------------------------------ section staff (course_instructor)
st = call('GET', f'/sections/{S101}/staff', token=ADA, expect=200, label='teacher lists section staff')
ADA_STAFF = st[0]['courseInstructorId']
check(len(st) == 1 and st[0]['role'] == 'PRIMARY', 'primary instructor from course creation')
call('POST', f'/sections/{S202}/staff', {'userId': ada, 'role': 'CO_INSTRUCTOR'}, token=ADA, expect=403, label='teachers cannot add staff')
co = call('POST', f'/sections/{S202}/staff', {'userId': ada, 'role': 'CO_INSTRUCTOR'}, token=A, expect=201, label='admin adds a co-instructor')
call('POST', f'/sections/{S202}/staff', {'userId': ada, 'role': 'GRADER'}, token=A, expect=409, label='already staff')

# ------------------------------------------------------------------ prerequisites
call('POST', f'/courses/{C201}/prerequisites', {'prerequisiteCourseId': C101}, token=TOM, expect=403, label='teacher cannot set prerequisites')
p1 = call('POST', f'/courses/{C201}/prerequisites', {'prerequisiteCourseId': C101}, token=A, expect=201, label='CS201 requires CS101')
check(p1['prerequisiteType'] == 'REQUIRED' and p1['prerequisiteCourseCode'] == 'CS101', 'REQUIRED by default')
call('POST', f'/courses/{C201}/prerequisites', {'prerequisiteCourseId': C101}, token=A, expect=409, label='duplicate')
call('POST', f'/courses/{C201}/prerequisites', {'prerequisiteCourseId': C201}, token=A, expect=400, label='a course cannot require itself')
call('POST', f'/courses/{C101}/prerequisites', {'prerequisiteCourseId': C201}, token=A, expect=400, label='cycle refused')
call('POST', f'/courses/{C201}/prerequisites', {'prerequisiteCourseId': C201L, 'prerequisiteType': 'COREQUISITE', 'minGrade': 'B'}, token=A,
     expect=400, label='minimum grade only for REQUIRED')
call('POST', f'/courses/{C201}/prerequisites', {'prerequisiteCourseId': C201L, 'prerequisiteType': 'COREQUISITE'}, token=A, expect=201, label='lab corequisite')
rec = call('POST', f'/courses/{C201}/prerequisites', {'prerequisiteCourseId': M100, 'prerequisiteType': 'RECOMMENDED'}, token=A, expect=201, label='math recommended')
call('POST', f'/courses/{C202}/prerequisites', {'prerequisiteCourseId': C101, 'minGrade': 'B'}, token=A, expect=201, label='CS202 needs CS101 with B')
call('POST', f'/courses/{C202}/prerequisites', {'prerequisiteCourseId': C101, 'minGrade': 'C'}, token=A, expect=409, label='duplicate with another grade')
check(len(call('GET', f'/courses/{C201}/prerequisites', token=S1, expect=200, label='students read prerequisites')) == 3, 'three prerequisites')
ck = call('GET', f'/courses/{C201}/prerequisites/check', token=S1, expect=200, label='student checks own standing')
check(ck['met'] is False and {i['status'] for i in ck['items']} == {'MISSING'} and next(i for i in ck['items'] if i['prerequisiteType'] == 'RECOMMENDED')['met'],
      'missing; recommended never blocks')
call('GET', f'/courses/{C201}/prerequisites/check?userId={s2}', token=S1, expect=403, label="cannot check someone else's")
code = call('PUT', f'/courses/{C201}/self-enrollment', {'enabled': True}, token=TOM, expect=200, label='self-enrollment on')['selfEnrollmentCode']
pe = call('POST', '/enrollments/self', {'code': code}, token=S1, expect=409, label='self-enrollment blocked by prerequisites')
check(pe.get('code') == 'PREREQUISITES_NOT_MET' and 'CS101' in pe.get('message', '') and 'CS201L as a corequisite' in pe.get('message', ''), 'tells what is missing')
call('POST', f'/sections/{S201}/enrollments', {'userId': s1}, token=TOM, expect=409, label='teacher enrollment also checked')
call('POST', f'/sections/{S201}/enrollments', {'userId': s1, 'overridePrerequisites': True}, token=TOM, expect=403, label='teachers cannot override')
call('POST', f'/sections/{S201}/enrollments', {'userId': s1, 'overridePrerequisites': True}, token=A, expect=201, label='admin overrides')
call('POST', f'/sections/{S201}/enrollments', {'userId': s3, 'enrollmentType': 'OBSERVER', 'observedUserId': s1}, token=A, expect=None, label='(observers are not checked)')
# s2: completes CS101, takes the lab now
e = call('POST', f'/sections/{S101}/enrollments', {'userId': s2}, token=ADA, expect=201, label='s2 in CS101')
call('PATCH', f"/enrollments/{e['enrollmentId']}/status", {'status': 'COMPLETED'}, token=ADA, expect=200, label='s2 completes CS101')
call('POST', '/enrollments/self', {'code': code}, token=S2, expect=409, label='still needs the lab')
call('POST', f'/sections/{S201L}/enrollments', {'userId': s2}, token=TOM, expect=201, label='s2 in the lab')
ck2 = call('GET', f'/courses/{C201}/prerequisites/check?userId={s2}', token=TOM, expect=200, label='teacher checks a student')
check(ck2['met'] and {i['courseCode']: i['status'] for i in ck2['items']} == {'CS101': 'COMPLETED', 'CS201L': 'ENROLLED', 'MATH100': 'MISSING'}, 'met')
call('POST', '/enrollments/self', {'code': code}, token=S2, expect=201, label='s2 self-enrolls in CS201')
# minimum grade (final grades are written directly: the gradebook API comes later)
g = call('GET', f'/courses/{C202}/prerequisites/check?userId={s2}', token=A, expect=200, label='grade check without a final grade')
check(g['items'][0]['status'] == 'GRADE_UNKNOWN' and not g['met'], 'GRADE_UNKNOWN blocks')
sql(f"INSERT INTO lms_core.gradebook (tenant_id, section_id, user_id, final_grade_letter) VALUES ('{TID}', '{S101}', '{s2}', 'C+')")
g = call('GET', f'/courses/{C202}/prerequisites/check?userId={s2}', token=A, expect=200, label='C+ against B')
check(g['items'][0]['status'] == 'GRADE_TOO_LOW' and g['items'][0]['grade'] == 'C+', 'grade too low')
sql(f"UPDATE lms_core.gradebook SET override_letter = 'B+' WHERE section_id = '{S101}' AND user_id = '{s2}'")
g = call('GET', f'/courses/{C202}/prerequisites/check?userId={s2}', token=A, expect=200, label='override B+ against B')
check(g['met'] and g['items'][0]['grade'] == 'B+', 'override letter counts')
call('PATCH', f'/sections/{S201}', {'capacity': 2}, token=A, expect=200, label='CS201 now full (s1, s2)')
wl = call('POST', f'/sections/{S201}/waitlist', {}, token=S4, expect=409, label='waitlist join checked too')
check(wl.get('code') == 'PREREQUISITES_NOT_MET', 'waitlist: PREREQUISITES_NOT_MET')
call('DELETE', f"/courses/{C201}/prerequisites/{rec['prerequisiteId']}", token=A, expect=204, label='remove a prerequisite')
call('GET', f'/courses/{C201}/prerequisites', token=X, expect=404, label='other institution cannot see the course')

# ------------------------------------------------------------------ meeting schedules
call('POST', f'/sections/{S101}/schedule', {'meetingType': 'IN_PERSON', 'dayOfWeek': 'MON', 'timeStart': '09:00', 'timeEnd': '10:15'}, token=A,
     expect=400, label='in-person needs a room')
call('POST', f'/sections/{S101}/schedule', {'meetingType': 'ASYNCHRONOUS', 'dayOfWeek': 'MON'}, token=A, expect=400, label='asynchronous has no time')
call('POST', f'/sections/{S101}/schedule', {'meetingType': 'ONLINE', 'dayOfWeek': 'MON', 'timeStart': '10:00', 'timeEnd': '09:00'}, token=A,
     expect=400, label='end after start')
call('POST', f'/sections/{S101}/schedule', {'meetingType': 'ONLINE', 'dayOfWeek': 'MON', 'timeStart': '09:00', 'timeEnd': '10:00',
     'instructorAssignmentId': co['courseInstructorId']}, token=A, expect=400, label='instructor must be staff of this section')
call('POST', f'/sections/{S101}/schedule', {'meetingType': 'ONLINE', 'dayOfWeek': 'MON', 'timeStart': '09:00', 'timeEnd': '10:00'}, token=ADA,
     expect=403, label='teachers cannot set meeting times')
m1 = call('POST', f'/sections/{S101}/schedule', {'meetingType': 'IN_PERSON', 'dayOfWeek': 'MON', 'timeStart': '09:00', 'timeEnd': '10:15',
          'location': 'Hall 101', 'instructorAssignmentId': ADA_STAFF}, token=A, expect=201, label='CS101 Monday 9:00 in Hall 101')
check(m1['instructorUserId'] == ada and m1['instructorName'], 'instructor named')
rc = call('POST', f'/sections/{S201}/schedule', {'meetingType': 'IN_PERSON', 'dayOfWeek': 'MON', 'timeStart': '09:30', 'timeEnd': '10:30',
          'location': 'hall 101'}, token=A, expect=409, label='room double-booked')
check(rc.get('code') == 'ROOM_CONFLICT' and 'CS101' in rc.get('message', ''), 'ROOM_CONFLICT names the other section')
ic = call('POST', f'/sections/{S202}/schedule', {'meetingType': 'HYBRID', 'dayOfWeek': 'MON', 'timeStart': '10:00', 'timeEnd': '11:00',
          'location': 'Hall 202', 'instructorAssignmentId': co['courseInstructorId']}, token=A, expect=409, label='instructor double-booked')
check(ic.get('code') == 'INSTRUCTOR_CONFLICT', 'INSTRUCTOR_CONFLICT')
call('POST', f'/sections/{S201}/schedule', {'meetingType': 'IN_PERSON', 'dayOfWeek': 'MON', 'timeStart': '10:15', 'timeEnd': '11:30',
     'location': 'Hall 101'}, token=A, expect=201, label='back-to-back in the same room is fine')
lab = call('POST', f'/sections/{S201L}/schedule', {'meetingType': 'IN_PERSON', 'dayOfWeek': 'WED', 'timeStart': '14:00', 'timeEnd': '16:00',
           'location': 'Lab 3'}, token=A, expect=201, label='lab on Wednesday')
call('POST', f'/sections/{S201L}/schedule', {'meetingType': 'ASYNCHRONOUS'}, token=A, expect=201, label='asynchronous part')
lab2 = call('PUT', f"/sections/{S201L}/schedule/{lab['scheduleId']}", {'meetingType': 'IN_PERSON', 'dayOfWeek': 'TUE', 'timeStart': '14:00',
            'timeEnd': '16:00', 'location': 'Lab 3'}, token=A, expect=200, label='move the lab to Tuesday')
check(lab2['dayOfWeek'] == 'TUE', 'replaced')
call('GET', f'/sections/{S201L}/schedule', token=S2, expect=200, label='enrolled student sees the meetings')
call('GET', f'/sections/{S201L}/schedule', token=S4, expect=404, label='others do not')
my = call('GET', '/me/schedule', token=S2, expect=200, label="student's timetable")
check([(m['courseCode'], m.get('dayOfWeek')) for m in my] == [('CS201', 'MON'), ('CS201L', 'TUE'), ('CS201L', None)], 'ordered by day and time; completed course left out')
mine = call('GET', '/me/schedule', token=ADA, expect=200, label="teacher's timetable")
check(any(m['courseCode'] == 'CS101' and m['role'] == 'PRIMARY' for m in mine), 'teaching shown with the role')
check(call('GET', f"/me/schedule?termId={'00000000-0000-0000-0000-000000000000'}", token=S2, expect=200) == [], 'term filter')
call('DELETE', f"/sections/{S201L}/schedule/{lab['scheduleId']}", token=A, expect=204, label='delete a meeting')

# ------------------------------------------------------------------ learning outcomes
call('POST', f"/org-units/{dept['orgUnitId']}/outcomes", {'outcomeDescription': 'Design correct algorithms'}, token=ADA, expect=403,
     label='teachers cannot write program outcomes')
po = call('POST', f"/org-units/{dept['orgUnitId']}/outcomes", {'outcomeDescription': 'Design correct and efficient algorithms', 'bloomLevel': 'CREATE',
          'outcomeType': 'COGNITIVE', 'masteryPoints': 3}, token=A, expect=201, label='department outcome')
check(po['owner'] == 'ORG_UNIT' and po['outcomeNumber'] == 1 and po['calculationMethod'] == 'DECAYING_AVERAGE' and po['calculationInt'] == 65, 'defaults')
call('POST', f"/org-units/{college['orgUnitId']}/outcomes", {'outcomeDescription': 'Communicate technical ideas clearly'}, token=A, expect=201, label='college outcome')
call('POST', f'/courses/{C101}/outcomes', {'outcomeDescription': 'too short'}, token=ADA, expect=400, label='description at least 10 characters')
call('POST', f'/courses/{C101}/outcomes', {'outcomeDescription': 'Write loops and functions', 'calculationMethod': 'N_MASTERY', 'calculationInt': 20},
     token=ADA, expect=400, label='N_MASTERY count 1-10')
call('POST', f'/courses/{C101}/outcomes', {'outcomeDescription': 'Write loops and functions', 'calculationMethod': 'LATEST', 'calculationInt': 3},
     token=ADA, expect=400, label='LATEST takes no number')
o1 = call('POST', f'/courses/{C101}/outcomes', {'outcomeDescription': 'Write loops and functions in Python', 'calculationMethod': 'HIGHEST'},
          token=ADA, expect=201, label='teacher adds a course outcome')
o2 = call('POST', f'/courses/{C101}/outcomes', {'outcomeDescription': 'Debug programs systematically'}, token=ADA, expect=201, label='second outcome')
check(o1['outcomeNumber'] == 1 and o2['outcomeNumber'] == 2 and o1.get('calculationInt') is None, 'numbered 1, 2')
call('POST', f'/courses/{C101}/outcomes', {'outcomeNumber': 2, 'outcomeDescription': 'Duplicate number outcome'}, token=ADA, expect=409, label='number taken')
call('POST', f'/courses/{C101}/outcomes', {'outcomeDescription': 'Students cannot add outcomes'}, token=S2, expect=403, label='students cannot')
up = call('PATCH', f"/outcomes/{o1['outcomeId']}", {'calculationMethod': 'N_MASTERY'}, token=ADA, expect=200, label='switch to N_MASTERY')
check(up['calculationInt'] == 5, 'N_MASTERY default 5')
call('PATCH', f"/outcomes/{o2['outcomeId']}", {'isActive': False}, token=ADA, expect=200, label='deactivate outcome 2')
inh = call('GET', f'/courses/{C101}/outcomes?includeInherited=true', token=ADA, expect=200, label='with inherited outcomes')
check([o['owner'] for o in inh] == ['ORG_UNIT', 'ORG_UNIT', 'COURSE'] and len(inh) == 3, 'program outcomes first; inactive hidden')
check(len(call('GET', f'/courses/{C101}/outcomes?includeInactive=true', token=ADA, expect=200)) == 2, 'teacher can include inactive')
e3 = call('POST', f'/sections/{S101}/enrollments', {'userId': s3}, token=ADA, expect=201, label='s3 in CS101')
check(len(call('GET', f'/courses/{C101}/outcomes?includeInactive=true', token=S3, expect=200, label='student list')) == 1, 'students never see inactive')
call('GET', f"/outcomes/{o2['outcomeId']}", token=S3, expect=404, label='inactive outcome hidden from students')
call('GET', f"/outcomes/{o2['outcomeId']}", token=X, expect=404, label='other institution')
call('DELETE', f"/outcomes/{o2['outcomeId']}", token=ADA, expect=204, label='delete an unused outcome')
call('DELETE', f"/outcomes/{po['outcomeId']}", token=ADA, expect=403, label='teacher cannot delete program outcomes')

# ------------------------------------------------------------------ course navigation
nav = call('GET', f'/courses/{C101}/navigation', token=ADA, expect=200, label='default navigation')
check(len(nav) == 16 and nav[0]['toolKey'] == 'HOME' and nav[-1]['toolKey'] == 'SETTINGS' and not nav[0]['canHide'], 'defaults with Settings last')
for body, why in [([{'toolKey': 'HOME', 'isHidden': True}], 'Home cannot be hidden'), ([{'toolKey': 'TELEPORT'}], 'unknown entry'),
                  ([{'toolKey': 'SETTINGS'}], 'Settings is fixed'), ([{'toolKey': 'PAGES'}, {'toolKey': 'pages'}], 'each entry once'),
                  ([{'toolKey': 'EXTERNAL_TOOL'}], 'external tools need an id')]:
    call('PUT', f'/courses/{C101}/navigation', {'items': body}, token=ADA, expect=400, label=why)
call('PUT', f'/courses/{C101}/navigation', {'items': [{'toolKey': 'MODULES'}]}, token=TOM, expect=403, label='other teacher cannot')
call('PUT', f'/courses/{C101}/navigation', {'items': [{'toolKey': 'MODULES'}]}, token=S3, expect=403, label='students cannot')
nv = call('PUT', f'/courses/{C101}/navigation', {'items': [{'toolKey': 'modules'}, {'toolKey': 'HOME'}, {'toolKey': 'ASSIGNMENTS', 'label': 'Coursework'},
          {'toolKey': 'DISCUSSIONS', 'isHidden': True}]}, token=ADA, expect=200, label='teacher customises the menu')
check([n['toolKey'] for n in nv[:4]] == ['MODULES', 'HOME', 'ASSIGNMENTS', 'DISCUSSIONS'] and nv[2]['label'] == 'Coursework'
      and nv[2]['defaultLabel'] == 'Assignments' and nv[3]['isHidden'] and len(nv) == 16, 'order, label, hidden; the rest keep their place')
sn = call('GET', f'/courses/{C101}/navigation', token=S3, expect=200, label='student menu')
check(sn[0]['toolKey'] == 'MODULES' and 'DISCUSSIONS' not in [n['toolKey'] for n in sn] and 'SETTINGS' not in [n['toolKey'] for n in sn], 'hidden and Settings left out')
call('GET', f'/courses/{C101}/navigation', token=S4, expect=404, label='not in the course')
check(call('PUT', f'/courses/{C101}/navigation', {'items': []}, token=ADA, expect=200, label='reset')[0]['toolKey'] == 'HOME', 'defaults again')

# ------------------------------------------------------------------ blueprint courses
BP, BPS = course('BP100', 'Intro Blueprint', ada, courseKind='BLUEPRINT')
BP2, _ = course('BP200', 'Other Blueprint', ada, courseKind='BLUEPRINT')
CH1, CH1S = course('CH101', 'Intro (Section A)', tom)
CH2, CH2S = course('CH102', 'Intro (Section B)', tom)
call('POST', f'/courses/{C101}/blueprint/associations', {'courseIds': [CH1]}, token=A, expect=400, label='only blueprints have associations')
call('POST', f'/courses/{BP}/blueprint/associations', {'courseIds': [BP2]}, token=A, expect=400, label='a blueprint cannot be associated')
call('POST', f'/courses/{BP}/blueprint/associations', {'courseIds': [CH1]}, token=ADA, expect=403, label='teachers cannot associate')
asc = call('POST', f'/courses/{BP}/blueprint/associations', {'courseIds': [CH1, CH2]}, token=A, expect=201, label='associate two courses')
check(len(asc) == 2 and all(a['isActive'] and a.get('dateLastSynced') is None for a in asc), 'associated, never synced')
hb = call('POST', f'/courses/{BP2}/blueprint/associations', {'courseIds': [CH1]}, token=A, expect=409, label='one blueprint per course')
check(hb.get('code') == 'HAS_BLUEPRINT', 'HAS_BLUEPRINT')
check(len(call('GET', f'/courses/{BP}/blueprint/associations', token=ADA, expect=200, label='blueprint teacher lists them')) == 2, 'listed')
call('GET', f'/courses/{BP}/blueprint/associations', token=TOM, expect=403, label='associated-course teacher cannot list')
# blueprint content
bm1 = call('POST', f'/courses/{BP}/modules', {'moduleTitle': 'Unit 1 Basics', 'moduleType': 'UNIT'}, token=ADA, expect=201, label='blueprint module 1')
bm2 = call('POST', f'/courses/{BP}/modules', {'moduleTitle': 'Unit 2 Practice'}, token=ADA, expect=201, label='blueprint module 2')
call('PUT', f"/modules/{bm2['moduleId']}/prerequisites", {'prerequisiteModuleIds': [bm1['moduleId']]}, token=ADA, expect=200, label='unit 2 after unit 1')
bpg = call('POST', f'/courses/{BP}/content', {'typeCode': 'PAGE', 'contentTitle': 'Reading One', 'contentBody': '<p>v1</p>', 'moduleId': bm1['moduleId']},
           token=ADA, expect=201, label='blueprint page')
bp3 = call('POST', f'/courses/{BP}/content', {'typeCode': 'PAGE', 'contentTitle': 'Temporary Page', 'contentBody': '<p>tmp</p>'}, token=ADA, expect=201, label='page to delete later')
bas = call('POST', f'/courses/{BP}/assignments', {'assignmentTitle': 'Essay One', 'assignmentType': 'TEXT_ENTRY', 'pointsPossible': 10,
           'dateDue': D(10), 'moduleId': bm1['moduleId']}, token=ADA, expect=201, label='blueprint assignment')
bas2 = call('POST', f'/courses/{BP}/assignments', {'assignmentTitle': 'Quick Check', 'assignmentType': 'TEXT_ENTRY', 'pointsPossible': 5,
            'dateDue': D(5)}, token=ADA, expect=201, label='second blueprint assignment')
for x in (bas, bas2):
    call('POST', f"/assignments/{x['assignmentId']}/publish", token=ADA, expect=200, label='publish blueprint assignment')
call('POST', f"/modules/{bm1['moduleId']}/requirements", {'contentId': bpg['contentId'], 'requirementType': 'VIEW'}, token=ADA, expect=201, label='requirement')
# locks
call('PUT', f'/courses/{BP}/blueprint/locks', {'objectType': 'CONTENT', 'objectId': bpg['contentId']}, token=TOM, expect=403, label='others cannot lock')
call('PUT', f'/courses/{BP}/blueprint/locks', {'objectType': 'CONTENT', 'objectId': o1['outcomeId']}, token=ADA, expect=400, label='item must be in the blueprint')
call('PUT', f'/courses/{BP}/blueprint/locks', {'objectType': 'FILE', 'objectId': bpg['contentId']}, token=ADA, expect=400, label='files cannot be locked yet')
lk = call('PUT', f'/courses/{BP}/blueprint/locks', {'objectType': 'ASSIGNMENT', 'objectId': bas['assignmentId'], 'lockPoints': True}, token=ADA,
          expect=200, label='lock assignment content and points')
check(lk['lockContent'] and lk['lockPoints'] and not lk['lockDueDates'], 'content locked by default')
call('PUT', f'/courses/{BP}/blueprint/locks', {'objectType': 'CONTENT', 'objectId': bpg['contentId']}, token=ADA, expect=200, label='lock page content')
tmp = call('PUT', f'/courses/{BP}/blueprint/locks', {'objectType': 'MODULE', 'objectId': bm2['moduleId'], 'lockContent': False, 'lockSettings': True},
           token=ADA, expect=200, label='lock module settings')
call('PUT', f'/courses/{BP}/blueprint/locks', {'objectType': 'MODULE', 'objectId': bm2['moduleId'], 'lockContent': False}, token=ADA, expect=204,
     label='every flag off removes the lock')
check(len(call('GET', f'/courses/{BP}/blueprint/locks', token=ADA, expect=200, label='list locks')) == 2, 'two locks')
# first sync
call('POST', f'/courses/{C101}/blueprint/syncs', {}, token=ADA, expect=400, label='only blueprints sync')
call('POST', f'/courses/{BP}/blueprint/syncs', {}, token=TOM, expect=403, label='only the blueprint teachers sync')
r1 = call('POST', f'/courses/{BP}/blueprint/syncs', {'syncComment': 'First push'}, token=ADA, expect=201, label='sync 1')
check(r1['runStatus'] == 'COMPLETED' and r1['coursesSynced'] == 2 and r1['changes']['courses'][CH1]['created'] == 6 and 'copies' not in r1['changes'],
      f"sync 1 created 6 items per course ({r1.get('changes')})")
mods = call('GET', f'/courses/{CH1}/modules', token=TOM, expect=200, label='associated course modules')
titles = [m['module']['moduleTitle'] for m in mods]
check(titles == ['Unit 1 Basics', 'Unit 2 Practice'], f'modules copied in order ({titles})')
u1 = mods[0]['module']
check(len(u1['requirements']) == 1 and mods[1]['module']['prerequisiteModuleIds'] == [u1['moduleId']], 'requirement and prerequisite point at the copies')
ch_items = {a['assignmentTitle']: a for a in alist(CH1, TOM, 'copied assignments')}
cA = ch_items['Essay One']
check(float(cA['pointsPossible']) == 10 and cA['isPublished'] and cA['moduleId'] == u1['moduleId'], 'assignment copied into the copied module')
pages = {c['contentTitle']: c for c in call('GET', f'/courses/{CH1}/content', token=TOM, expect=200, label='copied pages')}
cP = pages['Reading One']
st1 = call('GET', f'/courses/{CH1}/blueprint-status', token=TOM, expect=200, label="associated course's blueprint status")
check(st1['blueprintCourseCode'] == 'BP100' and st1['dateLastSynced'] and {l['copyId'] for l in st1['locks']} == {cA['assignmentId'], cP['contentId']},
      'locks shown with the course copies')
call('GET', f'/courses/{C101}/blueprint-status', token=ADA, expect=404, label='course without a blueprint')
# locks in the associated course
lt = call('PATCH', f"/assignments/{cA['assignmentId']}", {'assignmentTitle': 'My Essay'}, token=TOM, expect=409, label='locked title')
check(lt.get('code') == 'BLUEPRINT_LOCKED' and 'content' in lt.get('message', ''), 'BLUEPRINT_LOCKED')
call('PATCH', f"/assignments/{cA['assignmentId']}", {'pointsPossible': 50}, token=TOM, expect=409, label='locked points')
call('PATCH', f"/assignments/{cA['assignmentId']}", {'dateDue': D(12)}, token=TOM, expect=200, label='due date is not locked')
call('DELETE', f"/assignments/{cA['assignmentId']}", token=TOM, expect=409, label='locked item cannot be deleted')
call('PATCH', f"/content/{cP['contentId']}", {'contentBody': '<p>mine</p>'}, token=TOM, expect=409, label='locked page body')
call('PATCH', f"/content/{cP['contentId']}", {'dateStart': D(1)}, token=TOM, expect=200, label='page dates are not locked')
call('PATCH', f"/modules/{u1['moduleId']}", {'moduleTitle': 'Unit 1 (our version)'}, token=TOM, expect=200, label='module not locked')
own = call('POST', f'/courses/{CH1}/content', {'typeCode': 'PAGE', 'contentTitle': 'Our Own Page', 'contentBody': '<p>x</p>'}, token=TOM, expect=201, label="course's own page")
call('PATCH', f"/content/{own['contentId']}", {'contentBody': '<p>y</p>'}, token=TOM, expect=200, label='own items are never locked')
call('PATCH', f"/content/{bpg['contentId']}", {'contentBody': '<p>v2</p>', 'contentTitle': 'Reading One (revised)'}, token=ADA, expect=200,
     label='blueprint teacher edits the blueprint freely')
# a student hands in the copy of Quick Check in CH2
call('POST', f'/courses/{CH2}/publish', token=A, expect=200, label='publish CH102')
call('POST', f'/sections/{CH2S}/enrollments', {'userId': s4}, token=TOM, expect=201, label='s4 in CH102')
qc2 = next(a for a in alist(CH2, TOM) if a['assignmentTitle'] == 'Quick Check')
call('POST', f"/assignments/{qc2['assignmentId']}/submissions", {'submissionText': '<p>done</p>'}, token=S4, expect=201, label='s4 submits the copy')
# blueprint changes, second sync
call('PATCH', f"/assignments/{bas['assignmentId']}", {'assignmentTitle': 'Essay One (v2)', 'pointsPossible': 20, 'dateDue': D(14)}, token=ADA, expect=200, label='change essay')
call('PATCH', f"/modules/{bm1['moduleId']}", {'moduleTitle': 'Unit 1 Foundations'}, token=ADA, expect=200, label='rename unit 1')
call('POST', f'/courses/{BP}/content', {'typeCode': 'PAGE', 'contentTitle': 'Reading Two', 'contentBody': '<p>new</p>', 'moduleId': bm2['moduleId']},
     token=ADA, expect=201, label='new blueprint page')
call('DELETE', f"/content/{bp3['contentId']}", token=ADA, expect=204, label='delete the temporary page')
call('DELETE', f"/assignments/{bas2['assignmentId']}", token=ADA, expect=204, label='delete Quick Check from the blueprint')
r2 = call('POST', f'/courses/{BP}/blueprint/syncs', {'syncComment': 'Second push'}, token=ADA, expect=201, label='sync 2')
ex = {(e['courseId'], e['title'], e['reason']) for e in r2['exceptions']}
check(r2['runStatus'] == 'COMPLETED_WITH_EXCEPTIONS', 'exceptions reported')
check((CH1, 'Essay One (v2)', 'CHANGED_IN_COURSE') in ex and (CH1, 'Unit 1 Foundations', 'CHANGED_IN_COURSE') in ex
      and (CH2, 'Quick Check', 'KEPT_HAS_SUBMISSIONS') in ex and not any(e[0] == CH2 and e[2] == 'CHANGED_IN_COURSE' for e in ex), f'exceptions ({sorted(ex)})')
a1 = call('GET', f"/assignments/{cA['assignmentId']}", token=TOM, expect=200, label='CH101 essay after sync')['assignment']
check(a1['assignmentTitle'] == 'Essay One (v2)' and float(a1['pointsPossible']) == 20 and a1['dateDue'] == D(12),
      'locked title and points overwritten; changed due date kept')
a2 = next(a for a in alist(CH2, TOM) if a['assignmentTitle'].startswith('Essay One'))
check(a2['assignmentTitle'] == 'Essay One (v2)' and a2['dateDue'] == D(14), 'unchanged course gets everything')
m1c = call('GET', f"/modules/{u1['moduleId']}", token=TOM)['module']
check(m1c['moduleTitle'] == 'Unit 1 (our version)', "unlocked module rename kept in CH101")
p1c = call('GET', f"/content/{cP['contentId']}", token=TOM, expect=200, label='CH101 page after sync')
check(p1c['contentTitle'] == 'Reading One (revised)' and p1c['contentBody'] == '<p>v2</p>' and p1c['dateStart'] == D(1),
      'locked page content overwritten; its unlocked date kept')
t1 = [c['contentTitle'] for c in call('GET', f'/courses/{CH1}/content', token=TOM)]
check('Reading Two' in t1 and 'Temporary Page' not in t1 and 'Our Own Page' in t1, 'new page added, deleted page removed, own page untouched')
t2 = [a['assignmentTitle'] for a in alist(CH2, TOM)]
t1a = [a['assignmentTitle'] for a in alist(CH1, TOM)]
check('Quick Check' in t2 and 'Quick Check' not in t1a, 'deleted assignment: kept where students handed in, removed elsewhere')
# settings
call('PATCH', f'/courses/{BP}', {'syllabusHtml': '<h1>Blueprint syllabus</h1>', 'homePageType': 'SYLLABUS'}, token=ADA, expect=200, label='blueprint settings')
call('PUT', f'/courses/{BP}/blueprint/locks', {'objectType': 'SETTINGS'}, token=ADA, expect=200, label='lock course settings')
r3 = call('POST', f'/courses/{BP}/blueprint/syncs', {'includeCourseSettings': True}, token=ADA, expect=201, label='sync 3 with settings')
check(r3['includeCourseSettings'] and r3['coursesSynced'] == 2, 'synced')
c2 = call('GET', f'/courses/{CH2}', token=TOM, expect=200, label='CH102 after settings sync')
check(c2['syllabusHtml'] == '<h1>Blueprint syllabus</h1>' and c2['homePageType'] == 'SYLLABUS', 'settings copied')
call('PATCH', f'/courses/{CH1}', {'syllabusHtml': '<p>ours</p>'}, token=TOM, expect=409, label='locked course settings')
call('PATCH', f'/courses/{CH1}', {'courseTitle': 'Intro (Section A, Mon/Wed)'}, token=TOM, expect=200, label='title is not a synced setting')
# history and kind changes
hist = call('GET', f'/courses/{BP}/blueprint/syncs', token=ADA, expect=200, label='sync history')
check([h.get('syncComment') for h in hist][1:] == ['Second push', 'First push'] and all('copies' not in (h.get('changes') or {}) for h in hist), 'newest first')
call('GET', f"/courses/{BP}/blueprint/syncs/{r1['syncRunId']}", token=ADA, expect=200, label='one run')
call('PATCH', f'/courses/{BP}', {'courseKind': 'STANDARD'}, token=A, expect=409, label='blueprint with courses keeps its kind')
call('PATCH', f'/courses/{CH1}', {'courseKind': 'BLUEPRINT'}, token=A, expect=409, label='associated course cannot become a blueprint')
call('GET', f'/courses/{BP}/blueprint/locks', token=X, expect=404, label='other institution')
# removing an association frees the copies
ch2a = next(a for a in call('GET', f'/courses/{BP}/blueprint/associations', token=ADA) if a['associatedCourseId'] == CH2)
call('DELETE', f"/courses/{BP}/blueprint/associations/{ch2a['blueprintAssociationId']}", token=A, expect=204, label='remove CH102')
call('PATCH', f"/assignments/{a2['assignmentId']}", {'assignmentTitle': 'Our Essay'}, token=TOM, expect=200, label='copies are unlocked after removal')
r4 = call('POST', f'/courses/{BP}/blueprint/syncs', {}, token=ADA, expect=201, label='sync 4')
check(r4['coursesSynced'] == 1, 'only the remaining course is synced')
check(next(a for a in alist(CH2, TOM) if a['assignmentId'] == a2['assignmentId'])['assignmentTitle'] == 'Our Essay',
      'removed course is left alone')

passed = sum(1 for r in results if r[0])
print(f'{passed}/{len(results)} checks passed')
for ok, label, status, exp in results:
    print(('PASS' if ok else 'FAIL'), f'{status!s:>3}', label)
sys.exit(0 if passed == len(results) else 1)
