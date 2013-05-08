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
    h.region_id,
    aoi.aoi_label
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
    h.zone_id,
    aoi.aoi_label
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
    h.country_id,
    aoi.aoi_label
;
COMMENT ON TABLE _households_per_country IS 'No. of households interviewed within at least 10km of forest, by region';

