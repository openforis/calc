SET search_path TO naforma1;

// NOTE: Plots means planned, accessible plot centers

// No. clusters per stratum (ncluster2)
alter table _country_stratum
add column cluster_cnt integer;
 
with cnt as (
        select 
                p.stratum,
                p.country_id,
                count(distinct p.cluster_id) as count
        from
                _plot p
        where
                p.accessibility = '0' and p.measurement = 'P' and p.subplot = 'A'
        group by
                p.stratum,
                p.country_id        
)
update 
        _country_stratum
set 
        cluster_cnt = cnt.count
from
        cnt
where         
        cnt.country_id = _country_stratum.country_id
and 
        cnt.stratum = _country_stratum.stratum;


// No. plots per class per cluster (nplots_k)
drop table if exists _tmp_plots_per_class_per_cluster;

create table _tmp_plots_per_class_per_cluster as
select
    p.cluster_id,
    p.stratum,
    p.vegetation_type,
    count(*)
from
    _plot p
where
    p.accessibility = '0' and p.measurement = 'P' and p.subplot = 'A'
group by
    p.cluster_id,
    p.stratum,
    p.vegetation_type;
    
// No. plots per class per stratum (plots_k)
drop table if exists _tmp_plots_per_class_per_stratum;

create table _tmp_plots_per_class_per_stratum as
select
    stratum,
    vegetation_type,
    sum(count) as count
from
    _tmp_plots_per_class_per_cluster
group by
    stratum,
    vegetation_type;
    
// Area per class per stratum (area_k)
drop table if exists _tmp_area_per_class_per_stratum;

create table _tmp_area_per_class_per_stratum as
select
    stratum,
    vegetation_type,
    sum(est_area) as area
from
    _country_stratum_plot_agg
group by
    stratum,
    vegetation_type;
 
 // No. plots per cluster (nplots)
drop table if exists _tmp_plots_per_cluster;

create table _tmp_plots_per_cluster as
select
    cluster_id,
    stratum,
    sum(count) as count
from
    _tmp_plots_per_class_per_cluster
group by
    cluster_id,
    stratum;

// Proportion of plots per class per stratum (plots_k / no. obs. plots in stratum)
drop table if exists _tmp_plots_prop_class_per_stratum;

create table _tmp_plots_prop_class_per_stratum as 
select
    ps.stratum,
    ps.vegetation_type,
    ps.count / s.obs_plot_cnt as prop
from
    _tmp_plots_per_class_per_stratum ps
inner join
    _country_stratum s
on ps.stratum = s.stratum;

// Expected no. plots per class per cluster (proportion * nplots)
drop table if exists _tmp_exp_plots_per_class_per_cluster;

create table _tmp_exp_plots_per_class_per_cluster as 
select    
    c.cluster_id,
    s.stratum,
    s.vegetation_type,    
    s.prop * c.count as expected_cnt
from
    _tmp_plots_prop_class_per_stratum s
inner join
    _tmp_plots_per_cluster c
on
    s.stratum = c.stratum
;
    
// Residuals per class per cluster (difference in no. plots in each class observed vs. estimated)
drop table if exists _tmp_area_residuals_per_class_per_cluster;

create table _tmp_area_residuals_per_class_per_cluster as 
select    
    o.cluster_id,
    o.stratum,
    o.vegetation_type,    
    o.count - e.expected_cnt as residual
from
     _tmp_plots_per_class_per_cluster o
inner join
    _tmp_exp_plots_per_class_per_cluster e
on
    o.cluster_id = e.cluster_id and
    o.vegetation_type = e.vegetation_type    
;

// Residual variance per class per stratum
drop table if exists _tmp_area_var_per_class_per_stratum;

create table _tmp_area_var_per_class_per_stratum as 
select
    r.stratum,
    r.vegetation_type,
    var_samp(residual) var
from    
    _tmp_area_residuals_per_class_per_cluster r
group by
    r.stratum,
    r.vegetation_type
;
    
// Coefficient of variation per stratum per class
// strata$erprc <- (strata$ncluster2 * strata$residual_variance_k) / (strata$nplot2oc ^ 2)
drop table if exists _tmp_area_cv_per_class_per_stratum;

create table _tmp_area_cv_per_class_per_stratum as 
select
    r.stratum,
    r.vegetation_type,
    (s.cluster_cnt * r.var) / (s.obs_plot_cnt^2) as cv
from    
    _tmp_area_var_per_class_per_stratum r
inner join
    _country_stratum s
on
    r.stratum = s.stratum        
;

// TODO handle incomplete AOIs

// Absolute error per class 
// errors$absolute <- sqrt(sum(strata$area_k^2 * strata$erprc))

// Relative error (cv) per class = err / total area of class in AOI
// errors$relative <- errors$absolute / sum(strata$area_k)								 

