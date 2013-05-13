SET search_path TO naforma1, public;

select 
 cluster, no, measurement, sum(share), count(plot_id) as no_subplots
from _plot
group by cluster,no,measurement
having sum(share)!=100
order by cluster, no;

select max(share), min(share) from _plot;

select 
 cluster, no, measurement,subplot, share
from _plot
where share<10
order by cluster, no, subplot;
