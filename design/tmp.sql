drop view if exists sampling_unit_count_view;
create or replace view sampling_unit_count_view as
select 
    u.aoi_id,
    a.parent_aoi_id,
    u.workspace_id,
    count(*)
from 
    calc.sampling_unit_aoi u
inner join calc.aoi a
    on u.aoi_id = a.id
group by
    aoi_id,
    a.parent_aoi_id,
    workspace_id;
        