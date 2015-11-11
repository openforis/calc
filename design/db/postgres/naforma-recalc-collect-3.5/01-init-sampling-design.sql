SET search_path TO naforma1, public;

------------------------------------------------------------
--- SAMPLING DESIGN
------------------------------------------------------------

-- Copy sampling_design table
DROP TABLE IF EXISTS _sampling_design;

CREATE TABLE _sampling_design AS 
SELECT * 
FROM calc.naforma_sampling_design;

-- Convert theoretical plot locations to PostGIS points in WGS84 
ALTER TABLE _sampling_design
ADD COLUMN location Geometry(Point,4326);

UPDATE _sampling_design
SET location = ST_Transform(ST_SetSRID(ST_Point(x, y), srid), 4326);

-- Assign district to sampling design
ALTER TABLE _sampling_design
ADD COLUMN district_id INTEGER;

UPDATE _sampling_design
SET district_id = a.aoi_id
FROM calc.aoi a 
WHERE ST_Contains(a.aoi_shape, location)
AND a.aoi_hierarchy_level_id = 3;

-- Report unmatched plots
SELECT phase, COUNT(*)
FROM _sampling_design 
WHERE district_id IS NULL 
GROUP BY phase;

-- Assign region to sampling design
ALTER TABLE _sampling_design
ADD COLUMN region_id INTEGER;

UPDATE _sampling_design
SET region_id = a.aoi_parent_id
FROM calc.aoi a 
WHERE a.aoi_id = district_id;

-- Assign zone to sampling design
ALTER TABLE _sampling_design
ADD COLUMN zone_id INTEGER;

UPDATE _sampling_design
SET zone_id = a.aoi_parent_id
FROM calc.aoi a 
WHERE a.aoi_id = region_id;

-- Assign country to sampling design
ALTER TABLE _sampling_design
ADD COLUMN country_id INTEGER;

UPDATE _sampling_design
SET country_id = a.aoi_parent_id
FROM calc.aoi a 
WHERE a.aoi_id = zone_id;

-- Report unmatched plots
SELECT phase, COUNT(*)
FROM _sampling_design 
WHERE country_id IS NULL 
GROUP BY phase;