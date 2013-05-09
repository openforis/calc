SET search_path TO naforma1;

------------------------------------------------------------
--- STRATUM WEIGHTS 
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

alter table _country_stratum
add column stratum_weight double precision;

update _country_stratum
set stratum_weight = phase1_cnt / (select sum(phase1_cnt) from _country_stratum);

alter table _country_stratum
add column stratum_area double precision;

update _country_stratum
set stratum_area = stratum_weight * c.country_land_area
from _country c
where _country_stratum.country_id = c.country_id;


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


alter table _zone_stratum
add column stratum_weight double precision;

update _zone_stratum
set stratum_weight = phase1_cnt / (select sum(phase1_cnt) from _zone_stratum);

alter table _zone_stratum
add column stratum_area double precision;

update _zone_stratum
set stratum_area = stratum_weight * aoi.zone_land_area
from _zone aoi
where _zone_stratum.zone_id = aoi.zone_id;

---------- Region ----------

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

alter table _region_stratum
add column stratum_weight double precision;

update _region_stratum
set stratum_weight = phase1_cnt / (select sum(phase1_cnt) from _region_stratum);

alter table _region_stratum
add column stratum_area double precision;

update _region_stratum
set stratum_area = stratum_weight * aoi.region_land_area
from _region aoi
where _region_stratum.region_id = aoi.region_id;


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

alter table _district_stratum
add column stratum_weight double precision;

update _district_stratum
set stratum_weight = phase1_cnt / (select sum(phase1_cnt) from _district_stratum);

alter table _district_stratum
add column stratum_area double precision;

update _district_stratum
set stratum_area = stratum_weight * aoi.district_land_area
from _district aoi
where _district_stratum.district_id = aoi.district_id;
