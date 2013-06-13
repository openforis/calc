set search_path to naforma1_se;

-- remove duplucates

drop table
	if exists _product_used;
create table
	_product_used as
select
	*
from
	product_used
;
	
update
	_product_used
set
	rank = 1
where
	rank is null;

with r as ( 
    select
        p.household_id,
        p.category_code_id,
        min(p.rank) as min_rank
    from
        _product_used p
    group by
        p.household_id,
        p.category_code_id
)
delete from
    naforma1_se._product_used p
where p.rank > (select r.min_rank from r where p.household_id = r.household_id and p.category_code_id = r.category_code_id)           
;


with r as ( 
    select
        p.household_id,
        p.category_code_id,
        min(p.product_used_id) as min_id
    from
        _product_used p
    group by
        p.household_id,
        p.category_code_id
)
delete from
    naforma1_se._product_used p
where p.product_used_id > (select r.min_id from r where p.household_id = r.household_id and p.category_code_id = r.category_code_id)           
;


--select
--    *
--from
--    _product_used p1
--where
--    1 < (select count(*) from _product_used p2 where p1.household_id = p2.household_id and
-- p1.category_code_id = p2.category_code_id and p1.rank = p2.rank);


-- create 
DROP TABLE IF EXISTS _household_forest_product;

create table _household_forest_product
as
select distinct
    h.*,
    p.product_used_id,
    p.category,
    p.category_code_id as forest_product_category_id,    
    p.distance_to_source,
    p.distance_to_source_code_id,
    p.harvester,
    p.harvester_code_id,
    p.harvesting_change,
    p.harvesting_change_code_id,
    p.end_use,
    p.end_use_code_id,
    p.rights,
    p.rights_code_id,
    p.conflicts,
    p.conflicts_code_id,
    p.local_rules,
    p.local_rules_code_id,
    p.leg_awareness,
    p.leg_enforcement_code_id,
    p.rank
from
    _household h
join
    _product_used p 
    on p.household_id = h.household_id
;


drop table if exists _household_forest_product_land_use;

create table _household_forest_product_land_use
as
select
    p.*,
    l.land_use_code_id
from
    naforma1_se._household_forest_product p
join
    naforma1_se.product_used_land_use l
    on l.product_used_id = p.product_used_id
;    

DROP TABLE IF EXISTS _household_forest_product_harvested;

create table _household_forest_product_harvested
as
select distinct
    h.*,
    p.product_used_id,
    p.category,
--    c.product_category_label_en,
    p.category_code_id as forest_product_category_id,    
    p.distance_to_source,
    p.distance_to_source_code_id,
    p.harvester,    
    p.harvester_code_id,
    p.harvesting_change,
    p.harvesting_change_code_id,
    p.end_use,
    p.end_use_code_id,
    p.rights,
    p.rights_code_id,
    p.conflicts,
    p.conflicts_code_id,
    p.local_rules,
    p.local_rules_code_id,
    p.leg_awareness,
    p.leg_enforcement_code_id,
    p.rank,
    (p.harvested_amount_from + p.harvested_amount_to ) / 2 as harvested_amount,
    p.harvested_unit,
    p.harvested_unit_code_id
--    ,
--    u.harvest_unit_label_en
from
    _household h
join
    _product_used p 
    on p.household_id = h.household_id
--join
--    naforma1_se.harvest_unit_code u
--    on p.harvested_unit_code_id = u.harvest_unit_code_id    
--join
--    naforma1_se.product_category_code c
--    on c.product_category_code_id = p.category_code_id    
where
    (p.category = '102' and p.harvested_unit = 'A') or 
    (p.category = '103' and p.harvested_unit = 'B')