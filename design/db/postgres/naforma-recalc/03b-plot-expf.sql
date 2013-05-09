SET search_path TO naforma1;

------------------------------------------------------------
--- PLOT EXPANSION FACTORS
------------------------------------------------------------

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
    set expf = stratum_area / obs_plot_cnt
where
    obs_plot_cnt > 0        
;

update _country_stratum
set obs_plot_cnt = 0
where obs_plot_cnt is null;


---------- ZONE ----------
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
    set expf = stratum_area / obs_plot_cnt
where
    obs_plot_cnt > 0        
;

update _zone_stratum
set obs_plot_cnt = 0
where obs_plot_cnt is null;




---------- REGION ----------

-- Plot center counts per region per stratum

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
    set expf = stratum_area / obs_plot_cnt
where
    obs_plot_cnt > 0        
;

update _region_stratum
set obs_plot_cnt = 0
where obs_plot_cnt is null;


---------- DISTRICT ----------

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
    set expf = stratum_area / obs_plot_cnt
where
    obs_plot_cnt > 0        
;

update _district_stratum
set obs_plot_cnt = 0
where obs_plot_cnt is null;
