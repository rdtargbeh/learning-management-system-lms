"""Usage:  python scripts/gen_entities.py [output_dir]

Generate JPA entities (entity/), Spring Data repositories (repository/) and enums (enums/) for every table in lms_core.

Source of truth: the live PostgreSQL catalog of the final schema (catalog.json) and the
domain grouping used by the domain SQL files (groups.json). Re-run after schema changes.
"""
import json, re, os, sys

HERE = os.path.dirname(os.path.abspath(__file__))
# catalog.json = output of scripts/catalog.sql run against a database built from src/main/resources/db/schema:
#   psql -d lms -At -f scripts/catalog.sql > scripts/catalog.json
CAT = json.load(open(os.path.join(HERE, 'catalog.json')))
G = json.load(open(os.path.join(HERE, 'domain_groups.json')))   # table -> domain, same grouping as the domain SQL files
OUT = sys.argv[1] if len(sys.argv) > 1 else os.path.join(HERE, '..', 'src', 'main', 'java', 'learning_management_system', 'backend')
BASE = 'learning_management_system.backend'

# Every table must belong to exactly one domain (the domain is named in each entity's Javadoc).
_grouped = [t for ts in G['groups'].values() for t in ts]
assert sorted(_grouped) == sorted(t['table_name'] for t in CAT), set(_grouped) ^ {t['table_name'] for t in CAT}

CLASS_OVERRIDE = {'users': 'User', 'roles': 'Role', 'permissions': 'Permission', 'role_permissions': 'RolePermission',
                  'group_members': 'GroupMember'}
JAVA_KW = {'abstract','assert','boolean','break','byte','case','catch','char','class','const','continue','default','do','double',
           'else','enum','extends','final','finally','float','for','goto','if','implements','import','instanceof','int','interface',
           'long','native','new','package','private','protected','public','return','short','static','strictfp','super','switch',
           'synchronized','this','throw','throws','transient','try','void','volatile','while','record','var','yield'}

def pascal(s): return ''.join(p[:1].upper() + p[1:] for p in s.split('_') if p)
def camel(s):
    p = pascal(s); r = p[:1].lower() + p[1:]
    return r + '_' if r in JAVA_KW else r
def cls(t): return CLASS_OVERRIDE.get(t, pascal(t))

ENUM_RE = re.compile(r"\(+\(?(\w+)\)?::text = ANY \(\(?ARRAY\[(.*?)\]\)?::text\[\]\)\)")
VAL_RE = re.compile(r"'([^']*)'::character varying")

RESERVED_SIMPLE = {'Entity','Table','Id','GeneratedValue','GenerationType','Column','Enumerated','EnumType','EntityListeners',
    'JdbcTypeCode','SqlTypes','Generated','DynamicInsert','DynamicUpdate','Getter','Setter','NoArgsConstructor','UUID','JsonNode',
    'BigDecimal','OffsetDateTime','LocalDate','LocalTime','InetAddress','String','Integer','Long','Boolean','Short','Double',
    'TenantScoped','TenantEntityListener','Override','Object'}
def base_enum_name(col):
    e = pascal(col)
    return e + 'Value' if e in RESERVED_SIMPLE else e

def enums_for(table):
    found = {}
    for chk in table['checks'] or []:
        for m in ENUM_RE.finditer(chk):
            col, arr = m.group(1), m.group(2)
            vals = VAL_RE.findall(arr)
            if not vals or col in found: continue
            if all(re.fullmatch(r'[A-Z][A-Z0-9_]*', v) for v in vals):
                found[col] = vals
    return found

def jtype(c, imports):
    dt, udt = c['data_type'], c['udt']
    ann = []
    if udt == 'uuid': imports.add('java.util.UUID'); return 'UUID', ann
    if udt in ('varchar', 'text'): return 'String', ann
    if udt == 'citext': return 'String', ['columnDefinition = "citext"']
    if udt == 'bpchar': return 'String', ['columnDefinition = "bpchar"']
    if udt == 'bool': return 'Boolean', ann
    if udt == 'int4': return 'Integer', ann
    if udt == 'int8': return 'Long', ann
    if udt == 'int2': return 'Short', ann
    if udt == 'numeric': imports.add('java.math.BigDecimal'); return 'BigDecimal', ann
    if udt == 'float8': return 'Double', ann
    if udt == 'timestamptz': imports.add('java.time.OffsetDateTime'); return 'OffsetDateTime', ann
    if udt == 'date': imports.add('java.time.LocalDate'); return 'LocalDate', ann
    if udt == 'time': imports.add('java.time.LocalTime'); return 'LocalTime', ann
    if udt == 'jsonb': imports.add('com.fasterxml.jackson.databind.JsonNode'); return 'JsonNode', ann
    if udt == 'inet': imports.add('java.net.InetAddress'); return 'InetAddress', ann
    if udt == '_uuid': imports.add('java.util.UUID'); return 'UUID[]', ann
    if udt == '_text': return 'String[]', ann
    if udt == '_inet': imports.add('java.net.InetAddress'); return 'InetAddress[]', ann
    raise ValueError(f'unmapped type {udt} on {c["name"]}')

# ---------------------------------------------------------------- enum names (global)
# Every CHECK (col IN ('A','B',...)) becomes a top-level enum in the enums package. The enum is named after the column
# (UserType, AccountStatus); when two tables would produce the same name, or the name equals an entity class, the
# entity name is put in front (Course + Status -> CourseStatus) so every enum name is unique.
ENTITY_NAMES = {cls(t['table_name']) for t in CAT}

def _words(x): return re.findall(r'[A-Z][a-z0-9]*', x)
def prefixed(C, b):
    """Entity + enum name without repeating the shared word: AcademicPlan + PlanStatus -> AcademicPlanStatus."""
    cw, bw = _words(C), _words(b)
    for k in range(min(len(cw), len(bw) - 1), 0, -1):
        if cw[-k:] == bw[:k]:
            return C + ''.join(bw[k:])
    return C + b
_bases = {}
for t in CAT:
    for col in enums_for(t):
        _bases.setdefault(base_enum_name(col), []).append(t['table_name'])
ENUM_NAMES = {}            # (table, column) -> enum class name
for t in CAT:
    C = cls(t['table_name'])
    for col in enums_for(t):
        b = base_enum_name(col)
        clash = len(_bases[b]) > 1 or b in ENTITY_NAMES
        ENUM_NAMES[(t['table_name'], col)] = prefixed(C, b) if clash else b
_seen = {}
for k, v in ENUM_NAMES.items():
    assert v not in _seen, f'enum name {v} used by {k} and {_seen[v]}'
    assert v not in ENTITY_NAMES, f'enum name {v} equals an entity class'
    _seen[v] = k

def enum_name_for(table, col): return ENUM_NAMES[(table, col)]

def gen_enum(table, col, vals):
    E = enum_name_for(table, col)
    return E, '\n'.join([f'package {BASE}.enums;', '',
        f'/** Allowed values of <b>{table}.{col}</b> (CHECK constraint in the schema). Generated by scripts/gen_entities.py. */',
        f'public enum {E} {{', '    ' + ', '.join(vals), '}', ''])

def gen_entity(t):
    name, pk = t['table_name'], t['pk']
    C = cls(name)
    enums = enums_for(t)
    imports = {'jakarta.persistence.*', 'lombok.Getter', 'lombok.Setter', 'lombok.NoArgsConstructor',
               'org.hibernate.annotations.DynamicInsert', 'org.hibernate.annotations.DynamicUpdate'}
    cols = {c['name'] for c in t['cols']}
    tenant_scoped = 'tenant_id' in cols and name != 'tenant'
    fields = []
    for c in t['cols']:
        n = c['name']; f = camel(n)
        typ, extra = jtype(c, imports)
        anns = []
        colattrs = [f'name = "{n}"']
        if not c['nullable'] and n != pk: colattrs.append('nullable = false')
        if c['udt'] == 'varchar' and c['len']: colattrs.append(f'length = {c["len"]}')
        if c['udt'] == 'bpchar' and c['len']: colattrs.append(f'length = {c["len"]}')
        if c['udt'] == 'numeric' and c['prec']: colattrs.append(f'precision = {c["prec"]}, scale = {c["scale"] or 0}')
        colattrs += extra
        if n == pk:
            anns.append('@Id')
            if c['default'] and 'gen_random_uuid' in c['default']:
                anns.append('@GeneratedValue(strategy = GenerationType.UUID)')
            colattrs.append('updatable = false')
        if n in ('date_created', 'date_updated') and c['default']:
            imports.add('org.hibernate.annotations.Generated')
            colattrs += ['insertable = false', 'updatable = false']
            anns.append('@Generated(event = org.hibernate.generator.EventType.INSERT)' if n == 'date_created'
                        else '@Generated(event = {org.hibernate.generator.EventType.INSERT, org.hibernate.generator.EventType.UPDATE})')
        if c['udt'] == 'jsonb':
            imports |= {'org.hibernate.annotations.JdbcTypeCode', 'org.hibernate.type.SqlTypes'}; anns.append('@JdbcTypeCode(SqlTypes.JSON)')
        if c['udt'] in ('inet',):
            imports |= {'org.hibernate.annotations.JdbcTypeCode', 'org.hibernate.type.SqlTypes'}; anns.append('@JdbcTypeCode(SqlTypes.INET)')
        if c['udt'].startswith('_'):
            imports |= {'org.hibernate.annotations.JdbcTypeCode', 'org.hibernate.type.SqlTypes'}; anns.append('@JdbcTypeCode(SqlTypes.ARRAY)')
        if n in enums and typ == 'String':
            typ = enum_name_for(name, n); imports.add(f'{BASE}.enums.{typ}'); anns.append('@Enumerated(EnumType.STRING)')
        anns.append(f'@Column({", ".join(colattrs)})')
        fields.append((anns, typ, f))
    lines = [f'package {BASE}.entity;', '']
    if tenant_scoped:
        imports |= {f'{BASE}.security.TenantScoped', f'{BASE}.security.TenantEntityListener'}
    lines += [f'import {i};' for i in sorted(imports)]
    lines += ['', '/**', f' * Table <b>{name}</b> ({[d for d, ts in G["groups"].items() if name in ts][0]} domain).',
              ' * Generated from the PostgreSQL catalog of the final schema (scripts/gen_entities.py); do not edit by hand.',
              ' * Columns maintained by the database (date_created, date_updated, tenant_id defaults, triggers) are read back.',
              ' */',
              '@Entity', f'@Table(name = "{name}", schema = "lms_core")', '@DynamicInsert', '@DynamicUpdate',
              '@Getter', '@Setter', '@NoArgsConstructor']
    if tenant_scoped: lines.append('@EntityListeners(TenantEntityListener.class)')
    lines.append(f'public class {C}' + (' implements TenantScoped' if tenant_scoped else '') + ' {')
    for anns, typ, f in fields:
        lines.append('')
        lines += ['    ' + a for a in anns]
        lines.append(f'    private {typ} {f};')
    lines += ['}', '']
    return C, '\n'.join(lines), tenant_scoped

def gen_repo(C):
    return '\n'.join([f'package {BASE}.repository;', '',
        f'import {BASE}.entity.{C};', 'import org.springframework.data.jpa.repository.JpaRepository;',
        'import org.springframework.stereotype.Repository;', '', 'import java.util.UUID;', '',
        '/** Generated repository; add custom queries in a hand-written repository if needed. */',
        '@Repository', f'public interface {C}Repository extends JpaRepository<{C}, UUID> {{', '}', ''])

# Repositories with hand-written queries; the generator never overwrites these.
HANDWRITTEN_REPOS = {'users', 'user_profile', 'platform_user_profile', 'student_profile', 'employee_profile',
                     'academic_advisor_profile', 'observer_student_link', 'roles', 'permissions', 'role_permissions',
                     'user_role', 'tenant', 'tenant_domain', 'tenant_settings', 'institution', 'platform_support_access',
                     # academic structure + enrollment
                     'org_unit', 'academic_term', 'course_category', 'course', 'course_section', 'course_instructor',
                     'enrollment', 'waitlist', 'group_set', 'student_group', 'group_members',
                     # content + modules
                     'content_type', 'content', 'video_lecture', 'course_module', 'module_prerequisite',
                     'module_completion_requirement', 'student_module_progress', 'student_content_progress',
                     # assignments
                     'assignment', 'assignment_submission', 'feedback', 'assignment_override', 'assignment_peer_review',
                     'comment_library_item',
                     # tenancy + identity, complete
                     'tenant_branding', 'tenant_subscription', 'tenant_user_limit', 'user_session', 'login_history',
                     'password_reset_token', 'mfa_configuration', 'email_verification_token', 'account_lock', 'audit_log',
                     'user_accessibility_settings', 'user_preferences', 'accommodation', 'user_login',
                     # academic structure, complete
                     'course_prerequisite', 'course_schedule', 'learning_outcome', 'outcome_result', 'gradebook', 'blueprint_association', 'blueprint_content_lock', 'blueprint_sync_run', 'course_navigation_item',
                     # enrollment, complete
                     'group_leader', 'differentiation_tag', 'differentiation_tag_member', 'observer_pairing_code',
                     # learning content, complete
                     'content_block', 'interactive_content', 'content_permission', 'system_resource_library', 'release_condition', 'mastery_path_rule', 'mastery_path_range', 'mastery_path_set_item', 'course_pace', 'course_pace_item'}
import json as _json
for d in ('entity', 'repository', 'enums'): os.makedirs(f'{OUT}/{d}', exist_ok=True)
n = e = 0
legacy = {}                # Entity.NestedName -> new enum name (used once to migrate hand-written code)
for t in CAT:
    C, src, ts = gen_entity(t)
    open(f'{OUT}/entity/{C}.java', 'w').write(src)
    if t['table_name'] not in HANDWRITTEN_REPOS:
        open(f'{OUT}/repository/{C}Repository.java', 'w').write(gen_repo(C))
    for col, vals in enums_for(t).items():
        E, esrc = gen_enum(t['table_name'], col, vals)
        open(f'{OUT}/enums/{E}.java', 'w').write(esrc)
        legacy[f'{C}.{base_enum_name(col)}'] = E
        e += 1
    n += 1
if len(sys.argv) > 2:
    _json.dump(legacy, open(sys.argv[2], 'w'), indent=1, sort_keys=True)
print('entities', n, 'enums', e)
