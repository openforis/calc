SET search_path TO naforma1, public;

------------------------------------------------------------
--- SAMPLING DESIGN
------------------------------------------------------------

-- Copy sampling_design table
DROP TABLE IF EXISTS _sampling_design;

CREATE TABLE _sampling_design AS 
SELECT * 
FROM sampling_design;

-- Convert theoretical plot locations to PostGIS points in WGS84 
ALTER TABLE _sampling_design
ADD COLUMN location Geometry(Point,4326);

UPDATE _sampling_design
SET location = ST_Transform(ST_SetSRID(ST_Point(x, y), (21000+utm_zone)::integer), 4326);

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

------------------------------------------------------------
--- FIELD PLOTS
------------------------------------------------------------

-- Copy plot table
DROP TABLE IF EXISTS _plot;

CREATE TABLE _plot AS 
SELECT * 
FROM plot;

alter table _plot
drop column district,
drop column region;

-- CLEAN: Set default direction and distance
update _plot
set centre_dir = 0, centre_dist = 0
where (centre_dir=99 and centre_dist=99) 
        or centre_dir is null or centre_dist is null
        or centre_dist=0;

-- CLEAN: Set plots with observations as accessible
update _plot
set accessibility = '0'
where 
        accessibility != '0'
    and
        exists (select tree_id from tree t where t.plot_id = _plot.plot_id);

update _plot
set accessibility = '0'
where 
        accessibility != '0'
    and
        exists (select stump_id from stump s where s.plot_id = _plot.plot_id);

update _plot
set accessibility = '0'
where 
        accessibility != '0'
    and
        exists (select dead_wood_id from dead_wood dw where dw.plot_id = _plot.plot_id);

update _plot
set accessibility = '0'
where 
        accessibility != '0'
    and
        exists (select bamboo_id from bamboo b where b.plot_id = _plot.plot_id);

-- Assign cluster code and visit type to field plots 
ALTER TABLE _plot
ADD COLUMN cluster VARCHAR(255),
ADD COLUMN measurement VARCHAR(255);

UPDATE _plot
SET cluster = c.id, measurement = c.measurement
FROM cluster c
WHERE c.cluster_id = _plot.cluster_id;

-- CLEAN: Set default subplot code to 'A'
update _plot
set subplot = 'A'
where subplot is null;

-- CLEAN: Set default share to 100%
update _plot
set share = 100
where share is null;

-- CLEAN: Set vegtype of cluster in Kili incorrectly marked as Mangrove Forest to Forest: Humid montane
UPDATE _plot
SET vegetation_type = '101'
WHERE cluster = '188_69';

-- Convert theoretical plot locations to PostGIS points in WGS84 
ALTER TABLE _plot
ADD COLUMN location Geometry(Point,4326);

UPDATE _plot
SET location = ST_Transform(
        ST_SetSRID(ST_Point(location_x, location_y), substring(location_srs from '[0-9]+$')::integer)
        , 4326);

-- Calculate actual plot center using GPS reading and reported dir and dist
ALTER TABLE _plot
ADD COLUMN centre_location Geometry(Point,4326);

UPDATE _plot
SET centre_location = ST_Project( Geography(location), centre_dist, radians(centre_dir) )::geometry;

-- Assign stratum to field plots 
ALTER TABLE _plot
ADD COLUMN stratum INTEGER;

UPDATE _plot
SET stratum = s.stratum
FROM _sampling_design s
WHERE s.cluster = _plot.cluster AND s.plot::varchar = _plot.no;

-- Report unknown plots
SELECT cluster, no AS plot
FROM _plot
WHERE stratum is null;

-- Assign district to field plots
ALTER TABLE _plot
ADD COLUMN district_id INTEGER;

UPDATE _plot
SET district_id = a.aoi_id
FROM calc.aoi a 
WHERE ST_Contains(a.aoi_shape, centre_location)
AND a.aoi_hierarchy_level_id = 3;

-- Report unmatched plots
SELECT cluster, no AS plot
FROM _plot 
WHERE district_id IS NULL;

-- Assign region to field plots
ALTER TABLE _plot
ADD COLUMN region_id INTEGER;

UPDATE _plot
SET region_id = a.aoi_parent_id
FROM calc.aoi a 
WHERE a.aoi_id = district_id;

-- Assign zone to field plots
ALTER TABLE _plot
ADD COLUMN zone_id INTEGER;

UPDATE _plot
SET zone_id = a.aoi_parent_id
FROM calc.aoi a 
WHERE a.aoi_id = region_id;

-- Assign country to field plots
ALTER TABLE _plot
ADD COLUMN country_id INTEGER;

UPDATE _plot
SET country_id = a.aoi_parent_id
FROM calc.aoi a 
WHERE a.aoi_id = zone_id;

-- Report unmatched plots
SELECT COUNT(*)
FROM _plot 
WHERE country_id IS NULL;

------------------------------------------------------------
--- STRATUM WEIGHTS AND EXPANSION FACTORS
------------------------------------------------------------

--  1st phase plot count and areas per AOI

drop table if exists _district;

create table _district
as
select
        a.aoi_id as district_id,       
        a.aoi_total_area as district_area,
        a.aoi_land_area as district_land_area,
        count(s.*) as phase1_cnt
from
        calc.aoi a
join
        _sampling_design s
        on a.aoi_id = s.district_id
group by
        a.aoi_id,        
        a.aoi_total_area,
        a.aoi_land_area
;

-- 1st phase plot counts per AOI per stratum

drop table if exists _district_stratum;

create table _district_stratum
as
select
        a.aoi_id as district_id,
        s.stratum,        
        count(s.*) as phase1_cnt
from
        calc.aoi a
join
        _sampling_design s
        on a.aoi_id = s.district_id
group by
        a.aoi_id,
        s.stratum
;

-- Plot center counts per AOI per stratum

alter table _district_stratum 
add column obs_plot_cnt integer;

with cnt as (
        select 
                p.stratum,
                p.district_id,
                count(*)
        from
                _plot p
        where
                p.accessibility = '0' and p.measurement = 'P' and p.subplot = 'A'
        group by
                p.stratum,
                p.district_id        
)
update 
        _district_stratum
set 
        obs_plot_cnt = cnt.count
from
        cnt
where         
        cnt.district_id = _district_stratum.district_id
and 
        cnt.stratum = _district_stratum.stratum
;

-- Calculate plot expansion factors per stratum per AOI

alter table _district_stratum 
add column expf double precision;

update _district_stratum
        set expf = ( district_land_area * _district_stratum.phase1_cnt / d.phase1_cnt ) / obs_plot_cnt
from
        _district d
where
        d.district_id = _district_stratum.district_id
;

update _district_stratum
set obs_plot_cnt = 0
where obs_plot_cnt is null;
