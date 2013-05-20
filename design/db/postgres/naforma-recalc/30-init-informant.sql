SET search_path TO naforma1_se, public;

------------------------------------------------------------
--- INFORMANT INTERVIEW
------------------------------------------------------------

---- Dimension tables
DROP TABLE if exists _household_count_code;
CREATE TABLE _household_count_code (code CHARACTER VARYING NOT NULL, label CHARACTER VARYING NOT NULL);
INSERT INTO _household_count_code (code, label) VALUES ('1', '<=1');
INSERT INTO _household_count_code (code, label) VALUES ('2', '<=10');
INSERT INTO _household_count_code (code, label) VALUES ('3', '<=100');
INSERT INTO _household_count_code (code, label) VALUES ('4', '<=1000');
INSERT INTO _household_count_code (code, label) VALUES ('5', '<=10000');
INSERT INTO _household_count_code (code, label) VALUES ('6', '<=100000');



-- Copy table
DROP TABLE IF EXISTS _informant;

CREATE TABLE _informant AS 
SELECT * 
FROM informant;

alter table _informant
add column cnt integer;

update _informant
set cnt = 1;

ALTER TABLE _informant
ADD COLUMN location_x integer;

ALTER TABLE _informant
ADD COLUMN location_y integer;

with l as (
select
    i.informant_id,
    l.center_x,
    l.center_y
from
    naforma1_se.informant i
join
    naforma1.cluster c
    on i.cluster_id = c.cluster_id    
    
join
    calc.cluster_location l
    on c.id = l.code
)
update _informant i
set location_x = l.center_x, location_y = l.center_y
from l where l.informant_id = i.informant_id;

-- Convert household locations to PostGIS points in WGS84 
ALTER TABLE _informant
ADD COLUMN location Geometry(Point,4326);

UPDATE _informant
SET location = ST_Transform(
        ST_SetSRID(ST_Point(location_x, location_y), '32736')
        , 4326);

-- Assign district to households
ALTER TABLE _informant
ADD COLUMN district_id INTEGER;

UPDATE _informant
SET district_id = a.aoi_id
FROM calc.aoi a 
WHERE ST_Contains(a.aoi_shape, location)
AND a.aoi_hierarchy_level_id = 3;

-- Report unmatched households
SELECT count(*)
FROM _informant 
WHERE district_id IS NULL;

-- Assign region to households
ALTER TABLE _informant
ADD COLUMN region_id INTEGER;

UPDATE _informant
SET region_id = a.aoi_parent_id
FROM calc.aoi a 
WHERE a.aoi_id = district_id;

-- Assign zone to households
ALTER TABLE _informant
ADD COLUMN zone_id INTEGER;

UPDATE _informant
SET zone_id = a.aoi_parent_id
FROM calc.aoi a 
WHERE a.aoi_id = region_id;

-- Assign country to households
ALTER TABLE _informant
ADD COLUMN country_id INTEGER;

--UPDATE _informant
--SET country_id = 335;

UPDATE _informant
SET country_id = a.aoi_parent_id
FROM calc.aoi a 
WHERE a.aoi_id = zone_id;

-- Report unmatched plots
SELECT COUNT(*)
FROM _informant
WHERE country_id IS NULL;

-- Household counts per AOI
--
--DROP TABLE IF EXISTS _households_per_district CASCADE;
--
--CREATE TABLE _households_per_district AS 
--SELECT 
--    h.district_id,
--    aoi.aoi_label AS district_name,
--    count(*) as household_cnt
--FROM
--    _household h
--INNER JOIN
--    calc.aoi ON aoi.aoi_id = h.district_id
--WHERE
--    h.distance_to_forest >= 10
--GROUP BY
--    h.district_id,
--    aoi.aoi_label
--;
--COMMENT ON TABLE _households_per_district IS 'No. of households interviewed within at least 10km of forest, by district';
--
--DROP TABLE IF EXISTS _households_per_region CASCADE;
--
--CREATE TABLE _households_per_region AS 
--SELECT 
--    h.region_id,
--    aoi.aoi_label AS region_name,
--    count(*) as household_cnt
--FROM
--    _household h
--INNER JOIN
--    calc.aoi ON aoi.aoi_id = h.region_id
--WHERE
--    h.distance_to_forest >= 10
--GROUP BY
--    h.region_id,
--    aoi.aoi_label
--;
--COMMENT ON TABLE _households_per_region IS 'No. of households interviewed within at least 10km of forest, by region';
--
--
--DROP TABLE IF EXISTS _households_per_zone CASCADE;
--
--CREATE TABLE _households_per_zone AS 
--SELECT 
--    h.zone_id,
--    aoi.aoi_label AS zone_name,
--    count(*) as household_cnt
--FROM
--    _household h
--INNER JOIN
--    calc.aoi ON aoi.aoi_id = h.zone_id
--WHERE
--    h.distance_to_forest >= 10
--GROUP BY
--    h.zone_id,
--    aoi.aoi_label
--;
--COMMENT ON TABLE _households_per_zone IS 'No. of households interviewed within at least 10km of forest, by zone';
--
--
--DROP TABLE IF EXISTS _households_per_country CASCADE;
--
--CREATE TABLE _households_per_country AS 
--SELECT 
--    h.country_id,
--    aoi.aoi_label AS country_name,
--    count(*) as household_cnt
--FROM
--    _household h
--INNER JOIN
--    calc.aoi ON aoi.aoi_id = h.country_id
--WHERE
--    h.distance_to_forest >= 10
--GROUP BY
--    h.country_id,
--    aoi.aoi_label
--;
--COMMENT ON TABLE _households_per_country IS 'No. of households interviewed within at least 10km of forest, by region';
--

alter table _informant
add column household_count_code varchar;

update _informant 
set household_count_code =
     case
        when household_count <= 1 then '1'
        when household_count <= 10 then '2'
        when household_count <= 100 then '3'
        when household_count <= 1000 then '4'
        when household_count <= 10000 then '5'
        when household_count <= 100000 then '6'
     end 
;

