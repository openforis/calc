SET search_path TO naforma1;


--- Environmental services used


DROP TABLE IF EXISTS _service_used CASCADE;

CREATE TABLE _service_used AS
SELECT DISTINCT
    svc.household_id,
    svc.forest_service_code_id,
    svc.type as forest_service_code
FROM
    service_used svc
WHERE
    svc.exists;


DROP TABLE IF EXISTS _service_used_by_aoi CASCADE;

CREATE TABLE _service_used_by_aoi AS
SELECT 
    svc.forest_service_code,
    cat.forest_service_label_en AS forest_service,
    hh.district_id,
    hh.region_id,
    hh.zone_id,
    hh.country_id,
    count(*) as household_cnt
FROM
    _service_used svc
INNER JOIN
    _household hh
ON
    svc.household_id = hh.household_id
INNER JOIN
    forest_service_code cat
ON
    svc.forest_service_code_id = cat.forest_service_code_id
WHERE
    hh.distance_to_forest >= 10
GROUP BY
    svc.forest_service_code,
    cat.forest_service_label_en,
    hh.district_id,
    hh.region_id,
    hh.zone_id,
    hh.country_id
    ;
    
COMMENT ON TABLE _service_used_by_aoi IS 'No. of households within at least 10km of forest using each forest service by AOI';



DROP TABLE IF EXISTS _service_used_by_region CASCADE;

CREATE TABLE _service_used_by_region AS
SELECT 
    svc.forest_service_code,
    svc.forest_service,
    svc.region_id,
    aoi.aoi_label AS region_name,
    sum(svc.household_cnt) as household_cnt,
    sum(svc.household_cnt) / cnt.household_cnt::double precision as proportion
FROM
    _service_used_by_aoi svc
INNER JOIN
    calc.aoi
ON
    svc.region_id = calc.aoi.aoi_id
INNER JOIN
    _households_per_region cnt
ON
    svc.region_id = cnt.region_id
GROUP BY
    svc.forest_service_code,
    svc.forest_service,
    svc.region_id,
    aoi.aoi_label,
    cnt.household_cnt 
    ;
COMMENT ON TABLE _service_used_by_region IS 'No. and % of households within at least 10km of forest using each forest service, by region';
   
