truncate table calc.plot_expansion_factor;

insert into calc.plot_expansion_factor (aoi_id,stratum_id,exp_factor)
select
        s.aoi_id,
        s.stratum_id,
--        e.est_share,
        case 
                when s.obs_plot_cnt > 0 
                        then s.area / s.obs_plot_cnt 
--                        / e.est_share
                else
                        0
        end as expf                                 
from
        calc.aoi_stratum_view s              