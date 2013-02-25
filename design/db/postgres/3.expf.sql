truncate table calc.plot_expansion_factor;

insert into calc.plot_expansion_factor (aoi_id,stratum_id,exp_factor)
--with aoi_est_share as (
--        select
--                sum(a.aoi_share) as est_share
--        --        ,
--        --        a.obs_plot_cnt
--        --        ,
--        --        a.area
--        --        ,
--        --        to_char( a.obs_plot_cnt / a.area,'999,999,999,999,999.99999999999' ) as trashold
--                 ,
--                 a.aoi_id
--         --       , a.aoi_label
--        from
--                calc.aoi_stratum_view a
--        where
--                ( a.obs_plot_cnt / a.area ) >= 0.00014     
--        group by
--                a.aoi_id
--)
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
--inner join
--        aoi_est_share e on s.aoi_id = e.aoi_id
--where
--        e.est_share > 0.8          
              