truncate table calc.plot_expansion_factor;

insert into calc.plot_expansion_factor
(aoi_id, stratum_id, exp_factor)
select
        a.aoi_id,        
        s.stratum_id,
        s.area / s.obs_plot_cnt / a.aoi_est_share as exp_factor
from
        calc.aoi a
inner join 
        calc.aoi_stratum_view s on a.aoi_id = s.aoi_id        
where
        a.aoi_est_share >= 0.90
and
        s.obs_plot_cnt > 0        
        