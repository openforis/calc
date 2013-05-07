SET search_path TO naforma1, public;

--update _plot
--set vegetation_type = -1
--where vegetation_type is null;
--

--SELECT 
--    a.land_use as land_use_code,
--    a.vegetation_type as vt_code,
--    vt.category_label as vegetation_type,
--    lu.category_label as land_use,
--    round(sum(est_area)) as area
--FROM (

---------------- COUNTRY

drop table if exists naforma1._country_stratum_plot_agg;

create table naforma1._country_stratum_plot_agg as 
SELECT
    p.country_id,
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
    naforma1._country_stratum s
ON    
    p.stratum = s.stratum
AND
    p.country_id = s.country_id
WHERE
     p.accessibility = '0' and p.measurement = 'P' and p.subplot = 'A'
GROUP BY
    p.country_id,
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
    s.expf;
    
    
drop table if exists naforma1._country_plot_agg;
    
create table naforma1._country_plot_agg as 
SELECT
    p.country_id,
--    p.stratum,
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
    naforma1._country_stratum s
ON    
    p.stratum = s.stratum
AND
    p.country_id = s.country_id
WHERE
     p.accessibility = '0' and p.measurement = 'P' and p.subplot = 'A'
GROUP BY
    p.country_id,
--    p.stratum,
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
    s.expf;    
    
--) as a
--left outer join calc.category vt on vt.category_code = vegetation_type and vt.variable_id = 10
--left outer join calc.category lu on lu.category_code = land_use and lu.variable_id = 9
--GROUP BY 
--    a.land_use,
--    a.vegetation_type,
--    vt.category_label,
--    lu.category_label
--order by a.land_use, a.vegetation_type;

-------------------- ZONES

drop table if exists naforma1._zone_stratum_plot_agg;

create table naforma1._zone_stratum_plot_agg
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
    
    

drop table if exists naforma1._zone_plot_agg;

create table naforma1._zone_plot_agg
as
SELECT
    country_id,
    p.zone_id,    
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
    naforma1._zone_stratum_plot_agg p
INNER JOIN
    naforma1._zone_stratum s
ON    
    p.stratum = s.stratum
AND
    p.zone_id = s.zone_id
where
    s.obs_plot_cnt >= 30  
GROUP BY
    country_id,
    p.zone_id,    
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
    a1.aoi_parent_id as country_id,
    s.zone_id,
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
    naforma1._country_stratum_plot_agg f
join
    naforma1._zone_stratum s
    on s.stratum = f.stratum
join
    naforma1._country_stratum c
    on c.stratum = f.stratum
    and f.country_id = c.country_id
join
    calc.aoi a1
    on s.zone_id = a1.aoi_id    
where
    s.obs_plot_cnt < 30    
    
;


----------------- REGIONS


drop table if exists naforma1._region_stratum_plot_agg ;

create table naforma1._region_stratum_plot_agg 
as 
SELECT
    p.country_id,
    p.zone_id,
    p.region_id,
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
    naforma1._region_stratum s
ON    
    p.stratum = s.stratum
AND
    p.region_id = s.region_id
WHERE
    p.accessibility = '0' and p.measurement = 'P' and p.subplot = 'A'
and 
    s.obs_plot_cnt >= 30  
GROUP BY
    p.country_id,
    p.zone_id,
    p.region_id,
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
    a2.aoi_parent_id as zone_id,
    s.region_id,
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
    a.region_land_area * (s.phase1_cnt / a.phase1_cnt::double precision) as est_area
FROM
    naforma1._region_stratum s
join
    naforma1._region a
    on a.region_id = s.region_id
join
    calc.aoi a1
    on a1.aoi_id = s.region_id    
join
    calc.aoi a2
    on a2.aoi_id = a1.aoi_parent_id
WHERE
    s.obs_plot_cnt < 30  
;    
    
drop table if exists naforma1._region_plot_agg;

create table naforma1._region_plot_agg
as
SELECT
    country_id,
    zone_id,
    p.region_id,    
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
    naforma1._region_stratum_plot_agg p
INNER JOIN
    naforma1._region_stratum s
ON    
    p.stratum = s.stratum
AND
    p.region_id = s.region_id
where
    s.obs_plot_cnt >= 30  
GROUP BY
    country_id,
    zone_id,
    p.region_id,    
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
    a2.aoi_parent_id as country_id,
    a1.aoi_parent_id as zone_id,
    s.region_id,
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
    naforma1._country_stratum_plot_agg f
join
    naforma1._region_stratum s
    on s.stratum = f.stratum
join
    naforma1._country_stratum c
    on c.stratum = f.stratum
    and f.country_id = c.country_id
join
    calc.aoi a1
    on s.region_id = a1.aoi_id    
join
    calc.aoi a2
    on a1.aoi_parent_id = a2.aoi_id
where
    s.obs_plot_cnt < 30    

;


-------------- DISTRICTS

drop table if exists naforma1._district_stratum_plot_agg ;

create table naforma1._district_stratum_plot_agg 
as 
SELECT
    p.country_id,
    p.zone_id,
    p.region_id,
    p.district_id,
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
    naforma1._district_stratum s
ON    
    p.stratum = s.stratum
AND
    p.district_id = s.district_id
WHERE
    p.accessibility = '0' and p.measurement = 'P' and p.subplot = 'A'
and 
    s.obs_plot_cnt >= 30  
GROUP BY
    p.country_id,
    p.zone_id,
    p.region_id,
    p.district_id,
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
    a3.aoi_parent_id as country_id,
    a2.aoi_parent_id as zone_id,
    a1.aoi_parent_id as region_id,
    s.district_id,
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
    a.district_land_area * (s.phase1_cnt / a.phase1_cnt::double precision) as est_area
FROM
    naforma1._district_stratum s
join
    naforma1._district a
    on a.district_id = s.district_id
join
    calc.aoi a1
    on a1.aoi_id = s.district_id    
join
    calc.aoi a2
    on a2.aoi_id = a1.aoi_parent_id
join
    calc.aoi a3
    on a3.aoi_id = a2.aoi_parent_id    
WHERE
    s.obs_plot_cnt < 30  
;    
    
    
    
drop table if exists naforma1._district_plot_agg;

create table naforma1._district_plot_agg
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
    naforma1._district_stratum_plot_agg p
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
    naforma1._country_stratum_plot_agg f
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