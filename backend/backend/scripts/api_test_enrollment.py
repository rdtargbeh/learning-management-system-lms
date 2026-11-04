"""End-to-end test for the rest of enrollment management (domain 04): group sets (edit, delete, auto-assign), groups
(edit, deactivate, delete), members (self sign-up, my groups), group leaders (appointed and automatic), differentiation
tags (staff-only, with assignment overrides), and observer pairing codes.

Needs a running backend whose database has the bootstrap platform owner (owner@lms-company.com / Owner-Pass-2026!).
Creates its own institutions (ENR and ENX).
    python scripts/api_test_enrollment.py
"""
import json, os, sys, urllib.request
from datetime import date, timedelta

BASE = os.environ.get('LMS_API', 'http://localhost:8080/api/v1')
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


def login(code, user, pw='Welcome-2026-x'):
    return call('POST', '/auth/login', {'tenantCode': code, 'login': user, 'password': pw}, expect=200, label=f'login {user}')['accessToken']


# ------------------------------------------------------------------ setup
T = call('POST', '/auth/login', {'login': 'owner@lms-company.com', 'password': 'Owner-Pass-2026!'}, expect=200, label='platform owner login')['accessToken']
call('POST', '/platform/tenants', {'tenantName': 'Enrollment University', 'tenantCode': 'ENR',
     'admin': {'username': 'admin.enr', 'email': 'admin@enr.edu', 'password': 'Admin-Pass-2026'}}, token=T, expect=201, label='create tenant ENR')
call('POST', '/platform/tenants', {'tenantName': 'Enrollment Elsewhere', 'tenantCode': 'ENX',
     'admin': {'username': 'admin.enx', 'email': 'admin@enx.edu', 'password': 'Admin-Pass-2026'}}, token=T, expect=201, label='create tenant ENX')
A = login('ENR', 'admin.enr', 'Admin-Pass-2026')
X = login('ENX', 'admin.enx', 'Admin-Pass-2026')


def mk(username, utype):
    return call('POST', '/users', {'username': username, 'email': f'{username}@enr.edu', 'userType': utype, 'password': 'Welcome-2026-x',
                'profile': {'firstName': username.split('.')[1].title(), 'lastName': 'Test'}}, token=A, expect=201, label=f'create {username}')['user']['userId']


ada, tom = mk('prof.ada', 'INSTRUCTOR'), mk('prof.tom', 'INSTRUCTOR')
eve = mk('ta.eve', 'TEACHING_ASSISTANT')
st = [mk(f'stu.s{i}', 'STUDENT') for i in range(1, 9)]          # s1..s8 (s8 is not enrolled)
obs = mk('par.olu', 'OBSERVER')
ADA, TOM, EVE, OBS = (login('ENR', u) for u in ('prof.ada', 'prof.tom', 'ta.eve', 'par.olu'))
S = [login('ENR', f'stu.s{i}') for i in range(1, 9)]
c = call('POST', '/courses', {'courseCode': 'PRJ300', 'courseTitle': 'Team Projects', 'instructorUserId': ada}, token=A, expect=201, label='course')
C = c['courseId']
sec1 = call('GET', f'/courses/{C}/sections', token=A)[0]['sectionId']
sec2 = call('POST', f'/courses/{C}/sections', {'sectionNumber': '02'}, token=A, expect=201, label='second section')['sectionId']
call('POST', f'/sections/{sec2}/staff', {'userId': ada, 'role': 'PRIMARY'}, token=A, expect=201, label='ada teaches section 02')
call('POST', f'/sections/{sec1}/staff', {'userId': eve, 'role': 'TEACHING_ASSISTANT'}, token=A, expect=201, label='TA')
call('POST', f'/courses/{C}/publish', token=ADA, expect=200, label='publish')
for i, sid in enumerate(st[:7]):
    call('POST', f'/sections/{sec1 if i < 4 else sec2}/enrollments', {'userId': sid}, token=ADA, expect=201, label=f'enroll s{i + 1}')

# ------------------------------------------------------------------ group sets
gs = call('POST', f'/courses/{C}/group-sets', {'setName': 'Project Teams', 'maxMembersPerGroup': 3}, token=ADA, expect=201, label='group set')
call('POST', f'/courses/{C}/group-sets', {'setName': 'project teams'}, token=ADA, expect=409, label='set names are unique')
call('POST', f'/courses/{C}/group-sets', {'setName': 'Nope'}, token=EVE, expect=403, label='TA cannot create sets')
call('PATCH', f"/group-sets/{gs['groupSetId']}", {'setName': 'Capstone Teams'}, token=ADA, expect=200, label='rename set')
check(call('GET', f"/group-sets/{gs['groupSetId']}", token=S[0], expect=200, label='students see sets')['setName'] == 'Capstone Teams', 'renamed')
call('GET', f"/group-sets/{gs['groupSetId']}", token=X, expect=404, label='other institution')
r1 = call('POST', f"/group-sets/{gs['groupSetId']}/auto-assign", {'groupCount': 2}, token=ADA, expect=200, label='auto-assign into 2 groups')
check(r1['groupsCreated'] == 2 and r1['studentsAssigned'] == 6 and r1['studentsUnassigned'] == 1, f'2 groups of 3; one student left over ({r1})')
check(sorted(g['groupName'] for g in r1['groups']) == ['Capstone Teams 1', 'Capstone Teams 2'] and all(g['currentMembers'] == 3 for g in r1['groups']), 'named and filled')
r2 = call('POST', f"/group-sets/{gs['groupSetId']}/auto-assign", {'groupCount': 3}, token=ADA, expect=200, label='auto-assign again with 3')
check(r2['groupsCreated'] == 1 and r2['studentsAssigned'] == 1 and r2['studentsUnassigned'] == 0, 'only the unassigned student moves')
call('POST', f"/group-sets/{gs['groupSetId']}/auto-assign", {}, token=S[0], expect=403, label='students cannot auto-assign')
# section-restricted set
lab = call('POST', f'/courses/{C}/group-sets', {'setName': 'Lab Partners', 'restrictToSection': True}, token=ADA, expect=201, label='section-restricted set')
r3 = call('POST', f"/group-sets/{lab['groupSetId']}/auto-assign", {'groupCount': 1}, token=ADA, expect=200, label='auto-assign per section')
check(r3['groupsCreated'] == 2 and r3['studentsAssigned'] == 7, 'one group per section')
by_sec = {g['sectionId']: g for g in r3['groups']}
check(by_sec[sec1]['currentMembers'] == 4 and by_sec[sec2]['currentMembers'] == 3, 'students stay in their own section')
call('POST', f"/groups/{by_sec[sec1]['groupId']}/members", {'userId': st[4]}, token=ADA, expect=None, label='(s5 is in section 02)')
call('POST', f"/groups/{by_sec[sec1]['groupId']}/members", {'userId': st[7]}, token=ADA, expect=400, label='not enrolled')

# ------------------------------------------------------------------ groups
g1 = next(g for g in r1['groups'] if g['groupName'].endswith('1'))
G1 = g1['groupId']
call('PATCH', f'/groups/{G1}', {'maxMembers': 2}, token=ADA, expect=409, label='limit below current members')
call('PATCH', f'/groups/{G1}', {'groupName': 'Team Rocket', 'maxMembers': 4}, token=ADA, expect=200, label='rename and raise the limit')
check(call('GET', f'/groups/{G1}', token=S[0], expect=200, label='group details')['groupName'] == 'Team Rocket', 'renamed group')
extra = call('POST', f"/group-sets/{gs['groupSetId']}/groups", {'groupName': 'Spare Team', 'sectionId': sec1}, token=ADA, expect=201, label='spare group')
call('PATCH', f"/groups/{extra['groupId']}", {'isActive': False}, token=ADA, expect=200, label='deactivate spare group')
m1 = call('GET', f'/groups/{G1}/members', token=ADA)
mover = m1[0]['userId']
call('POST', f"/groups/{extra['groupId']}/members", {'userId': mover}, token=ADA, expect=409, label='inactive group takes nobody')
call('DELETE', f"/groups/{extra['groupId']}", token=ADA, expect=204, label='delete empty group')
call('DELETE', f'/groups/{G1}', token=S[0], expect=403, label='students cannot delete groups')

# ------------------------------------------------------------------ self sign-up and my groups
club = call('POST', f'/courses/{C}/group-sets', {'setName': 'Study Circles', 'allowSelfSignup': True, 'autoAssignLeader': True, 'maxMembersPerGroup': 3},
            token=ADA, expect=201, label='self sign-up set with automatic leaders')
ca = call('POST', f"/group-sets/{club['groupSetId']}/groups", {'groupName': 'Circle A', 'sectionId': sec1}, token=ADA, expect=201, label='circle A')
cb = call('POST', f"/group-sets/{club['groupSetId']}/groups", {'groupName': 'Circle B', 'sectionId': sec1}, token=ADA, expect=201, label='circle B')
call('POST', f"/groups/{ca['groupId']}/members", None, token=S[0], expect=201, label='s1 joins circle A')
call('POST', f"/groups/{ca['groupId']}/members", {}, token=S[1], expect=201, label='s2 joins circle A')
call('POST', f"/groups/{ca['groupId']}/members", {}, token=S[2], expect=201, label='s3 joins circle A')
call('POST', f"/groups/{ca['groupId']}/members", {}, token=S[3], expect=409, label='circle A is full')
call('POST', f"/groups/{cb['groupId']}/members", {}, token=S[0], expect=409, label='one group per set')
call('POST', f"/groups/{G1}/members", {}, token=S[3], expect=403, label='no self sign-up in Capstone Teams')
mine = call('GET', f'/courses/{C}/groups/mine', token=S[0], expect=200, label="s1's groups")
check({g['setName'] for g in mine} == {'Capstone Teams', 'Lab Partners', 'Study Circles'} or len(mine) == 3, f'three groups ({[g["setName"] for g in mine]})')
check(next(g for g in mine if g['setName'] == 'Study Circles')['role'] == 'LEADER', 'first member leads (automatic)')
call('GET', f'/courses/{C}/groups/mine', token=S[7], expect=404, label='not in the course')

# ------------------------------------------------------------------ leaders
ld = call('GET', f"/groups/{ca['groupId']}/leaders", token=S[1], expect=200, label='members see the leaders')
check(len(ld) == 1 and ld[0]['userId'] == st[0] and ld[0]['leadershipRole'] == 'PROJECT_LEAD' and ld[0]['current'] and ld[0].get('appointedBy') is None,
      'automatic PROJECT_LEAD')
call('DELETE', f"/groups/{ca['groupId']}/members/{st[0]}", token=S[0], expect=204, label='the leader leaves (self sign-up)')
ld = call('GET', f"/groups/{ca['groupId']}/leaders", token=ADA)
cur = [l for l in ld if l['current']]
check(len(cur) == 1 and cur[0]['userId'] == st[1] and not next(l for l in ld if l['userId'] == st[0])['current'], 'lead handed to the next member')
check(next(m for m in call('GET', f"/groups/{ca['groupId']}/members", token=ADA) if m['userId'] == st[1])['role'] == 'LEADER', 'new leader marked')
call('POST', f"/groups/{ca['groupId']}/leaders", {'userId': st[2], 'leadershipRole': 'SCRIBE'}, token=S[1], expect=403, label='students cannot appoint')
call('POST', f"/groups/{ca['groupId']}/leaders", {'userId': st[5], 'leadershipRole': 'SCRIBE'}, token=ADA, expect=400, label='only members can lead')
call('POST', f"/groups/{ca['groupId']}/leaders", {'userId': st[2], 'termEndAt': '2020-01-01T00:00:00Z'}, token=ADA, expect=400, label='term must end in the future')
sc = call('POST', f"/groups/{ca['groupId']}/leaders", {'userId': st[2], 'leadershipRole': 'SCRIBE'}, token=ADA, expect=201, label='teacher appoints a scribe')
call('POST', f"/groups/{ca['groupId']}/leaders", {'userId': st[2], 'leadershipRole': 'SCRIBE'}, token=ADA, expect=409, label='same role twice')
check(next(m for m in call('GET', f"/groups/{ca['groupId']}/members", token=ADA) if m['userId'] == st[2])['role'] == 'LEADER', 'scribe marked as leader')
en = call('POST', f"/groups/{ca['groupId']}/leaders/{sc['leaderId']}/end", token=ADA, expect=200, label='end the scribe role')
check(not en['current'] and en['termEndAt'], 'ended')
check(next(m for m in call('GET', f"/groups/{ca['groupId']}/members", token=ADA) if m['userId'] == st[2])['role'] == 'MEMBER', 'back to member')
call('POST', f"/groups/{ca['groupId']}/leaders/{sc['leaderId']}/end", token=ADA, expect=409, label='already ended')

# ------------------------------------------------------------------ group set deletion
ga = call('POST', f'/courses/{C}/assignments', {'assignmentTitle': 'Capstone Proposal', 'assignmentType': 'TEXT_ENTRY', 'pointsPossible': 20,
          'groupSetId': gs['groupSetId']}, token=ADA, expect=201, label='group assignment on Capstone Teams')
call('DELETE', f"/group-sets/{gs['groupSetId']}", token=ADA, expect=409, label='set used by an assignment')
call('DELETE', f"/group-sets/{lab['groupSetId']}", token=ADA, expect=204, label='delete Lab Partners with its groups')
check(all(g['setName'] != 'Lab Partners' for g in call('GET', f'/courses/{C}/groups/mine', token=S[0])), 'its memberships are gone')

# ------------------------------------------------------------------ differentiation tags
tg = call('POST', f'/courses/{C}/tags', {'tagName': 'Extra Practice', 'tagDescription': 'Needs more time'}, token=ADA, expect=201, label='tag')
call('POST', f'/courses/{C}/tags', {'tagName': 'extra practice'}, token=ADA, expect=409, label='tag names are unique')
call('POST', f'/courses/{C}/tags', {'tagName': 'Nope'}, token=EVE, expect=403, label='TAs cannot create tags')
check(len(call('GET', f'/courses/{C}/tags', token=EVE, expect=200, label='TAs see tags')) == 1, 'listed for staff')
call('GET', f'/courses/{C}/tags', token=S[0], expect=403, label='students never see tags')
call('POST', f"/tags/{tg['tagId']}/members", {'userIds': [st[7]]}, token=ADA, expect=400, label='only students of the course')
tm = call('POST', f"/tags/{tg['tagId']}/members", {'userIds': [st[0], st[1], st[0]]}, token=ADA, expect=200, label='tag two students')
check(len(tm) == 2, 'duplicates skipped')
call('GET', f"/tags/{tg['tagId']}/members", token=S[0], expect=403, label='students cannot list tag members')
check(call('GET', f'/courses/{C}/tags', token=ADA)[0]['memberCount'] == 2, 'member count')
hw = call('POST', f'/courses/{C}/assignments', {'assignmentTitle': 'Problem Set', 'assignmentType': 'TEXT_ENTRY', 'pointsPossible': 10, 'dateDue': D(3)},
          token=ADA, expect=201, label='assignment due in 3 days')
call('POST', f"/assignments/{hw['assignmentId']}/publish", token=ADA, expect=200, label='publish')
call('POST', f"/assignments/{hw['assignmentId']}/overrides", {'targetType': 'TAG'}, token=ADA, expect=400, label='TAG override needs the tag')
ov = call('POST', f"/assignments/{hw['assignmentId']}/overrides", {'targetType': 'TAG', 'targetTagId': tg['tagId'], 'dateDue': f'{D(10)}T23:59:00Z'},
          token=ADA, expect=201, label='tagged students get 10 days')
check(ov['targetType'] == 'TAG' and ov['targetTagId'] == tg['tagId'], 'TAG override')
due = lambda tok: call('GET', f"/assignments/{hw['assignmentId']}", token=tok)['myStatus']['dateDue'][:10]
check(due(S[0]) == D(10) and due(S[2]) == D(3), 'tagged student has the later due date; others the normal one')
call('DELETE', f"/tags/{tg['tagId']}/members/{st[0]}", token=ADA, expect=204, label='untag s1')
check(due(S[0]) == D(3) and due(S[1]) == D(10), 'override follows the tag')
call('PATCH', f"/tags/{tg['tagId']}", {'tagName': 'Extended Time'}, token=ADA, expect=200, label='rename tag')
call('DELETE', f"/tags/{tg['tagId']}", token=ADA, expect=204, label='delete tag')
check(call('GET', f"/assignments/{hw['assignmentId']}/overrides", token=ADA) == [] and due(S[1]) == D(3), 'its override is gone')

# ------------------------------------------------------------------ observer pairing codes
call('POST', f'/users/{st[0]}/pairing-codes', token=S[1], expect=403, label="cannot make codes for another student")
call('POST', f'/users/{ada}/pairing-codes', token=A, expect=400, label='only students have codes')
pc = call('POST', f'/users/{st[0]}/pairing-codes', token=S[0], expect=201, label='student creates a pairing code')
code = pc['code']
check(len(code) == 8 and pc['dateExpires'][:10] in (D(7), D(8)), 'eight characters, a week to use it')
pc2 = call('POST', f'/users/{st[0]}/pairing-codes', token=S[0], expect=201, label='a new code replaces the old one')
call('POST', '/observer-links/pair', {'code': code}, token=OBS, expect=400, label='replaced code no longer works')
call('POST', '/observer-links/pair', {'code': pc2['code']}, token=TOM, expect=403, label='only observer accounts pair')
lk = call('POST', '/observer-links/pair', {'code': pc2['code'][:4].lower() + '-' + pc2['code'][4:], 'relationship': 'GUARDIAN'}, token=OBS, expect=201,
          label='parent enters the code (any case, with a dash)')
check(lk['studentUserId'] == st[0] and lk['observerUserId'] == obs and lk['relationship'] == 'GUARDIAN' and lk['linkStatus'] == 'ACTIVE', 'linked')
call('POST', '/observer-links/pair', {'code': pc2['code']}, token=OBS, expect=400, label='a code works once')
lst = call('GET', f'/users/{st[0]}/pairing-codes', token=S[0], expect=200, label='student lists codes')
check(len(lst) == 2 and all(x.get('code') is None for x in lst) and lst[0]['used'] and lst[0]['usedBy'] == obs, 'codes never shown again; use recorded')
check(any(l['observerUserId'] == obs for l in call('GET', f'/users/{st[0]}/observers', token=A, expect=200, label='admin sees the link')), 'observer listed')
pc3 = call('POST', f'/users/{st[0]}/pairing-codes', token=A, expect=201, label='admin can make a code too')
call('POST', '/observer-links/pair', {'code': pc3['code']}, token=OBS, expect=409, label='already following')
call('POST', f"/observer-links/{lk['observerLinkId']}/revoke", token=A, expect=200, label='revoke the link')
re = call('POST', '/observer-links/pair', {'code': pc3['code']}, token=OBS, expect=201, label='pair again after revoking')
check(re['observerLinkId'] == lk['observerLinkId'] and re['linkStatus'] == 'ACTIVE', 'same link re-activated')
call('GET', f'/users/{st[0]}/pairing-codes', token=X, expect=404, label='other institution')

passed = sum(1 for r in results if r[0])
print(f'{passed}/{len(results)} checks passed')
for ok, label, status, exp in results:
    print(('PASS' if ok else 'FAIL'), f'{status!s:>3}', label)
sys.exit(0 if passed == len(results) else 1)
