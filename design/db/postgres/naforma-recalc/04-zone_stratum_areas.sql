drop table if exists naforma1._agg_zone_stratum_plot_fact ;

create table naforma1._agg_zone_stratum_plot_fact 
as 
SELECT
    p.country_id,
    p.zone_id,
    p.stratum,
    coalesce(p.vegetation_type, '-1') as vegetation_type,
    coalesce(p.land_use, '-1') as land_use,
    coalesce(ownership_type, '-1') as ownership_type,
    coalesce(undergrowth_type, '-1') as undergrowth_type,
    coalesce(soil_structure, '-1') as soil_structure,
    coalesce(soil_texture, '-1') as soil_texture,    
    coalesce(erosion, '-1') as erosion,
    coalesce(grazing, '-1') as grazing,
    coalesce(catchment, '-1') as catchment,
    coalesce(shrubs_coverage, '-1') as shrubs_coverage,      
    s.expf * count(*) as est_area
FROM
    naforma1._plot p
INNER JOIN
    naforma1._zone_stratum s
ON    
    p.stratum = s.stratum
AND
    p.zone_id = s.zone_id
WHERE
    p.accessibility = '0' and p.measurement = 'P' and p.subplot = 'A'
and 
    s.obs_plot_cnt >= 30  
GROUP BY
    p.country_id,
    p.zone_id,
    p.stratum,
    p.vegetation_type,
    p.land_use,
    ownership_type,
    undergrowth_type,
    soil_structure,
    soil_texture,    
    erosion,
    grazing,
    catchment,
    shrubs_coverage,
    s.expf
    
UNION    
    
SELECT
    a1.aoi_parent_id as country_id,
    s.zone_id,
    s.stratum,
    '-1' as vegetation_type,
    '-1' as land_use,
    '-1' as ownership_type,
    '-1' as undergrowth_type,
    '-1' as soil_structure,
    '-1' as soil_texture,    
    '-1' as erosion,
    '-1' as grazing,
    '-1' as catchment,
    '-1' as shrubs_coverage,          
    a.zone_land_area * (s.phase1_cnt / a.phase1_cnt::double precision) as est_area
FROM
    naforma1._zone_stratum s
join
    naforma1._zone a
    on a.zone_id = s.zone_id
join
    calc.aoi a1
    on a1.aoi_id = s.zone_id    
WHERE
    s.obs_plot_cnt < 30  
;    
    
    