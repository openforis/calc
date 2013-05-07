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
drop table if exists naforma1._agg_country_stratum_plot_fact;

create table naforma1._agg_country_stratum_plot_fact as 
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
    
    
drop table if exists naforma1._agg_country_plot_fact;
    
create table naforma1._agg_country_plot_fact as 
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