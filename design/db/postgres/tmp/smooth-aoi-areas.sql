with a as (
select 
        a.aoi_parent_id,
        sum(a.aoi_land_area) as area
from
        calc.aoi a
where 
        a.aoi_parent_id is not null                        
group by
        a.aoi_parent_id
)
update 
        calc.aoi         
set 
        aoi_land_area = a.area
from
        a
where 
        aoi_id = a.aoi_parent_id;
        
with a as (
select 
        a.aoi_parent_id,
        sum(a.aoi_total_area) as area
from
        calc.aoi a
where 
        a.aoi_parent_id is not null                        
group by
        a.aoi_parent_id
)
update 
        calc.aoi         
set 
        aoi_total_area = a.area
from
        a
where 
        aoi_id = a.aoi_parent_id                        