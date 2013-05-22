SET search_path TO naforma1;
select * from _cluster_forest_volume2 v2 order by v2.growing_stock_volume_per_ha desc;
--
--select c.id, sum(t.volume) from _plot p
--inner join cluster c
--on c.cluster_id = p.cluster_id
--inner join _tree t
--on t.plot_id = p.plot_id
--where t.commercial_tree =1
--and p.vegetation_type < '300'
--group by c.id;
--

drop table if exists _plot_volume_summary;

create table _plot_volume_summary as
select c.id as cluster, p.no as plot, avg(t.volume) mean_vol, count(t.*) ntrees
from _plot p
inner join cluster c
on c.cluster_id = p.cluster_id
inner join _tree t
on t.plot_id = p.plot_id
where c.measurement='P'
and t.commercial_tree =1
and p.vegetation_type < '300'
group by c.id, p.no;

drop table if exists _tree_dbh_outliers ;

create table _tree_dbh_outliers as
select 
    c.id as cluster, 
    c.measurement,
    p.no as plot, 
    t.tree_no, 
    t.stem_no, 
    t.species,
    t.species_scientific_name,
    t.health,
    t.dbh,
    s.avg_dbh,
    s.stddev,
    t.total_height as obs_height,
    t.est_height,
    t.volume
from _plot p
inner join cluster c
on c.cluster_id = p.cluster_id
inner join _tree t
on t.plot_id = p.plot_id
inner join _tree_species_dbh_stats s
on s.species = t.species
where t.dbh >= (s.avg_dbh + 4*s.stddev);


select avg(dbh), stddev_samp(dbh), avg(dbh) + 2* stddev_samp(dbh) from _tree
where species not like 'ADA%';

select count(*) from _tree_dbh_outliers;

drop table if exists _tree_species_dbh_stats;

create table _tree_species_dbh_stats as
select species, avg(dbh) avg_dbh, stddev_samp(dbh) as stddev from _tree
group by species;




select count(*) 
from _plot p
inner join cluster c
on c.cluster_id = p.cluster_id
inner join _tree t
on t.plot_id = p.plot_id
inner join _tree_species_dbh_stats s
on s.species = t.species
where t.dbh >= (s.avg_dbh + 4*s.stddev);
