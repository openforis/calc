--select
--count(*)

delete
from
calc.specimen_numeric_value v
where
v.variable_id  in (39,59.60,143,144)
and 
v.current
and 
not v.original
;

update
calc.specimen_numeric_value v
set current = true
where
v.variable_id in (39,59.60,143,144)
;


--select
--count(*)
--from
--calc.specimen_numeric_value v
--where
--v.variable_id = 144
----  in (39,59.60,143,144)
--and 
--v.current

