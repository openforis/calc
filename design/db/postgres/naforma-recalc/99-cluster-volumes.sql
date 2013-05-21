SET search_path TO naforma1;

drop table if exists _cluster_forest_volume;

create table _cluster_forest_volume as
select
    t.cluster,    
    cl.center_location,
    avg(t.volume) commercial_volume_per_ha
from
    _plot_tree_agg t
inner join
    calc.cluster_location cl
on
    t.cluster = cl.code        
where
    t.vegetation_type >= '100' and t.vegetation_type < '300' and t.commercial_tree = 1
group by
    t.cluster,
    cl.center_location;

alter table _cluster_forest_volume
add column growing_stock_volume_per_ha double precision;

with v as (
    select
        t.cluster,    
        avg(t.volume) as vol
    from
        _plot_tree_agg t
    inner join
        calc.cluster_location cl
    on
        t.cluster = cl.code        
    where
        t.vegetation_type >= '100' and t.vegetation_type < '300' and t.growing_stock = 1
    group by
        t.cluster )
update _cluster_forest_volume
set growing_stock_volume_per_ha = v.vol
from v
where v.cluster = _cluster_forest_volume.cluster;
