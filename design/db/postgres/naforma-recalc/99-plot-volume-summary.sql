SET search_path TO naforma1;
---- _cluster_forest_volume2?!
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
select c.id as cluster, p.no as plot, sum(t.volume) vol, count(t.*) ntrees
from _plot p
inner join cluster c
on c.cluster_id = p.cluster_id
inner join _tree t
on t.plot_id = p.plot_id
where c.measurement='P'
group by c.id, p.no;