SET search_path TO naforma1, public;

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


-- CLEAN: Set plots with observations as accessible
update _plot
set accessibility = '0'
where 
        accessibility != '0'
    and
        exists (select _tree_id from tree t where t._plot_id = _plot._plot_id);

update _plot
set accessibility = '0'
where 
        accessibility != '0'
    and
        exists (select _stump_id from stump s where s._plot_id = _plot._plot_id);

update _plot
set accessibility = '0'
where 
        accessibility != '0'
    and
        exists (select _dead_wood_id from dead_wood dw where dw._plot_id = _plot._plot_id);

update _plot
set accessibility = '0'
where 
        accessibility != '0'
    and
        exists (select _bamboo_id from bamboo b where b._plot_id = _plot._plot_id);

-- CLEAN: Set default direction and distance
update _plot
set centre_dir = 0, centre_dist = 0
where (centre_dir=99 and centre_dist=99) 
        or centre_dir is null or centre_dist is null
        or centre_dist=0;

-- CLEAN: Set defaults for missing codes
update _plot
set catchment = '0'  -- Bare land
where catchment is null and accessibility = '0';

update _plot
set erosion = '0'  -- No erosion
where erosion is null and accessibility = '0';

update _plot
set ownership_type = '90'  -- Not known
where ownership_type is null and accessibility = '0';

update _plot
set grazing = '0'  -- No grazing
where grazing is null and accessibility = '0' and vegetation_type >= '401';

-- Replace vt 506 with 504?
--select count(*) from _plot where _plot.vegetation_type='506'

-- Assign cluster code and visit type to field plots 
ALTER TABLE _plot
ADD COLUMN cluster VARCHAR(255),
ADD COLUMN measurement VARCHAR(255);

UPDATE _plot
SET cluster = c.id, measurement = c.measurement
FROM cluster c
WHERE c._cluster_id = _plot._cluster_id;

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

-- CLEAN: Max slope = 80
update _plot
set slope = 80
where slope > 80;

-- CLEAN: Default share = 100
update _plot
set share = 100
where share is null;

-- CLEAN: Assume slope of 0 if missing
update _plot 
set slope = 0
where slope is null;

---- Reduce slope to steps of 5
--update _plot
--set slope = round(slope/5)*5;

---- Create correction for incorrect slope tables
--ALTER TABLE _plot
--ADD COLUMN slope_cf double precision;
--
--update _plot
--set slope_cf = cos(atan(slope / 100.0)) / cos(radians(0.9 * slope));
--
---- Calculate plot section areas
--ALTER TABLE _plot
--ADD COLUMN section_area double precision;
--
--update _plot
--set section_area = 15.0 * 15.0 * pi() * slope_cf * (share / 100.0);


alter table _plot
add column canopy_cover_tmp double precision;

alter table _plot
add column canopy_cover_class varchar;

update _plot
set canopy_cover_tmp =
    case
        when canopy_coverage_centre is not null and canopy_coverage_north is not null and canopy_coverage_east is not null and canopy_coverage_south is not null and canopy_coverage_west is not null                
            then (canopy_coverage_centre + canopy_coverage_north + canopy_coverage_east + canopy_coverage_south + canopy_coverage_west) / 5 * 4.17
        when canopy_cover is not null then    
                canopy_cover
        else     
            null
    end
;

update _plot
set canopy_cover_class =
    case 
        when canopy_cover_tmp < 5 then '1'
        when canopy_cover_tmp < 10 then '2'
        when canopy_cover_tmp < 40 then '3'
        when canopy_cover_tmp < 70 then '4'
        else '5'
    end
;

alter table _plot
drop column canopy_cover_tmp;

-- Add protected areas
drop table if exists protection_status_code;

create table protection_status_code as
select
    row_number() over (order by protection_status_label_en) as protection_status_id, 
    protection_status_label_en
from (    
    select 'Unprotected Area' as protection_status_label_en
    union
    select distinct    
        desig_eng as protection_status_label_en
    from     
        calc.naforma_protarea) ps;

insert into protection_status_code values ('-1', 'N/A');
    
alter table _plot
add column protection_status varchar(255);

with pa as (
select
    p._plot_id, pa.desig_eng
from 
    naforma1._plot p
inner join
    calc.naforma_protarea pa
on 
    ST_Contains(pa.geom, p.location)
)
update _plot
set protection_status = pa.desig_eng
from pa
where _plot._plot_id = pa._plot_id;

update _plot
set protection_status = 'Unprotected Area'
where protection_status is null;

alter table _plot
add column protection_status_id integer;

update _plot
set protection_status_id = ps.protection_status_id
from protection_status_code ps
where _plot.protection_status = ps.protection_status_label_en;

update _plot
set protection_status_id = -1
where _plot.protection_status_id is null;
