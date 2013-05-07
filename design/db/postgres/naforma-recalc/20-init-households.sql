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
