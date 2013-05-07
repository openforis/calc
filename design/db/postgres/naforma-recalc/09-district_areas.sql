drop table if exists naforma1._agg_district_plot_fact;

create table naforma1._agg_district_plot_fact
as
SELECT
    country_id,
    zone_id,
    region_id,    
    p.district_id,
    vegetation_type,
    land_use,
    ownership_type,
    undergrowth_type,
    soil_structure,
    soil_texture,    
    erosion,
    grazing,
    catchment,
    shrubs_coverage,      
    sum(est_area) as est_area
FROM
    naforma1._agg_district_stratum_plot_fact p
INNER JOIN
    naforma1._district_stratum s
ON    
    p.stratum = s.stratum
AND
    p.district_id = s.district_id
where
    s.obs_plot_cnt >= 30  
GROUP BY
    country_id,
    zone_id,
    region_id,    
    p.district_id,
    vegetation_type,
    land_use,
    ownership_type,
    undergrowth_type,
    soil_structure,
    soil_texture,    
    erosion,
    grazing,
    catchment,
    shrubs_coverage
    
union

select
    a3.aoi_parent_id as country_id,
    a2.aoi_parent_id as zone_id,
    a1.aoi_parent_id as region_id,
    s.district_id,
    f.vegetation_type,
    f.land_use,
    f.ownership_type,
    f.undergrowth_type,
    f.soil_structure,
    f.soil_texture,    
    f.erosion,
    f.grazing,
    f.catchment,
    f.shrubs_coverage,
    ( s.phase1_cnt / c.phase1_cnt::double precision ) * f.est_area as est_area
 
from
    naforma1._agg_country_stratum_plot_fact f
join
    naforma1._district_stratum s
    on s.stratum = f.stratum
join
    naforma1._country_stratum c
    on c.stratum = f.stratum
    and f.country_id = c.country_id
join
    calc.aoi a1
    on s.district_id = a1.aoi_id    
join
    calc.aoi a2
    on a1.aoi_parent_id = a2.aoi_id
join
    calc.aoi a3
    on a2.aoi_parent_id = a3.aoi_id    
where
    s.obs_plot_cnt < 30    

;