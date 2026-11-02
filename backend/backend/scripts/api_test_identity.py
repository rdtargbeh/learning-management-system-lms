"""End-to-end test for the rest of tenancy (branding, plan, user limits) and identity (sessions, refresh, sign-out, MFA,
password reset, email verification, locks, audit log, accessibility, preferences, accommodations, extra logins).

Needs a running backend whose database has the bootstrap platform owner (owner@lms-company.com / Owner-Pass-2026!)
and the app log at LMS_APP_LOG (default /tmp/app_layers.log), where the development mail sender writes reset links.
Creates its own institutions (IDF and IDX). Run it last: it turns MFA on and off for the platform owner.
    python scripts/api_test_identity.py
"""
import base64, hashlib, hmac, json, os, re, struct, sys, time, urllib.request
from datetime import datetime, timedelta, timezone

BASE = os.environ.get('LMS_API', 'http://localhost:8080/api/v1')
LOG = os.environ.get('LMS_APP_LOG', '/tmp/app_layers.log')
results = []


def call(method, path, body=None, token=None, expect=None, label=None):
    req = urllib.request.Request(BASE + path, method=method, data=None if body is None else json.dumps(body).encode(),
                                 headers={'Content-Type': 'application/json', 'User-Agent': 'api-test/1.0 (Mobile)',
                                          **({'Authorization': 'Bearer ' + token} if token else {})})
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


def totp(secret, at=None):
    key = base64.b32decode(secret + '=' * (-len(secret) % 8))
    step = int((at or time.time()) // 30)
    h = hmac.new(key, struct.pack('>Q', step), hashlib.sha1).digest()
    o = h[-1] & 0x0f
    return '%06d' % ((struct.unpack('>I', h[o:o + 4])[0] & 0x7fffffff) % 1000000)


def mail_token(kind, email):
    """Latest token of a kind (reset-token / verify-token) mailed to an address, from the app log."""
    time.sleep(0.3)
    found = None
    for line in open(LOG, errors='ignore'):
        if f'MAIL to={email} ' in line:
            m = re.search(kind + r'=([A-Za-z0-9_-]+)', line)
            if m: found = m.group(1)
    return found


def login(code, user, pw='Welcome-2026-x', expect=200, label=None):
    return call('POST', '/auth/login', {'tenantCode': code, 'login': user, 'password': pw}, expect=expect, label=label or f'login {user}')


# ------------------------------------------------------------------ setup
own = call('POST', '/auth/login', {'login': 'owner@lms-company.com', 'password': 'Owner-Pass-2026!'}, expect=200, label='platform owner login')
T = own['accessToken']
check(own['mfaSetupRequired'] is True and own['refreshToken'] and own['sessionId'], 'platform owner must set up MFA; session + refresh token issued')
call('POST', '/platform/tenants', {'tenantName': 'Identity University', 'tenantCode': 'IDF',
     'admin': {'username': 'admin.idf', 'email': 'admin@idf.edu', 'password': 'Admin-Pass-2026'}}, token=T, expect=201, label='create tenant IDF')
tx = call('POST', '/platform/tenants', {'tenantName': 'Elsewhere College', 'tenantCode': 'IDX',
          'admin': {'username': 'admin.idx', 'email': 'admin@idx.edu', 'password': 'Admin-Pass-2026'}}, token=T, expect=201, label='create tenant IDX')
TID = call('GET', '/platform/tenants?size=200', token=T)['items']
TID = next(t['tenantId'] for t in TID if t['tenantCode'] == 'IDF')
A = login('IDF', 'admin.idf', 'Admin-Pass-2026')['accessToken']
X = login('IDX', 'admin.idx', 'Admin-Pass-2026')['accessToken']


def mk(username, utype, expect=201):
    r = call('POST', '/users', {'username': username, 'email': f'{username}@idf.edu', 'userType': utype, 'password': 'Welcome-2026-x',
             'profile': {'firstName': username.split('.')[1].title(), 'lastName': 'Test'}}, token=A, expect=expect, label=f'create {username}')
    return r['user']['userId'] if expect == 201 else r


# ------------------------------------------------------------------ 01: branding
call('GET', '/public/tenants/NOPE/branding', expect=404, label='public branding of unknown institution')
call('PUT', '/tenant/branding', {'primaryColor': 'blue'}, token=A, expect=400, label='colors must be #RRGGBB')
b = call('PUT', '/tenant/branding', {'logoUrl': 'https://idf.edu/logo.png', 'primaryColor': '#003366', 'accentColor': '#FFCC00',
         'organizationTagline': 'Learning that lasts', 'customCss': '.header{font-weight:600}'}, token=A, expect=200, label='admin sets branding')
check(b['primaryColor'] == '#003366', 'branding saved')
pb = call('GET', '/public/tenants/idf/branding', expect=200, label='sign-in page branding without a token')
check(pb['tenantName'] == 'Identity University' and pb['logoUrl'].endswith('logo.png') and 'customCss' not in pb, 'public branding has no custom CSS')

# ------------------------------------------------------------------ 01: plan and user limits
call('PUT', f'/platform/tenants/{TID}/subscription', {'planType': 'PROFESSIONAL', 'planName': 'Pro 2026', 'maxStudents': 3,
     'dateStart': (datetime.now(timezone.utc).date() - timedelta(days=1)).isoformat()}, token=T, expect=200, label='platform sets the plan (3 students)')
sub = call('GET', '/tenant/subscription', token=A, expect=200, label='institution reads its plan')
check(sub['planType'] == 'PROFESSIONAL' and sub['maxStudents'] == 3, 'plan visible to the institution')
call('PUT', f'/platform/tenants/{TID}/user-limits', {'maxInstructors': 2}, token=A, expect=403, label='institution cannot change its limits')
call('PUT', f'/platform/tenants/{TID}/user-limits', {'maxInstructors': 2}, token=T, expect=200, label='platform limits instructors to 2')
ada, tom = mk('prof.ada', 'INSTRUCTOR'), mk('prof.tom', 'INSTRUCTOR')
lim = mk('prof.extra', 'INSTRUCTOR', expect=409)
check(lim.get('code') == 'USER_LIMIT_REACHED', 'third instructor refused')
s1, s2, s3 = mk('stu.one', 'STUDENT'), mk('stu.two', 'STUDENT'), mk('stu.three', 'STUDENT')
mk('stu.four', 'STUDENT', expect=409)
ul = call('GET', '/tenant/user-limits', token=A, expect=200, label='institution reads its limits')
check(ul['currentStudents'] == 3 and ul['currentInstructors'] == 2 and ul['currentAdmins'] == 1 and ul.get('maxStudents') is None, 'counts kept; student limit comes from the plan')
call('POST', f'/users/{s3}/deactivate', token=A, expect=200, label='deactivate a student')
s4 = mk('stu.four', 'STUDENT')
call('POST', f'/users/{s3}/reactivate', token=A, expect=409, label='reactivation refused while full')
fay = mk('reg.fay', 'STAFF')

# ------------------------------------------------------------------ 02: sessions, refresh, sign-out
l1 = login('IDF', 'stu.one')
check(l1['tokenType'] == 'Bearer' and l1['refreshToken'] and l1['refreshExpiresIn'] > 86400 and l1['mfaRequired'] is False, 'access + refresh token')
l2 = login('IDF', 'stu.one', label='second device')
ss = call('GET', '/auth/sessions', token=l1['accessToken'], expect=200, label='list my sessions')
check(len(ss) == 2 and sum(1 for s in ss if s['current']) == 1 and ss[0]['userAgent'].startswith('api-test'), 'two sessions, one current')
rf = call('POST', '/auth/refresh', {'refreshToken': l1['refreshToken']}, expect=200, label='refresh')
check(rf['accessToken'] and rf['refreshToken'] != l1['refreshToken'] and rf['sessionId'] == l1['sessionId'], 'rotated refresh token, same session')
call('POST', '/auth/refresh', {'refreshToken': l1['refreshToken']}, expect=401, label='old refresh token no longer works')
call('GET', '/auth/me', token=rf['accessToken'], expect=200, label='new access token works')
call('POST', '/auth/logout', token=rf['accessToken'], expect=204, label='sign out')
gone = call('GET', '/auth/me', token=rf['accessToken'], expect=401, label='token of a signed-out session refused')
check(gone and gone.get('code') == 'SESSION_ENDED', 'SESSION_ENDED')
call('POST', '/auth/refresh', {'refreshToken': rf['refreshToken']}, expect=401, label='refresh after sign-out refused')
call('GET', '/auth/me', token=l2['accessToken'], expect=200, label='the other device is still signed in')
l3 = login('IDF', 'stu.one', label='third device')
call('DELETE', f"/auth/sessions/{l2['sessionId']}", token=l3['accessToken'], expect=204, label='sign out another device')
call('GET', '/auth/me', token=l2['accessToken'], expect=401, label='that device is signed out')
call('DELETE', f"/auth/sessions/{l3['sessionId']}", token=login('IDF', 'stu.two')['accessToken'], expect=403, label="cannot end someone else's session")
t2 = login('IDF', 'stu.two')['accessToken']
call('POST', f'/users/{s2}/suspend', token=A, expect=200, label='admin suspends student two')
call('GET', '/auth/me', token=t2, expect=401, label='suspension ends sessions at once')
call('POST', f'/users/{s2}/reactivate', token=A, expect=200, label='reactivate student two')

# ------------------------------------------------------------------ 02: MFA
S1 = l3['accessToken']
check(call('GET', '/auth/mfa', token=S1, expect=200, label='MFA status')['enabled'] is False, 'MFA off')
setup = call('POST', '/auth/mfa/totp/setup', token=S1, expect=200, label='start authenticator setup')
check(setup['otpauthUri'].startswith('otpauth://totp/Identity%20University:stu.one?secret=' + setup['secret']), 'otpauth link for a QR code')
call('POST', '/auth/mfa/totp/enable', {'code': '000000' if totp(setup['secret']) != '000000' else '111111'}, token=S1, expect=400, label='wrong code refused')
bc = call('POST', '/auth/mfa/totp/enable', {'code': totp(setup['secret'])}, token=S1, expect=200, label='confirm setup')
check(len(bc['backupCodes']) == 10 and all(re.fullmatch(r'[A-Z2-9]{4}-[A-Z2-9]{4}', c) for c in bc['backupCodes']), '10 backup codes')
st = call('GET', '/auth/mfa', token=S1)
check(st['enabled'] and st['mfaType'] == 'TOTP' and st['backupCodesRemaining'] == 10, 'MFA on')
call('POST', '/auth/mfa/totp/setup', token=S1, expect=409, label='setup again refused while on')
p1 = login('IDF', 'stu.one', label='password step')
check(p1['mfaRequired'] is True and p1.get('accessToken') is None and p1['mfaToken'], 'second step required, no access token yet')
call('GET', '/auth/me', token=p1['mfaToken'], expect=401, label='MFA-step token is not an access token')
call('POST', '/auth/mfa/verify', {'mfaToken': p1['mfaToken'], 'code': '123456' if totp(setup['secret']) != '123456' else '654321'}, expect=401, label='wrong code')
v = call('POST', '/auth/mfa/verify', {'mfaToken': p1['mfaToken'], 'code': totp(setup['secret'])}, expect=200, label='right code')
check(v['accessToken'] and v['refreshToken'], 'signed in after MFA')
p2 = login('IDF', 'stu.one', label='password step (backup code)')
call('POST', '/auth/mfa/verify', {'mfaToken': p2['mfaToken'], 'code': bc['backupCodes'][0].lower()}, expect=200, label='backup code works')
p3 = login('IDF', 'stu.one', label='password step (same backup code)')
call('POST', '/auth/mfa/verify', {'mfaToken': p3['mfaToken'], 'code': bc['backupCodes'][0]}, expect=401, label='a backup code works once')
check(call('GET', '/auth/mfa', token=v['accessToken'])['backupCodesRemaining'] == 9, '9 backup codes left')
call('POST', '/auth/mfa/verify', {'mfaToken': 'not-a-token', 'code': '123456'}, expect=401, label='garbage MFA token')
hist = call('GET', '/auth/login-history?size=20', token=v['accessToken'], expect=200, label='my sign-in history')
statuses = [h['loginStatus'] for h in hist['items']]
check('MFA_REQUIRED' in statuses and any(h['loginStatus'] == 'SUCCESS' and h['mfaUsed'] for h in hist['items']) and hist['items'][0]['ipAddress'], 'history shows the MFA step')
call('POST', '/auth/mfa/disable', {'password': 'wrong-password', 'code': totp(setup['secret'])}, token=v['accessToken'], expect=400, label='disable needs the password')
call('POST', '/auth/mfa/disable', {'password': 'Welcome-2026-x', 'code': totp(setup['secret'])}, token=v['accessToken'], expect=204, label='disable MFA')
check(login('IDF', 'stu.one', label='sign in without MFA again')['mfaRequired'] is False, 'MFA off again')
# administrator reset (lost phone)
S4 = login('IDF', 'stu.four')['accessToken']
sec4 = call('POST', '/auth/mfa/totp/setup', token=S4, expect=200, label='student four sets up MFA')['secret']
call('POST', '/auth/mfa/totp/enable', {'code': totp(sec4)}, token=S4, expect=200, label='student four turns MFA on')
call('POST', f'/users/{s4}/mfa/reset', token=login('IDF', 'prof.ada')['accessToken'], expect=403, label='teacher cannot reset MFA')
call('POST', f'/users/{s4}/mfa/reset', token=A, expect=204, label='admin resets MFA')
check(login('IDF', 'stu.four', label='sign in after reset')['mfaRequired'] is False, 'MFA removed by admin')

# ------------------------------------------------------------------ 02: forgot password, email verification
call('POST', '/auth/password/forgot', {'tenantCode': 'IDF', 'login': 'nobody.here'}, expect=202, label='forgot password: unknown login looks the same')
old_session = login('IDF', 'stu.two')['accessToken']
call('POST', '/auth/password/forgot', {'tenantCode': 'IDF', 'login': 'stu.two@idf.edu'}, expect=202, label='forgot password')
tok = mail_token('reset-token', 'stu.two@idf.edu')
check(tok, 'reset link mailed (development mail log)')
call('POST', '/auth/password/reset', {'token': 'x' * 40, 'newPassword': 'Brand-New-Pass-1'}, expect=400, label='wrong token')
call('POST', '/auth/password/reset', {'token': tok, 'newPassword': 'Welcome-2026-x'}, expect=400, label='new password must differ')
call('POST', '/auth/password/reset', {'token': tok, 'newPassword': 'Brand-New-Pass-1'}, expect=204, label='reset password')
call('POST', '/auth/password/reset', {'token': tok, 'newPassword': 'Another-Pass-22'}, expect=400, label='reset link works once')
call('GET', '/auth/me', token=old_session, expect=401, label='reset ends existing sessions')
login('IDF', 'stu.two', expect=401, label='old password no longer works')
S2 = login('IDF', 'stu.two', 'Brand-New-Pass-1', label='sign in with the new password')['accessToken']
call('POST', '/auth/email/verify/request', token=S2, expect=202, label='ask for email verification')
vt = mail_token('verify-token', 'stu.two@idf.edu')
call('POST', '/auth/email/verify', {'token': vt}, expect=204, label='verify email')
call('POST', '/auth/email/verify', {'token': vt}, expect=400, label='verification link works once')
call('POST', '/auth/email/verify/request', token=S2, expect=409, label='already verified')
call('POST', '/auth/password', {'currentPassword': 'Brand-New-Pass-1', 'newPassword': 'Brand-New-Pass-1'}, token=S2, expect=400, label='change password: must differ')
second = login('IDF', 'stu.two', 'Brand-New-Pass-1', label='another device')['accessToken']
call('POST', '/auth/password', {'currentPassword': 'Brand-New-Pass-1', 'newPassword': 'Changed-Pass-333'}, token=S2, expect=204, label='change own password')
call('GET', '/auth/me', token=S2, expect=200, label='this device stays signed in')
call('GET', '/auth/me', token=second, expect=401, label='other devices are signed out')

# ------------------------------------------------------------------ 02: locks
for i in range(5):
    login('IDF', 'stu.one', 'bad-pw', expect=401, label=f'wrong password {i + 1}')
login('IDF', 'stu.one', expect=423, label='locked after 5 misses even with the right password')
lk = call('GET', f'/users/{s1}/lock', token=A, expect=200, label='admin sees the lock')
check(lk['isLocked'] and lk['lockReason'] == 'Too many failed sign-ins' and lk['lockedUntil'], 'automatic lock recorded')
call('POST', f'/users/{s1}/unlock', token=A, expect=200, label='admin unlocks')
S1 = login('IDF', 'stu.one', label='sign in after unlock')['accessToken']
call('POST', f'/users/{s1}/lock', {'reason': 'Academic integrity review', 'lockedUntil': (datetime.now(timezone.utc) - timedelta(hours=1)).isoformat()},
     token=A, expect=400, label='lock end must be in the future')
call('POST', f'/users/{s1}/lock', {'reason': 'Academic integrity review'}, token=A, expect=200, label='admin locks indefinitely')
call('GET', '/auth/me', token=S1, expect=401, label='locking ends sessions')
lr = login('IDF', 'stu.one', expect=423, label='locked account cannot sign in')
check(lr.get('code') == 'ACCOUNT_LOCKED', 'ACCOUNT_LOCKED')
call('POST', f'/users/{s1}/unlock', token=A, expect=200, label='unlock')
S1 = login('IDF', 'stu.one', label='sign in again')['accessToken']
call('POST', f'/users/{s1}/lock', {'reason': 'x'}, token=S1, expect=403, label='students cannot lock accounts')

# ------------------------------------------------------------------ 02: extra logins
call('POST', f'/users/{s1}/logins', {'uniqueLogin': 'S1000123', 'sisSourceId': 'SIS-1000123'}, token=A, expect=201, label='add a SIS login')
call('POST', f'/users/{s2}/logins', {'uniqueLogin': 'stu.one'}, token=A, expect=409, label="cannot take another user's username")
login('IDF', 'S1000123', label='sign in with the SIS login')
lg = call('GET', f'/users/{s1}/logins', token=S1, expect=200, label='own logins')
call('DELETE', f"/users/{s1}/logins/{lg[0]['loginId']}", token=A, expect=204, label='remove the SIS login')
login('IDF', 'S1000123', expect=401, label='removed login no longer works')

# ------------------------------------------------------------------ 02: accessibility and preferences
acc = call('GET', '/me/accessibility', token=S1, expect=200, label='my accessibility (defaults)')
check(acc['textSizePercentage'] == 100 and acc['highContrastEnabled'] is False, 'database defaults')
call('PATCH', '/me/accessibility', {'extendedTimePercentage': 130}, token=S1, expect=400, label='extended time must be a standard step')
call('PATCH', '/me/accessibility', {'colorBlindMode': True}, token=S1, expect=400, label='color-blind mode needs a type')
call('PATCH', '/me/accessibility', {'textSizePercentage': 300}, token=S1, expect=400, label='text size 50-200')
acc = call('PATCH', '/me/accessibility', {'textSizePercentage': 150, 'highContrastEnabled': True, 'colorBlindMode': True, 'colorBlindType': 'DEUTERANOPIA'},
           token=S1, expect=200, label='change accessibility')
check(acc['textSizePercentage'] == 150 and acc['highContrastEnabled'] and acc['captionsEnabled'] is not None, 'changed; other values kept')
ADA = login('IDF', 'prof.ada')['accessToken']
call('GET', f'/users/{s1}/accessibility', token=ADA, expect=403, label="teacher without USER_READ cannot read a student's settings")
check(call('GET', f'/users/{s1}/accessibility', token=A, expect=200, label='admin reads them')['textSizePercentage'] == 150, 'admin view')
pr = call('GET', '/me/preferences', token=S1, expect=200, label='my preferences')
pr = call('PATCH', '/me/preferences', {'themePreference': 'DARK', 'learningStyle': 'VISUAL'}, token=S1, expect=200, label='change preferences')
check(pr['themePreference'] == 'DARK' and pr['learningStyle'] == 'VISUAL', 'preferences saved')

# ------------------------------------------------------------------ 02: accommodations
c = call('POST', '/courses', {'courseCode': 'BIO101', 'courseTitle': 'Biology', 'instructorUserId': ada}, token=A, expect=201, label='course')
sec = call('GET', f"/courses/{c['courseId']}/sections", token=A)[0]['sectionId']
call('POST', f'/sections/{sec}/enrollments', {'userId': s1}, token=A, expect=201, label='enroll student one')
call('POST', f'/users/{ada}/accommodations', {'accommodationType': 'NOTE_TAKER'}, token=A, expect=400, label='accommodations are for students')
call('POST', f'/users/{s1}/accommodations', {'accommodationType': 'EXTENDED_TIME'}, token=A, expect=400, label='extended time needs a percentage')
call('POST', f'/users/{s1}/accommodations', {'accommodationType': 'EXTENDED_TIME', 'extendedTimePercentage': 150}, token=ADA, expect=403,
     label='teacher cannot approve accommodations')
ac = call('POST', f'/users/{s1}/accommodations', {'accommodationType': 'EXTENDED_TIME', 'extendedTimePercentage': 150, 'isConfidential': True,
          'details': {'diagnosis': 'confidential note'}}, token=A, expect=201, label='disability services approves extended time')
check(ac['isActive'] and ac['approvedBy'] and ac['dateApproved'], 'approved')
check(len(call('GET', f'/users/{s1}/accommodations', token=S1, expect=200, label='student sees own accommodations')) == 1, 'own list')
ca = call('GET', f"/courses/{c['courseId']}/accommodations", token=ADA, expect=200, label="teacher sees the course's accommodations")
check(len(ca) == 1 and ca[0]['extendedTimePercentage'] == 150 and ca[0].get('details') is None and ca[0]['username'] == 'stu.one', 'confidential details hidden from teachers')
call('GET', f"/courses/{c['courseId']}/accommodations", token=login('IDF', 'prof.tom')['accessToken'], expect=403, label='other teacher cannot')
call('PATCH', f"/accommodations/{ac['accommodationId']}", {'isActive': False}, token=A, expect=200, label='end the accommodation')
check(call('GET', f"/courses/{c['courseId']}/accommodations", token=ADA) == [], 'inactive ones are not shown')

# ------------------------------------------------------------------ 02: audit log
au = call('GET', f'/audit-log?affectedUserId={s1}&size=100', token=A, expect=200, label='admin searches the audit log')
acts = {x['actionType'] for x in au['items']}
check({'USER_CREATED', 'MFA_ENABLED', 'MFA_DISABLED', 'ACCOUNT_LOCKED', 'ACCOUNT_UNLOCKED', 'LOGIN_ADDED', 'ACCOMMODATION_APPROVED'} <= acts, f'audit trail ({sorted(acts)})')
pw = call('GET', f'/audit-log?affectedUserId={s2}&actionType=PASSWORD_RESET', token=A, expect=200, label='password reset audited')
check(pw['totalItems'] == 1 and pw['items'][0]['actorUserId'] == s2 and pw['items'][0]['severity'] == 'WARNING', 'reset recorded as the user')
check(all(x['ipAddress'] for x in au['items'] if x['actionType'] == 'ACCOUNT_LOCKED'), 'IP recorded')
call('GET', '/audit-log', token=ADA, expect=403, label='teacher cannot read the audit log')
role = call('POST', '/roles', {'roleName': 'Compliance Office', 'permissions': ['AUDIT_READ']}, token=A, expect=201, label='role with AUDIT_READ')
call('POST', f'/users/{fay}/roles', {'roleId': role['roleId']}, token=A, expect=201, label='give it to staff')
call('GET', '/audit-log?size=5', token=login('IDF', 'reg.fay')['accessToken'], expect=200, label='staff with AUDIT_READ can read it')
xa = call('GET', '/audit-log?size=200', token=X, expect=200, label='other institution reads its own log')
check(all(x['tenantId'] != TID for x in xa['items']), "other institution sees none of IDF's entries")
pa = call('GET', f'/audit-log?tenantId={TID}&size=5', token=T, expect=200, label='platform admin reads an institution\'s log')
check(pa['totalItems'] > 5 and all(x['tenantId'] == TID for x in pa['items']), 'platform view filtered by tenant')
call('GET', f'/users/{s1}/lock', token=X, expect=404, label='other institution cannot see our users')

# ------------------------------------------------------------------ platform owner: MFA is required
osec = call('POST', '/auth/mfa/totp/setup', token=T, expect=200, label='owner sets up MFA')['secret']
call('POST', '/auth/mfa/totp/enable', {'code': totp(osec)}, token=T, expect=200, label='owner turns MFA on')
call('POST', '/auth/mfa/disable', {'password': 'Owner-Pass-2026!', 'code': totp(osec)}, token=T, expect=409, label='platform users cannot turn MFA off')
op = call('POST', '/auth/login', {'login': 'owner@lms-company.com', 'password': 'Owner-Pass-2026!'}, expect=200, label='owner password step')
check(op['mfaRequired'] and op.get('accessToken') is None, 'owner now needs a code')
ov = call('POST', '/auth/mfa/verify', {'mfaToken': op['mfaToken'], 'code': totp(osec)}, expect=200, label='owner signs in with a code')
check(ov['mfaSetupRequired'] is False, 'setup no longer required')
call('POST', f"/users/{ov['user']['userId']}/mfa/reset", token=ov['accessToken'], expect=204, label='(reset owner MFA so the database stays usable for other tests)')

passed = sum(1 for r in results if r[0])
print(f'{passed}/{len(results)} checks passed')
for ok, label, status, exp in results:
    print(('PASS' if ok else 'FAIL'), f'{status!s:>3}', label)
sys.exit(0 if passed == len(results) else 1)
