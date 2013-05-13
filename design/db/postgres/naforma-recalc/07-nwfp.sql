SET search_path TO naforma1;

--drop table if exists _human_impact_occurrence;
--create table _human_impact_occurrence
--as
--select distinct
--	ht.code as human_impact,
--	h.plot_id
--from
--	human_impact h
--inner join
--	human_impact_type ht
--on
--	
--    h.human_impact_id = ht.human_impact_id
;

-- fact table
drop table if exists naforma1._nwfp_occurrence cascade;
    
create table naforma1._nwfp_occurrence as 
SELECT distinct
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
    coalesce(canopy_cover_class, '-1') as canopy_cover_class, 
    coalesce(h.code, '-1') as nwfp,    
    111 as est_area,
    1 as cnt,
    1 as agg_cnt
FROM
    naforma1._plot p
INNER JOIN
    naforma1._country_stratum s
ON    
    p.stratum = s.stratum
AND
    p.country_id = s.country_id
join
    naforma1.plot_nwfp h
    on h.plot_id = p.plot_id

WHERE
     p.accessibility = '0' and p.measurement = 'P' and p.subplot = 'A'
--GROUP BY
--    p.country_id,
--    p.stratum,
--    p.vegetation_type,
--    p.land_use,
--    ownership_type,
--    undergrowth_type,
--    soil_structure,
--    soil_texture,    
--    erosion,
--    grazing,
--    catchment,
--    shrubs_coverage,
--    canopy_cover_class,    
--    h.human_impact
;

--- Country
drop table if exists naforma1._country_nwfp_agg cascade;
    
create table naforma1._country_nwfp_agg as 
SELECT
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
    canopy_cover_class, 
    p.nwfp,
    s.expf * count(*) as est_area,
    count(*) as cnt,
    count(*) as agg_cnt
FROM
    _nwfp_occurrence p
INNER JOIN
    naforma1._country_stratum s
ON    
    p.stratum = s.stratum
AND
    p.country_id = s.country_id
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
    canopy_cover_class,
    s.expf,
    p.nwfp
;    

    