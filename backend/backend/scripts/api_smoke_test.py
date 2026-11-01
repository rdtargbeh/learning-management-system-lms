"""End-to-end smoke test for the tenancy + identity API.

Run against a FRESH database started with LMS_BOOTSTRAP_ADMIN_EMAIL=owner@lms-company.com and
LMS_BOOTSTRAP_ADMIN_PASSWORD=Owner-Pass-2026!  ->  python scripts/api_smoke_test.py
"""
import json, urllib.request, sys
import os
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
    try: payload = json.loads(data) if data else None
    except Exception: payload = data.decode()[:200]
    ok = expect is None or status == expect
    results.append((ok, label or f'{method} {path}', status, expect))
    if not ok: print('   !!', label, status, json.dumps(payload)[:300])
    return payload

# 1) platform owner signs in (no tenantCode)
owner = call('POST', '/auth/login', {'login': 'owner@lms-company.com', 'password': 'Owner-Pass-2026!'}, expect=200, label='platform owner login')
T = owner['accessToken']
me = call('GET', '/auth/me', token=T, expect=200, label='platform /me')
assert me['platformProfile']['platformRole'] == 'SUPER_ADMIN' and me['user'].get('tenantId') is None and me['user']['systemUser']

# 2) onboard two institutions, each with its first admin
su = call('POST', '/platform/tenants', {'tenantName': 'State University', 'tenantCode': 'SU', 'primaryDomain': 'su.edu',
          'institutionName': 'State University', 'admin': {'username': 'admin.su', 'email': 'admin@su.edu', 'password': 'Admin-Pass-2026', 'firstName': 'Ana', 'lastName': 'Admin'}},
          token=T, expect=201, label='create tenant SU + admin')
cc = call('POST', '/platform/tenants', {'tenantName': 'City College', 'tenantCode': 'CC',
          'admin': {'username': 'admin.cc', 'email': 'admin@cc.edu', 'password': 'Admin-Pass-2026'}}, token=T, expect=201, label='create tenant CC + admin')
call('POST', '/platform/tenants', {'tenantName': 'Dup', 'tenantCode': 'su', 'admin': {'username': 'x.x', 'email': 'x@x.edu', 'password': 'Admin-Pass-2026'}},
     token=T, expect=409, label='duplicate tenant code rejected')
tl = call('GET', '/platform/tenants', token=T, expect=200, label='list tenants')
assert tl['totalItems'] == 2

# 3) platform support staff (not admin)
call('POST', '/platform/users', {'username': 'support.dana', 'email': 'dana@lms-company.com', 'password': 'Support-Pass-2026', 'platformRole': 'SUPPORT',
     'canImpersonate': True, 'profile': {'firstName': 'Dana', 'lastName': 'Support'}}, token=T, expect=201, label='create platform support user')

# 4) SU admin signs in with tenantCode
sa = call('POST', '/auth/login', {'tenantCode': 'su', 'login': 'admin.su', 'password': 'Admin-Pass-2026'}, expect=200, label='tenant admin login')
A = sa['accessToken']
call('POST', '/auth/login', {'login': 'admin.su', 'password': 'Admin-Pass-2026'}, expect=401, label='tenant user cannot sign in as platform')
call('POST', '/auth/login', {'tenantCode': 'CC', 'login': 'admin.su', 'password': 'Admin-Pass-2026'}, expect=401, label='SU admin cannot sign in to CC')
call('GET', '/platform/tenants', token=A, expect=403, label='tenant admin blocked from platform API')

# 5) create every tenant user type; profiles are created automatically
def mk(username, utype, profile=None):
    return call('POST', '/users', {'username': username, 'email': f'{username}@su.edu', 'userType': utype, 'password': 'Welcome-2026-x',
               'profile': profile or {'firstName': username.split('.')[1].title(), 'lastName': 'Test'}}, token=A, expect=201, label=f'create {utype}')
bob = mk('stu.bob', 'STUDENT'); ada = mk('prof.ada', 'INSTRUCTOR'); eve = mk('ta.eve', 'TEACHING_ASSISTANT')
hal = mk('adv.hal', 'ADVISOR'); fay = mk('reg.fay', 'STAFF'); gail = mk('mom.gail', 'OBSERVER')
assert bob.get('studentProfile') is not None and bob.get('employeeProfile') is None
assert ada['employeeProfile']['employeeType'] == 'FACULTY'
assert eve['employeeProfile']['academicRank'] == 'TEACHING_ASSISTANT'
assert hal['employeeProfile']['employeeType'] == 'ACADEMIC_ADVISOR' and hal.get('advisorProfile') is not None
assert fay['employeeProfile']['employeeType'] == 'STAFF'
assert gail.get('studentProfile') is None and gail.get('employeeProfile') is None and gail.get('advisorProfile') is None
assert bob['profile']['displayName'] == 'Bob Test'
call('POST', '/users', {'username': 'fake.ops', 'email': 'fake@su.edu', 'userType': 'PLATFORM', 'password': 'Welcome-2026-x'}, token=A, expect=400, label='tenant cannot create PLATFORM user')
call('POST', '/users', {'username': 'stu.bob', 'email': 'other@su.edu', 'userType': 'STUDENT', 'password': 'Welcome-2026-x'}, token=A, expect=409, label='duplicate username rejected')

# 6) profile updates
sp = call('PATCH', f"/users/{bob['user']['userId']}/student-profile", {'studentNumber': 'S1001', 'academicLevel': 'UNDERGRADUATE', 'major': 'Computer Science', 'cumulativeGpa': 3.6, 'advisorUserId': hal['user']['userId']}, token=A, expect=200, label='update student profile')
assert sp['studentNumber'] == 'S1001'
call('PATCH', f"/users/{ada['user']['userId']}/employee-profile", {'employeeNumber': 'E2001', 'academicRank': 'ASSOCIATE_PROFESSOR', 'jobTitle': 'Associate Professor'}, token=A, expect=200, label='update employee profile')
call('PATCH', f"/users/{fay['user']['userId']}/employee-profile", {'academicRank': 'PROFESSOR'}, token=A, expect=409, label='rank on non-faculty rejected by DB rule')
call('PATCH', f"/users/{hal['user']['userId']}/advisor-profile", {'advisorType': 'ATHLETICS', 'maxAdvisees': 40}, token=A, expect=200, label='update advisor profile')
call('PATCH', f"/users/{bob['user']['userId']}/advisor-profile", {'maxAdvisees': 5}, token=A, expect=400, label='advisor profile needs employee profile')

# 7) search + filters
lst = call('GET', '/users?type=STUDENT', token=A, expect=200, label='search students')
assert lst['totalItems'] == 1 and lst['items'][0]['username'] == 'stu.bob'
allu = call('GET', '/users?size=100', token=A, expect=200, label='list SU users')
assert allu['totalItems'] == 7

# 8) roles + permissions
perms = call('GET', '/permissions', token=A, expect=200, label='list permissions')
assert {'USER_READ', 'USER_MANAGE'} <= {p['permissionCode'] for p in perms}
role = call('POST', '/roles', {'roleName': 'Registrar Office', 'permissions': ['USER_READ', 'USER_MANAGE']}, token=A, expect=201, label='create tenant role')
call('POST', f"/users/{fay['user']['userId']}/roles", {'roleId': role['roleId']}, token=A, expect=201, label='assign role')
fl = call('POST', '/auth/login', {'tenantCode': 'SU', 'login': 'reg.fay@su.edu', 'password': 'Welcome-2026-x'}, expect=200, label='staff login by email')
assert fl['permissions'] == ['USER_MANAGE', 'USER_READ']
F = fl['accessToken']
call('GET', '/users?type=STUDENT', token=F, expect=200, label='staff with USER_READ can list users')
bl = call('POST', '/auth/login', {'tenantCode': 'SU', 'login': 'stu.bob', 'password': 'Welcome-2026-x'}, expect=200, label='student login')
Bt = bl['accessToken']
call('GET', '/users', token=Bt, expect=403, label='student cannot list users')
call('GET', f"/users/{bob['user']['userId']}", token=Bt, expect=200, label='student can read self')
call('PATCH', f"/users/{bob['user']['userId']}/profile", {'pronouns': 'he/him'}, token=Bt, expect=200, label='student edits own profile')
call('PATCH', f"/users/{bob['user']['userId']}/student-profile", {'cumulativeGpa': 4.0}, token=Bt, expect=403, label='student cannot edit own GPA')

# 9) observers
link = call('POST', '/observer-links', {'observerUserId': gail['user']['userId'], 'studentUserId': bob['user']['userId'], 'relationship': 'PARENT'}, token=A, expect=201, label='link parent to student')
call('POST', '/observer-links', {'observerUserId': hal['user']['userId'], 'studentUserId': bob['user']['userId'], 'relationship': 'ATHLETIC_ADVISOR', 'accessLevel': 'VIEW_GRADES'}, token=A, expect=201, label='link advisor as observer')
obs = call('GET', f"/users/{bob['user']['userId']}/observers", token=A, expect=200, label='list observers')
assert len(obs) == 2
call('POST', f"/observer-links/{link['observerLinkId']}/revoke", token=A, expect=200, label='revoke observer link')

# 10) tenant isolation through the API
ca = call('POST', '/auth/login', {'tenantCode': 'CC', 'login': 'admin.cc', 'password': 'Admin-Pass-2026'}, expect=200, label='CC admin login')
C = ca['accessToken']
call('GET', f"/users/{bob['user']['userId']}", token=C, expect=404, label='CC admin cannot see SU student')
ccl = call('GET', '/users', token=C, expect=200, label='CC sees only own users')
assert ccl['totalItems'] == 1
call('POST', '/observer-links', {'observerUserId': ca['user']['userId'], 'studentUserId': bob['user']['userId']}, token=C, expect=404, label='CC cannot link to SU student')

# 11) deactivate instead of delete
call('POST', f"/users/{bob['user']['userId']}/deactivate", token=A, expect=200, label='deactivate student')
call('POST', '/auth/login', {'tenantCode': 'SU', 'login': 'stu.bob', 'password': 'Welcome-2026-x'}, expect=401, label='deactivated user cannot sign in')
call('POST', f"/users/{sa['user']['userId']}/deactivate", token=A, expect=400, label='admin cannot deactivate self')

# 12) support access: request -> self-approve blocked -> approved by owner -> tenant-scoped token
dl = call('POST', '/auth/login', {'login': 'support.dana', 'password': 'Support-Pass-2026'}, expect=200, label='support user login')
D = dl['accessToken']
call('GET', '/platform/tenants', token=D, expect=403, label='support user is not platform admin')
req = call('POST', '/platform/support-access', {'tenantId': su['tenant']['tenantId'], 'accessReason': 'Ticket 42: student cannot log in', 'ticketReference': 'T-42', 'durationMinutes': 30}, token=D, expect=201, label='request support access')
call('POST', f"/platform/support-access/{req['supportAccessId']}/start", token=D, expect=409, label='cannot start before approval')
call('POST', f"/platform/support-access/{req['supportAccessId']}/approve", token=T, expect=200, label='owner approves')
ss = call('POST', f"/platform/support-access/{req['supportAccessId']}/start", token=D, expect=200, label='start support session')
S = ss['accessToken']
sl = call('GET', '/users?size=100', token=S, expect=200, label='support session lists SU users')
assert sl['totalItems'] == 7
call('GET', f"/users/{ca['user']['userId']}", token=S, expect=404, label='support session cannot see CC users')
call('POST', f"/platform/support-access/{req['supportAccessId']}/end", token=D, expect=200, label='end support session')

# 13) tenant self + suspend tenant blocks sign-in
call('GET', '/tenant', token=A, expect=200, label='tenant admin reads own tenant')
call('PATCH', '/tenant/settings', {'supportEmail': 'help@su.edu', 'passwordMinLength': 12}, token=A, expect=200, label='update tenant settings')
call('PATCH', f"/platform/tenants/{cc['tenant']['tenantId']}", {'tenantStatus': 'SUSPENDED'}, token=T, expect=200, label='suspend CC')
call('POST', '/auth/login', {'tenantCode': 'CC', 'login': 'admin.cc', 'password': 'Admin-Pass-2026'}, expect=401, label='suspended tenant cannot sign in')
call('GET', '/users', expect=401, label='no token -> 401')

passed = sum(1 for r in results if r[0])
print(f'{passed}/{len(results)} checks passed')
for ok, label, status, exp in results:
    print(('PASS' if ok else 'FAIL'), f'{status:>3}', label)
