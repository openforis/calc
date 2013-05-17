SET search_path TO naforma1, public;

-- Unique species in each plot

drop table if exists _cluster_nwfp_observed;

create table _cluster_nwfp_observed as
SELECT
    c.cluster_id as cluster_id,
    cl.code AS cluster,
    cl.center_location,
    0 as fruit_observed,
    0 as medicine_plants_observed
FROM
    naforma1.cluster c
INNER JOIN
    calc.cluster_location cl
ON
    c.id = cl.code
INNER JOIN
    naforma1._plot p
ON
    c.cluster_id = p.cluster_id
where
    p.accessibility='0' and c.measurement='P';
    
update _cluster_nwfp_observed
set fruit_observed = 1
from plot_nwfp nwfp, _plot p
where _cluster_nwfp_observed.cluster_id = p.cluster_id and 
     p.plot_id = nwfp.plot_id and
    nwfp.code='1' and p.accessibility='0';
    
update _cluster_nwfp_observed
set medicine_plants_observed = 1
from plot_nwfp nwfp, _plot p
where _cluster_nwfp_observed.cluster_id = p.cluster_id and 
     p.plot_id = nwfp.plot_id and
    nwfp.code='5' and p.accessibility='0';


