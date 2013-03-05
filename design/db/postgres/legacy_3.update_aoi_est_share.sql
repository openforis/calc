update aoi
set aoi_est_share = a.est_share
from (
select
        sum(a.aoi_share) as est_share,
--         a.obs_plot_cnt / ( a.aoi_share * a.aoi_area )
         a.aoi_id
 --       , a.aoi_label
 --       , a.area / a.obs_plot_cnt / sum(a.aoi_share) as exp_factor
from
        calc.aoi_stratum_view a
where
        (a.obs_plot_cnt / a.area  ) >= 40      
group by
        a.aoi_id
 --       , a.aoi_label
 --       , a.area
 --       , a.obs_plot_cnt
--having
--        sum(a.aoi_share) >= 0.90        
) as a 
where a.aoi_id = aoi.aoi_id