"""End-to-end test for academic structure, courses, sections, staff, enrollment, waitlist and groups.

Needs a running backend whose database has the bootstrap platform owner (owner@lms-company.com / Owner-Pass-2026!).
Creates its own institutions (codes UNI and OTH), so it can run after scripts/api_smoke_test.py on the same database.
    python scripts/api_test_courses.py
"""
import json, os, sys, urllib.request

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


# ------------------------------------------------------------------ setup: two institutions, people
T = call('POST', '/auth/login', {'login': 'owner@lms-company.com', 'password': 'Owner-Pass-2026!'}, expect=200, label='platform owner login')['accessToken']
call('POST', '/platform/tenants', {'tenantName': 'Uni of Testing', 'tenantCode': 'UNI',
     'admin': {'username': 'admin.uni', 'email': 'admin@uni.edu', 'password': 'Admin-Pass-2026'}}, token=T, expect=201, label='create tenant UNI')
call('POST', '/platform/tenants', {'tenantName': 'Other College', 'tenantCode': 'OTH',
     'admin': {'username': 'admin.oth', 'email': 'admin@oth.edu', 'password': 'Admin-Pass-2026'}}, token=T, expect=201, label='create tenant OTH')
A = login('UNI', 'admin.uni', 'Admin-Pass-2026')
O = login('OTH', 'admin.oth', 'Admin-Pass-2026')


def mk(username, utype):
    u = call('POST', '/users', {'username': username, 'email': f'{username}@uni.edu', 'userType': utype, 'password': 'Welcome-2026-x',
             'profile': {'firstName': username.split('.')[1].title(), 'lastName': 'Test'}}, token=A, expect=201, label=f'create {username}')
    return u['user']['userId']


ada, tom = mk('prof.ada', 'INSTRUCTOR'), mk('prof.tom', 'INSTRUCTOR')
eve = mk('ta.eve', 'TEACHING_ASSISTANT')
s1, s2, s3, s4, s5 = (mk(f'stu.s{i}', 'STUDENT') for i in range(1, 6))
mom = mk('mom.gail', 'OBSERVER')
fay = mk('reg.fay', 'STAFF')

# ------------------------------------------------------------------ academic structure
college = call('POST', '/org-units', {'unitName': 'College of Science', 'unitCode': 'SCI', 'unitType': 'COLLEGE'}, token=A, expect=201, label='create college')
dept = call('POST', '/org-units', {'unitName': 'Computer Science', 'unitCode': 'CS', 'unitType': 'DEPARTMENT', 'parentOrgUnitId': college['orgUnitId']},
            token=A, expect=201, label='create department under college')
check(dept['isActive'] is True, 'database default is returned (isActive)')
call('PATCH', f"/org-units/{college['orgUnitId']}", {'parentOrgUnitId': dept['orgUnitId']}, token=A, expect=400, label='org unit cycle rejected')
fall = call('POST', '/terms', {'termName': 'Fall 2026', 'termCode': 'FA26', 'termType': 'SEMESTER', 'dateStart': '2026-08-24', 'dateEnd': '2026-12-18', 'isCurrent': True},
            token=A, expect=201, label='create current term')
spring = call('POST', '/terms', {'termName': 'Spring 2027', 'termCode': 'SP27', 'dateStart': '2027-01-11', 'dateEnd': '2027-05-07'}, token=A, expect=201, label='create term')
call('POST', '/terms', {'termName': 'Bad', 'termCode': 'BAD', 'dateStart': '2027-05-07', 'dateEnd': '2027-01-11'}, token=A, expect=400, label='term end before start rejected')
call('PATCH', f"/terms/{spring['termId']}", {'isCurrent': True}, token=A, expect=200, label='make another term current')
cur = call('GET', '/terms/current', token=A, expect=200, label='get current term')
check(cur['termId'] == spring['termId'], 'only one current term')
call('PATCH', f"/terms/{fall['termId']}", {'isCurrent': True}, token=A, expect=200, label='switch current back')
cat = call('POST', '/course-categories', {'categoryName': 'Computing', 'categoryCode': 'COMP'}, token=A, expect=201, label='create category')
call('POST', '/org-units', {'unitName': 'X', 'unitType': 'OTHER'}, token=login('UNI', 'prof.ada'), expect=403, label='instructor cannot manage org units')

# ------------------------------------------------------------------ courses
ADA = login('UNI', 'prof.ada')
c = call('POST', '/courses', {'courseCode': 'CS101', 'courseTitle': 'Intro to Programming', 'termId': fall['termId'], 'categoryId': cat['categoryId'],
         'orgUnitId': dept['orgUnitId'], 'creditHours': 3, 'defaultSectionCapacity': 2, 'instructorUserId': ada}, token=A, expect=201, label='create course + default section + primary instructor')
check(c['courseStatus'] == 'UNPUBLISHED' and c['courseKind'] == 'STANDARD', 'new course is UNPUBLISHED / STANDARD (DB defaults)')
C = c['courseId']
call('POST', '/courses', {'courseCode': 'CS101', 'courseTitle': 'Duplicate'}, token=A, expect=409, label='duplicate course code rejected')
call('POST', '/courses', {'courseCode': 'CS999', 'courseTitle': 'Not allowed'}, token=ADA, expect=403, label='instructor cannot create courses')
other = call('POST', '/courses', {'courseCode': 'MATH200', 'courseTitle': 'Linear Algebra', 'termId': fall['termId'], 'instructorUserId': tom}, token=A, expect=201, label='create second course')
secs = call('GET', f'/courses/{C}/sections', token=A, expect=200, label='list sections')
check(len(secs) == 1 and secs[0]['sectionNumber'] == '01' and secs[0]['capacity'] == 2 and secs[0]['enrolledCount'] == 0, 'default section 01 with capacity 2')
S = secs[0]['sectionId']
sec2 = call('POST', f'/courses/{C}/sections', {'sectionNumber': '02', 'capacity': 30, 'deliveryMode': 'ONLINE'}, token=A, expect=201, label='create second section')
check(sec2['termId'] == fall['termId'], 'section inherits the course term')
call('POST', f'/courses/{C}/sections', {'sectionNumber': '03'}, token=ADA, expect=403, label='teacher cannot add sections')

staff = call('GET', f'/sections/{S}/staff', token=ADA, expect=200, label='teacher lists staff')
check(len(staff) == 1 and staff[0]['role'] == 'PRIMARY' and staff[0]['isPrimaryInstructor'] and staff[0]['displayName'] == 'Ada Test', 'primary instructor with name')
call('POST', f'/sections/{S}/staff', {'userId': eve, 'role': 'TEACHING_ASSISTANT'}, token=A, expect=201, label='add TA')
call('POST', f'/sections/{S}/staff', {'userId': tom, 'role': 'PRIMARY'}, token=A, expect=409, label='second primary rejected (DB unique index)')
call('POST', f'/sections/{S}/staff', {'userId': mom, 'role': 'GRADER'}, token=A, expect=400, label='observer cannot be staff')
call('POST', f'/sections/{S}/staff', {'userId': s5, 'role': 'CO_INSTRUCTOR'}, token=A, expect=400, label='student account cannot co-teach')

# teacher edits + catalog fields
call('PATCH', f'/courses/{C}', {'syllabusHtml': '<p>Week 1: variables</p>', 'courseDescription': 'Learn to code'}, token=ADA, expect=200, label='teacher edits syllabus')
call('PATCH', f'/courses/{C}', {'creditHours': 4}, token=ADA, expect=403, label='teacher cannot change credits')
call('PATCH', f"/courses/{other['courseId']}", {'courseDescription': 'nope'}, token=ADA, expect=403, label='teacher cannot edit another course')
call('PATCH', f'/courses/{C}', {'creditHours': 4}, token=A, expect=200, label='admin changes credits')

# ------------------------------------------------------------------ enrollment + seats
E1 = call('POST', f'/sections/{S}/enrollments', {'userId': s1}, token=ADA, expect=201, label='teacher enrolls student 1')
check(E1['enrollmentStatus'] == 'ACTIVE' and E1['enrollmentType'] == 'STUDENT' and E1['courseId'] == C and E1['displayName'] == 'S1 Test', 'enrollment fields + name')
call('POST', f'/sections/{S}/enrollments', {'userId': s1}, token=ADA, expect=409, label='duplicate enrollment rejected')
call('POST', f'/sections/{S}/enrollments', {'userId': s2}, token=A, expect=201, label='admin enrolls student 2')
call('POST', f'/sections/{S}/enrollments', {'userId': s3}, token=ADA, expect=409, label='section full (capacity 2)')
call('POST', f'/sections/{S}/enrollments', {'userId': eve}, token=A, expect=409, label='staff member cannot also be a student')
call('POST', f'/sections/{S}/enrollments', {'userId': mom, 'enrollmentType': 'OBSERVER', 'observedUserId': s1}, token=ADA, expect=201, label='observer enrollment (does not take a seat)')
call('POST', f'/sections/{S}/enrollments', {'userId': s4, 'enrollmentType': 'OBSERVER'}, token=ADA, expect=400, label='observer enrollment needs observedUserId')
sec = call('GET', f'/sections/{S}', token=A, expect=200, label='get section')
check(sec['enrolledCount'] == 2, 'enrolled count = 2 seats (observer not counted)')
EVE = login('UNI', 'ta.eve')
roster = call('GET', f'/sections/{S}/enrollments', token=EVE, expect=200, label='TA sees roster')
check(len(roster) == 3, 'roster has 2 students + 1 observer')
call('POST', f'/sections/{S}/enrollments', {'userId': s4}, token=EVE, expect=403, label='TA cannot change roster')
TOM = login('UNI', 'prof.tom')
call('GET', f'/sections/{S}/enrollments', token=TOM, expect=403, label='teacher of another course cannot see roster')

# students and visibility
S1 = login('UNI', 'stu.s1')
call('GET', f'/courses/{C}', token=S1, expect=404, label='student cannot see unpublished course')
check(call('GET', '/courses/mine', token=S1, expect=200, label='student dashboard (unpublished)') == [], 'unpublished course hidden from student dashboard')
call('POST', f'/courses/{C}/publish', token=ADA, expect=200, label='teacher publishes course')
sv = call('GET', f'/courses/{C}', token=S1, expect=200, label='student sees published course')
check(sv.get('selfEnrollmentCode') is None, 'student view has no enrollment code')
mine = call('GET', '/courses/mine', token=S1, expect=200, label='student dashboard')
check(len(mine) == 1 and mine[0]['role'] == 'STUDENT' and mine[0]['sectionNumber'] == '01', 'dashboard shows CS101 as STUDENT')
amine = call('GET', '/courses/mine', token=ADA, expect=200, label='teacher dashboard')
check(len(amine) == 1 and amine[0]['role'] == 'PRIMARY', 'teacher dashboard shows CS101 as PRIMARY')
call('GET', f'/sections/{S}/enrollments', token=S1, expect=403, label='student cannot see roster')
call('GET', '/courses', token=S1, expect=403, label='student cannot search the catalog')
srch = call('GET', '/courses?q=cs1', token=A, expect=200, label='admin searches catalog')
check(srch['totalItems'] == 1, 'catalog search finds CS101')

# ------------------------------------------------------------------ waitlist
S3, S4 = login('UNI', 'stu.s3'), login('UNI', 'stu.s4')
w3 = call('POST', f'/sections/{S}/waitlist', {}, token=S3, expect=201, label='student 3 joins waitlist (auto-enroll)')
w4 = call('POST', f'/sections/{S}/waitlist', {'autoEnrollWhenAvailable': False}, token=S4, expect=201, label='student 4 joins waitlist (wants an offer)')
check(w3['position'] == 1 and w4['position'] == 2, 'waitlist positions 1, 2')
call('POST', f'/sections/{S}/waitlist', {}, token=S3, expect=409, label='cannot join waitlist twice')
call('POST', f"/sections/{sec2['sectionId']}/waitlist", {}, token=S3, expect=409, label='cannot waitlist a section with free seats')
check(call('GET', f'/sections/{S}', token=A)['waitlistCount'] == 2, 'waitlist count = 2')

call('PATCH', f"/enrollments/{E1['enrollmentId']}/status", {'status': 'DROPPED'}, token=S1, expect=403, label='student cannot drop a teacher-made enrollment')
call('PATCH', f"/enrollments/{E1['enrollmentId']}/status", {'status': 'WITHDRAWN'}, token=ADA, expect=200, label='teacher withdraws student 1')
st3 = call('GET', '/waitlist/mine', token=S3, expect=200, label='student 3 waitlist')
check(st3[0]['waitlistStatus'] == 'ENROLLED', 'student 3 auto-enrolled from waitlist')
check(any(m['role'] == 'STUDENT' for m in call('GET', '/courses/mine', token=S3, expect=200, label='student 3 dashboard')), 'student 3 now sees the course')
check(call('GET', f'/sections/{S}', token=A)['enrolledCount'] == 2, 'seat refilled: enrolled count back to 2')
w1 = call('GET', f"/enrollments/{E1['enrollmentId']}", token=S1, expect=200, label='student 1 reads own enrollment')
check(w1['enrollmentStatus'] == 'WITHDRAWN' and w1['enrollmentType'] == 'WITHDRAWN_STUDENT' and w1['dateWithdrawn'], 'withdrawal recorded')

call('PATCH', f'/sections/{S}', {'capacity': 1}, token=A, expect=409, label='capacity below enrollment rejected')
call('PATCH', f'/sections/{S}', {'capacity': 3}, token=A, expect=200, label='raise capacity to 3')
st4 = call('GET', '/waitlist/mine', token=S4, expect=200, label='student 4 waitlist')
check(st4[0]['waitlistStatus'] == 'OFFERED' and st4[0]['dateOffered'], 'student 4 got an offer')
call('POST', f'/sections/{S}/enrollments', {'userId': s5}, token=A, expect=409, label='offered seat is held for student 4')
e4 = call('POST', f"/waitlist/{st4[0]['waitlistId']}/accept", token=S4, expect=200, label='student 4 accepts offer')
check(e4['enrollmentStatus'] == 'ACTIVE', 'student 4 enrolled after accepting')
check(call('GET', f'/sections/{S}', token=A)['enrolledCount'] == 3, 'enrolled count = 3')
call('POST', f"/waitlist/{st4[0]['waitlistId']}/accept", token=S4, expect=409, label='offer cannot be accepted twice')

# ------------------------------------------------------------------ self-enrollment by code
se = call('PUT', f"/courses/{other['courseId']}/self-enrollment", {'enabled': True}, token=TOM, expect=200, label='teacher turns on self-enrollment')
code = se['selfEnrollmentCode']
check(code and len(code) == 8, 'self-enrollment code issued')
S5 = login('UNI', 'stu.s5')
call('POST', '/enrollments/self', {'code': code.lower()}, token=S5, expect=404, label='self-enroll in unpublished course refused')
call('POST', f"/courses/{other['courseId']}/publish", token=TOM, expect=200, label='publish MATH200')
se5 = call('POST', '/enrollments/self', {'code': code.lower()}, token=S5, expect=201, label='student self-enrolls with code')
check(se5['enrollmentSource'] == 'SELF', 'enrollment source SELF')
call('POST', '/enrollments/self', {'code': code}, token=S5, expect=409, label='self-enroll twice rejected')
call('POST', '/enrollments/self', {'code': 'NOPE1234'}, token=S5, expect=404, label='wrong code')
call('POST', '/enrollments/self', {'code': code}, token=O, expect=404, label='code does not work in another institution')
call('PATCH', f"/enrollments/{se5['enrollmentId']}/status", {'status': 'DROPPED'}, token=S5, expect=200, label='student drops own self-enrollment')

# ------------------------------------------------------------------ groups
gs = call('POST', f'/courses/{C}/group-sets', {'setName': 'Project Teams', 'allowSelfSignup': True, 'maxMembersPerGroup': 2}, token=ADA, expect=201, label='teacher creates group set')
check(gs['restrictToSection'] is False and gs['autoAssignLeader'] is False, 'group set defaults')
g1 = call('POST', f"/group-sets/{gs['groupSetId']}/groups", {'groupName': 'Team Alpha', 'sectionId': S}, token=ADA, expect=201, label='create group 1')
g2 = call('POST', f"/group-sets/{gs['groupSetId']}/groups", {'groupName': 'Team Beta', 'sectionId': S}, token=ADA, expect=201, label='create group 2')
check(g1['maxMembers'] == 2 and g1['currentMembers'] == 0, 'group takes the set limit')
call('POST', f"/group-sets/{gs['groupSetId']}/groups", {'groupName': 'Wrong', 'sectionId': call('GET', f"/courses/{other['courseId']}/sections", token=A)[0]['sectionId']},
     token=ADA, expect=400, label='group section must belong to the course')
call('POST', f"/groups/{g1['groupId']}/members", None, token=S3, expect=201, label='student 3 joins group 1 (self signup)')
call('POST', f"/groups/{g2['groupId']}/members", None, token=S3, expect=409, label='one group per set')
call('POST', f"/groups/{g1['groupId']}/members", {'userId': s4}, token=ADA, expect=201, label='teacher adds student 4')
S2 = login('UNI', 'stu.s2')
call('POST', f"/groups/{g1['groupId']}/members", None, token=S2, expect=409, label='group full')
call('POST', f"/groups/{g2['groupId']}/members", None, token=S5, expect=400, label='not-enrolled student cannot join')
mem = call('GET', f"/groups/{g1['groupId']}/members", token=S2, expect=200, label='classmate sees group members')
check(len(mem) == 2 and {m['username'] for m in mem} == {'stu.s3', 'stu.s4'}, 'group 1 members')
call('DELETE', f"/groups/{g1['groupId']}/members/{s3}", token=S3, expect=204, label='student 3 leaves group 1')
call('POST', f"/groups/{g2['groupId']}/members", None, token=S3, expect=201, label='student 3 joins group 2')
check(call('GET', f"/group-sets/{gs['groupSetId']}/groups", token=ADA)[0]['currentMembers'] == 1, 'member counts kept')

# ------------------------------------------------------------------ cross-listing, life cycle, isolation
x = call('POST', f"/sections/{sec2['sectionId']}/crosslist", {'targetCourseId': other['courseId']}, token=A, expect=200, label='cross-list section 02 into MATH200')
check(x['courseId'] == other['courseId'] and x['crosslistedFromCourseId'] == C, 'section moved, origin remembered')
call('POST', f"/sections/{sec2['sectionId']}/uncrosslist", token=A, expect=200, label='un-cross-list')
call('POST', f"/sections/{sec2['sectionId']}/crosslist", {'targetCourseId': other['courseId']}, token=ADA, expect=403, label='teacher cannot cross-list')

call('GET', f'/courses/{C}', token=O, expect=404, label='other institution cannot see the course')
call('GET', f'/sections/{S}/enrollments', token=O, expect=404, label='other institution cannot see the roster')
call('POST', f'/sections/{S}/enrollments', {'userId': s5}, token=O, expect=404, label='other institution cannot enroll')

call('POST', f'/courses/{C}/archive', token=ADA, expect=403, label='teacher cannot archive')
call('POST', f'/courses/{C}/conclude', token=ADA, expect=200, label='teacher concludes course')
call('POST', f'/sections/{S}/enrollments', {'userId': s5}, token=A, expect=409, label='no enrollment into a concluded course')
call('POST', f'/courses/{C}/archive', token=A, expect=200, label='admin archives course')
check(call('GET', '/courses/mine', token=S3, expect=200, label='dashboard after archive') == [], 'archived course leaves the dashboard')

# role-based admin: registrar with COURSE_MANAGE + ENROLLMENT_MANAGE
role = call('POST', '/roles', {'roleName': 'Registrar', 'permissions': ['COURSE_MANAGE', 'ENROLLMENT_MANAGE', 'COURSE_READ']}, token=A, expect=201, label='registrar role with course permissions')
call('POST', f'/users/{fay}/roles', {'roleId': role['roleId']}, token=A, expect=201, label='assign registrar role')
FAY = login('UNI', 'reg.fay')
call('POST', '/courses', {'courseCode': 'BIO110', 'courseTitle': 'Biology'}, token=FAY, expect=201, label='registrar creates a course')
call('GET', '/courses', token=FAY, expect=200, label='registrar searches catalog')

# ------------------------------------------------------------------ concurrency: 10 students race for 3 seats
import threading
race = call('POST', '/courses', {'courseCode': 'RACE1', 'courseTitle': 'Popular Seminar', 'defaultSectionCapacity': 3}, token=A, expect=201, label='create 3-seat course')
call('POST', f"/courses/{race['courseId']}/publish", token=A, expect=200, label='publish 3-seat course')
rcode = call('PUT', f"/courses/{race['courseId']}/self-enrollment", {'enabled': True}, token=A, expect=200, label='self-enrollment on')['selfEnrollmentCode']
racers = []
for i in range(10):
    mk(f'stu.race{i}', 'STUDENT')
    racers.append(login('UNI', f'stu.race{i}'))
statuses, lock = [], threading.Lock()
def go(tok):
    r = call('POST', '/enrollments/self', {'code': rcode}, token=tok, label='race self-enroll')
    with lock:
        statuses.append(results[-1][2])
results_before = len(results)
threads = [threading.Thread(target=go, args=(t,)) for t in racers]
for t in threads: t.start()
for t in threads: t.join()
del results[results_before:]                       # individual race calls are not checks by themselves
check(statuses.count(201) == 3 and statuses.count(409) == 7, f'exactly 3 of 10 concurrent self-enrollments win ({sorted(statuses)})')
rsec = call('GET', f"/courses/{race['courseId']}/sections", token=A, expect=200, label='race section')[0]
check(rsec['enrolledCount'] == 3, 'seat counter = 3 after the race')

passed = sum(1 for r in results if r[0])
print(f'{passed}/{len(results)} checks passed')
for ok, label, status, exp in results:
    print(('PASS' if ok else 'FAIL'), f'{status!s:>3}', label)
sys.exit(0 if passed == len(results) else 1)
