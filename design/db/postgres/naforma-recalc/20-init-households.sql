SET search_path TO naforma1, public;

------------------------------------------------------------
--- FIELD PLOTS
------------------------------------------------------------

-- Copy plot table
DROP TABLE IF EXISTS _household;

CREATE TABLE _household AS 
SELECT * 
FROM household;

alter table _household
drop column district;


-- Convert theoretical plot locations to PostGIS points in WGS84 
ALTER TABLE _household
ADD COLUMN location Geometry(Point,4326);

UPDATE _household
SET location = ST_Transform(
        ST_SetSRID(ST_Point(location_x, location_y), substring(location_srs from '[0-9]+$')::integer)
        , 4326);

-- Assign district to field plots
ALTER TABLE _household
ADD COLUMN district_id INTEGER;

UPDATE _household
SET district_id = a.aoi_id
FROM calc.aoi a 
WHERE ST_Contains(a.aoi_shape, location)
AND a.aoi_hierarchy_level_id = 3;

-- Report unmatched plots
SELECT count(*)
FROM _household 
WHERE district_id IS NULL;

-- Assign region to field plots
ALTER TABLE _household
ADD COLUMN region_id INTEGER;

UPDATE _household
SET region_id = a.aoi_parent_id
FROM calc.aoi a 
WHERE a.aoi_id = district_id;

-- Assign zone to field plots
ALTER TABLE _household
ADD COLUMN zone_id INTEGER;

UPDATE _household
SET zone_id = a.aoi_parent_id
FROM calc.aoi a 
WHERE a.aoi_id = region_id;

-- Assign country to field plots
ALTER TABLE _household
ADD COLUMN country_id INTEGER;

UPDATE _household
SET country_id = a.aoi_parent_id
FROM calc.aoi a 
WHERE a.aoi_id = zone_id;

-- Report unmatched plots
SELECT COUNT(*)
FROM _household
WHERE country_id IS NULL;

-- Household counts per AOI

DROP TABLE IF EXISTS _households_per_district CASCADE;

CREATE TABLE _households_per_district AS 
SELECT 
    h.district_id,
    aoi.aoi_label AS district_name,
    count(*) as household_cnt
FROM
    _household h
INNER JOIN
    calc.aoi ON aoi.aoi_id = h.district_id
WHERE
    h.distance_to_forest >= 10
GROUP BY
    h.district_id,
    aoi.aoi_label
;
COMMENT ON TABLE _households_per_district IS 'No. of households interviewed within at least 10km of forest, by district';

DROP TABLE IF EXISTS _households_per_region CASCADE;

CREATE TABLE _households_per_region AS 
SELECT 
    h.region_id,
    aoi.aoi_label AS region_name,
    count(*) as household_cnt
FROM
    _household h
INNER JOIN
    calc.aoi ON aoi.aoi_id = h.region_id
WHERE
    h.distance_to_forest >= 10
GROUP BY
    h.region_id
;
COMMENT ON TABLE _households_per_region IS 'No. of households interviewed within at least 10km of forest, by region';


DROP TABLE IF EXISTS _households_per_zone CASCADE;

CREATE TABLE _households_per_zone AS 
SELECT 
    h.zone_id,
    aoi.aoi_label AS zone_name,
    count(*) as household_cnt
FROM
    _household h
INNER JOIN
    calc.aoi ON aoi.aoi_id = h.zone_id
WHERE
    h.distance_to_forest >= 10
GROUP BY
    h.zone_id
;
COMMENT ON TABLE _households_per_zone IS 'No. of households interviewed within at least 10km of forest, by zone';


DROP TABLE IF EXISTS _households_per_country CASCADE;

CREATE TABLE _households_per_country AS 
SELECT 
    h.country_id,
    aoi.aoi_label AS country_name,
    count(*) as household_cnt
FROM
    _household h
INNER JOIN
    calc.aoi ON aoi.aoi_id = h.country_id
WHERE
    h.distance_to_forest >= 10
GROUP BY
    h.country_id
;
COMMENT ON TABLE _households_per_country IS 'No. of households interviewed within at least 10km of forest, by region';


-- Forest product usage matrix by AOI

DROP TABLE IF EXISTS _product_used CASCADE;

CREATE TABLE _product_used AS
SELECT DISTINCT
    p.household_id,
    p.category,
    p.product_category_code_id
FROM
    product_used p;


DROP TABLE IF EXISTS _product_used_by_aoi CASCADE;

CREATE TABLE _product_used_by_aoi AS
SELECT 
    p.category AS category_code,
    c.product_category_label_en AS category,
    h.district_id,
    h.region_id,
    h.zone_id,
    h.country_id,
    count(*) as household_cnt
FROM
    _product_used p
INNER JOIN
    _household h
ON
    p.household_id = h.household_id
INNER JOIN
    product_category_code c
ON
    p.product_category_code_id = c.product_category_code_id
WHERE
    h.distance_to_forest >= 10
GROUP BY
    p.category,
    c.product_category_label_en,
    h.district_id,
    h.region_id,
    h.zone_id,
    h.country_id
    ;
    
COMMENT ON TABLE _product_used_by_aoi IS 'No. of households within at least 10km of forest using each forest product by AOI';
   



DROP TABLE IF EXISTS _product_used_by_district CASCADE;

CREATE TABLE _product_used_by_district AS
SELECT 
    p.category_code,
    p.category,
    p.district_id,
    aoi.aoi_label AS district_name,
    sum(p.household_cnt) as household_cnt,
    sum(p.household_cnt) / cnt.household_cnt::double precision as proportion
FROM
    _product_used_by_aoi p
INNER JOIN
    calc.aoi
ON
    p.district_id = calc.aoi.aoi_id
INNER JOIN
    _households_per_district cnt
ON
    p.district_id = cnt.district_id        
GROUP BY
    p.category_code,
    p.category,
    p.district_id,
    aoi.aoi_label,
    cnt.household_cnt 
    ;

COMMENT ON TABLE _product_used_by_district IS 'No. and % of households within at least 10km of forest using each forest product, by district';
   

DROP TABLE IF EXISTS _product_used_by_region CASCADE;

CREATE TABLE _product_used_by_region AS
SELECT 
    p.category_code,
    p.category,
    p.region_id,
    aoi.aoi_label AS region_name,
    sum(p.household_cnt) as household_cnt,
    sum(p.household_cnt) / cnt.household_cnt::double precision as proportion
FROM
    _product_used_by_aoi p
INNER JOIN
    calc.aoi
ON
    p.region_id = calc.aoi.aoi_id
INNER JOIN
    _households_per_region cnt
ON
    p.region_id = cnt.region_id
GROUP BY
    p.category_code,
    p.category,
    p.region_id,
    aoi.aoi_label,
    cnt.household_cnt 
    ;
COMMENT ON TABLE _product_used_by_region IS 'No. and % of households within at least 10km of forest using each forest product, by region';
   


DROP TABLE IF EXISTS _product_used_by_zone CASCADE;

CREATE TABLE _product_used_by_zone AS
SELECT 
    p.category_code,
    p.category,
    p.zone_id,
    aoi.aoi_label AS zone_name,
    sum(p.household_cnt) as household_cnt,
    sum(p.household_cnt) / cnt.household_cnt::double precision as proportion
FROM
    _product_used_by_aoi p
INNER JOIN
    calc.aoi
ON
    p.zone_id = calc.aoi.aoi_id
INNER JOIN
    _households_per_zone cnt
ON
    p.zone_id = cnt.zone_id
GROUP BY
    p.category_code,
    p.category,
    p.zone_id,
    aoi.aoi_label,
    cnt.household_cnt 
    ;
COMMENT ON TABLE _product_used_by_zone IS 'No. and % of households within at least 10km of forest using each forest product, by zone';
   




DROP TABLE IF EXISTS _product_used_by_country CASCADE;

CREATE TABLE _product_used_by_country AS
SELECT 
    p.category_code,
    p.category,
    sum(p.household_cnt) as household_cnt,
    sum(p.household_cnt) / cnt.household_cnt::double precision as proportion
FROM
    _product_used_by_aoi p
INNER JOIN
    _households_per_country cnt
ON
    p.country_id = cnt.country_id
GROUP BY
    p.category_code,
    p.category,
    cnt.household_cnt 
    ;
COMMENT ON TABLE _product_used_by_country IS 'Total no. and % of households within at least 10km of forest using each forest product';
   


-- Household income matrix by source and AOI

DROP TABLE IF EXISTS _income_source_used CASCADE;

CREATE TABLE _income_source_used AS
SELECT DISTINCT
    src.household_id,
    src.income_source_code_id,
    src.type as income_source_code
FROM
    income_source src
WHERE
    src.cash_income;


DROP TABLE IF EXISTS _income_source_used_by_aoi CASCADE;

CREATE TABLE _income_source_used_by_aoi AS
SELECT 
    src.income_source_code,
    cat.income_source_label_en AS income_source,
    hh.district_id,
    hh.region_id,
    hh.zone_id,
    hh.country_id,
    count(*) as household_cnt
FROM
    _income_source_used src
INNER JOIN
    _household hh
ON
    src.household_id = hh.household_id
INNER JOIN
    income_source_code cat
ON
    src.income_source_code_id = cat.income_source_code_id
WHERE
    hh.distance_to_forest >= 10
GROUP BY
    src.income_source_code,
    cat.income_source_label_en,
    hh.district_id,
    hh.region_id,
    hh.zone_id,
    hh.country_id
    ;
    
COMMENT ON TABLE _income_source_used_by_aoi IS 'No. of households within at least 10km of forest using each income source by AOI';



DROP TABLE IF EXISTS _income_source_used_by_region CASCADE;

CREATE TABLE _income_source_used_by_region AS
SELECT 
    src.income_source_code,
    src.income_source,
    src.region_id,
    aoi.aoi_label AS region_name,
    sum(src.household_cnt) as household_cnt,
    sum(src.household_cnt) / cnt.household_cnt::double precision as proportion
FROM
    _income_source_used_by_aoi src
INNER JOIN
    calc.aoi
ON
    src.region_id = calc.aoi.aoi_id
INNER JOIN
    _households_per_region cnt
ON
    src.region_id = cnt.region_id
GROUP BY
    src.income_source_code,
    src.income_source,
    src.region_id,
    aoi.aoi_label,
    cnt.household_cnt 
    ;
COMMENT ON TABLE _income_source_used_by_region IS 'No. and % of households within at least 10km of forest using each income source, by region';
   










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
   
