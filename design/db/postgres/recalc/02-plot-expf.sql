SET search_path TO naforma1, public;

------------------------------------------------------------
--- STRATUM WEIGHTS AND EXPANSION FACTORS
------------------------------------------------------------

---------- COUNTRY ----------

--  1st phase plot count and areas per AOI

drop table if exists _country;

create table _country
as
select
        a.aoi_id as country_id,       
        a.aoi_total_area as country_area,
        a.aoi_land_area as country_land_area,
        count(s.*) as phase1_cnt
from
        calc.aoi a
join
        _sampling_design s
        on a.aoi_id = s.country_id
group by
        a.aoi_id,        
        a.aoi_total_area,
        a.aoi_land_area
;

-- 1st phase plot counts per AOI per stratum

drop table if exists _country_stratum;

create table _country_stratum
as
select
        a.aoi_id as country_id,
        s.stratum,        
        count(s.*) as phase1_cnt
from
        calc.aoi a
join
        _sampling_design s
        on a.aoi_id = s.country_id
group by
        a.aoi_id,
        s.stratum
;

-- Plot center counts per AOI per stratum

alter table _country_stratum 
add column obs_plot_cnt integer;

with cnt as (
        select 
                p.stratum,
                p.country_id,
                count(*)
        from
                _plot p
        where
                p.accessibility = '0' and p.measurement = 'P' and p.subplot = 'A'
        group by
                p.stratum,
                p.country_id        
)
update 
        _country_stratum
set 
        obs_plot_cnt = cnt.count
from
        cnt
where         
        cnt.country_id = _country_stratum.country_id
and 
        cnt.stratum = _country_stratum.stratum
;

-- Calculate plot expansion factors per stratum per AOI

alter table _country_stratum 
add column expf double precision;

update _country_stratum
        set expf = ( country_land_area * _country_stratum.phase1_cnt / d.phase1_cnt ) / obs_plot_cnt
from
        _country d
where
        d.country_id = _country_stratum.country_id
;

update _country_stratum
set obs_plot_cnt = 0
where obs_plot_cnt is null;


---------- ZONE ----------

--  1st phase plot count and areas per AOI

drop table if exists _zone;

create table _zone
as
select
        a.aoi_id as zone_id,       
        a.aoi_total_area as zone_area,
        a.aoi_land_area as zone_land_area,
        count(s.*) as phase1_cnt
from
        calc.aoi a
join
        _sampling_design s
        on a.aoi_id = s.zone_id
group by
        a.aoi_id,        
        a.aoi_total_area,
        a.aoi_land_area
;

-- 1st phase plot counts per AOI per stratum

drop table if exists _zone_stratum;

create table _zone_stratum
as
select
        a.aoi_id as zone_id,
        s.stratum,        
        count(s.*) as phase1_cnt
from
        calc.aoi a
join
        _sampling_design s
        on a.aoi_id = s.zone_id
group by
        a.aoi_id,
        s.stratum
;

-- Plot center counts per AOI per stratum

alter table _zone_stratum 
add column obs_plot_cnt integer;

with cnt as (
        select 
                p.stratum,
                p.zone_id,
                count(*)
        from
                _plot p
        where
                p.accessibility = '0' and p.measurement = 'P' and p.subplot = 'A'
        group by
                p.stratum,
                p.zone_id        
)
update 
        _zone_stratum
set 
        obs_plot_cnt = cnt.count
from
        cnt
where         
        cnt.zone_id = _zone_stratum.zone_id
and 
        cnt.stratum = _zone_stratum.stratum
;

-- Calculate plot expansion factors per stratum per AOI

alter table _zone_stratum 
add column expf double precision;

update _zone_stratum
        set expf = ( zone_land_area * _zone_stratum.phase1_cnt / d.phase1_cnt ) / obs_plot_cnt
from
        _zone d
where
        d.zone_id = _zone_stratum.zone_id
;

update _zone_stratum
set obs_plot_cnt = 0
where obs_plot_cnt is null;




---------- zone ----------

--  1st phase plot count and areas per AOI

drop table if exists _region;

create table _region
as
select
        a.aoi_id as region_id,       
        a.aoi_total_area as region_area,
        a.aoi_land_area as region_land_area,
        count(s.*) as phase1_cnt
from
        calc.aoi a
join
        _sampling_design s
        on a.aoi_id = s.region_id
group by
        a.aoi_id,        
        a.aoi_total_area,
        a.aoi_land_area
;

-- 1st phase plot counts per AOI per stratum

drop table if exists _region_stratum;

create table _region_stratum
as
select
        a.aoi_id as region_id,
        s.stratum,        
        count(s.*) as phase1_cnt
from
        calc.aoi a
join
        _sampling_design s
        on a.aoi_id = s.region_id
group by
        a.aoi_id,
        s.stratum
;

-- Plot center counts per AOI per stratum

alter table _region_stratum 
add column obs_plot_cnt integer;

with cnt as (
        select 
                p.stratum,
                p.region_id,
                count(*)
        from
                _plot p
        where
                p.accessibility = '0' and p.measurement = 'P' and p.subplot = 'A'
        group by
                p.stratum,
                p.region_id        
)
update 
        _region_stratum
set 
        obs_plot_cnt = cnt.count
from
        cnt
where         
        cnt.region_id = _region_stratum.region_id
and 
        cnt.stratum = _region_stratum.stratum
;

-- Calculate plot expansion factors per stratum per AOI

alter table _region_stratum 
add column expf double precision;

update _region_stratum
        set expf = ( region_land_area * _region_stratum.phase1_cnt / d.phase1_cnt ) / obs_plot_cnt
from
        _region d
where
        d.region_id = _region_stratum.region_id
;

update _region_stratum
set obs_plot_cnt = 0
where obs_plot_cnt is null;


---------- DISTRICT ----------

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
