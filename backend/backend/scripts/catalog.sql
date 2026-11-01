select json_agg(t order by t.table_name) from (
 select c.relname as table_name,
  (select json_agg(json_build_object('name',a.column_name,'data_type',a.data_type,'udt',a.udt_name,'nullable',a.is_nullable='YES',
     'default',a.column_default,'len',a.character_maximum_length,'prec',a.numeric_precision,'scale',a.numeric_scale) order by a.ordinal_position)
   from information_schema.columns a where a.table_schema='lms_core' and a.table_name=c.relname) as cols,
  (select json_agg(pg_get_constraintdef(k.oid)) from pg_constraint k where k.conrelid=c.oid and k.contype='c') as checks,
  (select a.attname from pg_constraint k join pg_attribute a on a.attrelid=k.conrelid and a.attnum=k.conkey[1] where k.conrelid=c.oid and k.contype='p') as pk,
  obj_description(c.oid) as comment
 from pg_class c join pg_namespace n on n.oid=c.relnamespace where n.nspname='lms_core' and c.relkind='r') t;
