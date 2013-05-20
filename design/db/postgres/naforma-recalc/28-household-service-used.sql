set search_path to naforma1_se;

drop table if exists _household_service_used;

create table _household_service_used
as
select 
    h.*,
    s.type_code_id,        
    s.payment_source_code_id
from

    _household h 
join
    naforma1_se.service_used s
    on h.household_id = s.household_id  
    and s.exists = 'TRUE'
;

with c as (
    select 
        c.payment_source_code_id
    from
        payment_source_code c
    where
        c.payment_source = '0'
)
update 
    _household_service_used h
set
    payment_source_code_id = c.payment_source_code_id   
from
    c
where
   h.payment_source_code_id = -1
;
                  
select distinct 
    payment_source_code_id
from
    _household_service_used
where
    payment_source_code_id = -1
;   
