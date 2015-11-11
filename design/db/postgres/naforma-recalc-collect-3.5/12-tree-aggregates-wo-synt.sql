SET search_path TO naforma1, public;

alter table
	"_plot" add primary key ("_plot_id")
;
	
alter table
	"_tree" add constraint "_tree_plot_fk" foreign key ("_plot_id") references
	"_plot" ("_plot_id")
;

drop table if exists naforma1._plot_tree_agg;

create table naforma1._plot_tree_agg
as
select
    p.stratum,
    p.district_id,
    p.region_id,
    p.zone_id,
    p.country_id,
    p.cluster,
    t._plot_id,    
    t.dbh_class,    
    t.health,
    t.origin,
    t.species,
    t.species_group,
    t.commercial_tree,
    t.commercial_class,
    t.growing_stock,
    coalesce(p.vegetation_type, '-1') as vegetation_type,
    coalesce(p.land_use, '-1') as land_use,
    coalesce(p.ownership_type, '-1') as ownership_type,
    coalesce(p.undergrowth_type, '-1') as undergrowth_type,
    coalesce(p.soil_structure, '-1') as soil_structure,
    coalesce(p.soil_texture, '-1') as soil_texture,
    coalesce(p.erosion, '-1') as erosion,
    coalesce(p.grazing, '-1') as grazing,
    coalesce(p.catchment, '-1') as catchment,
    coalesce(p.shrubs_coverage, '-1') as shrubs_coverage,
    coalesce(p.canopy_cover_class, '-1') as canopy_cover_class,
    sum(t.aboveground_biomass / t.inclusion_area) as aboveground_biomass,
    sum(t.belowground_biomass / t.inclusion_area) as belowground_biomass,
    sum(t.carbon / t.inclusion_area) as carbon,
    sum(t.volume / t.inclusion_area) as volume,
    sum(t.est_cnt / t.inclusion_area) as est_cnt,
    sum(t.basal_area ) as basal_area,
    count(*) as cnt,
    count(*) as agg_cnt
from
    naforma1._tree t
join
    naforma1._plot p
    on t._plot_id = p._plot_id
where
     p.accessibility = '0' and p.measurement = 'P'
group by
    p.stratum,
    p.district_id,
    p.region_id,
    p.zone_id,
    p.country_id,
    p.cluster,
    t._plot_id,
    t.dbh_class,    
    t.health,
    t.origin,
    t.species,
    t.species_group,
    t.commercial_tree,
    t.commercial_class,
    t.growing_stock,
    p.vegetation_type,
    p.land_use,
    p.ownership_type,
    p.undergrowth_type,
    p.soil_structure,
    p.soil_texture,    
    p.erosion,
    p.grazing,
    p.catchment,
    p.shrubs_coverage,
    p.canopy_cover_class
;




---------------- COUNTRY

drop table if exists naforma1._country_stratum_tree_agg cascade;

create table
	naforma1._country_stratum_tree_agg as
select
    p.country_id,
    p.stratum,	
	p.dbh_class,
	p.health,
	p.origin,
	p.species,
	p.species_group,
	p.commercial_tree,
	p.commercial_class,
	p.growing_stock,
	p.vegetation_type,
	p.land_use,
	p.ownership_type,
	p.undergrowth_type,
	p.soil_structure,
	p.soil_texture,
	p.erosion,
	p.grazing,
	p.catchment,
	p.shrubs_coverage,	
	p.canopy_cover_class,
	s.expf * sum(p.volume) as volume,
	s.expf * sum(p.aboveground_biomass) as aboveground_biomass,
	s.expf * sum(p.belowground_biomass) as belowground_biomass,
	s.expf * sum(p.carbon) as carbon,
	s.expf * sum(p.est_cnt) as est_cnt,
	s.expf * sum(p.basal_area) as basal_area,
	count(*) as cnt,
    count(*) as agg_cnt

from
	_plot_tree_agg p    
INNER JOIN
    naforma1._country_stratum s
ON    
    p.stratum = s.stratum
AND
    p.country_id = s.country_id
GROUP BY
    p.country_id,
    p.stratum,	
	p.dbh_class,
	p.health,
	p.origin,
	p.species,
	p.species_group,
	p.commercial_tree,
	p.commercial_class,
	p.growing_stock,
	p.vegetation_type,
	p.land_use,
	p.ownership_type,
	p.undergrowth_type,
	p.soil_structure,
	p.soil_texture,
	p.erosion,
	p.grazing,
	p.catchment,
	p.shrubs_coverage,
	p.canopy_cover_class,
    s.expf;    
    
drop table if exists naforma1._country_tree_agg cascade;

create table
	naforma1._country_tree_agg as
select
    p.country_id,    
	p.dbh_class,
	p.health,
	p.origin,
	p.species,
	p.species_group,
	p.commercial_tree,
	p.commercial_class,
	p.growing_stock,
	p.vegetation_type,
	p.land_use,
	p.ownership_type,
	p.undergrowth_type,
	p.soil_structure,
	p.soil_texture,
	p.erosion,
	p.grazing,
	p.catchment,
	p.shrubs_coverage,	
	p.canopy_cover_class,
	sum(p.volume) as volume,
	sum(p.aboveground_biomass) as aboveground_biomass,
	sum(p.belowground_biomass) as belowground_biomass,
	sum(p.carbon) as carbon,
	sum(p.est_cnt) as est_cnt,
	sum(p.basal_area) as basal_area,
	sum(cnt) as cnt,
    sum(agg_cnt) as agg_cnt
from
	_country_stratum_tree_agg p    
GROUP BY
    p.country_id,    
	p.dbh_class,
	p.health,
	p.origin,
	p.species,
	p.species_group,
	p.commercial_tree,
	p.commercial_class,
	p.growing_stock,
	p.vegetation_type,
	p.land_use,
	p.ownership_type,
	p.undergrowth_type,
	p.soil_structure,
	p.soil_texture,
	p.erosion,
	p.grazing,
	p.catchment,
	p.shrubs_coverage,
	p.canopy_cover_class
;    




---------------- ZONE

drop table if exists naforma1._zone_stratum_tree_agg cascade;

create table
	naforma1._zone_stratum_tree_agg as
select
    p.country_id,
    p.zone_id,
    p.stratum,	
	p.dbh_class,
	p.health,
	p.origin,
	p.species,
	p.species_group,
	p.commercial_tree,
	p.commercial_class,
	p.growing_stock,
	p.vegetation_type,
	p.land_use,
	p.ownership_type,
	p.undergrowth_type,
	p.soil_structure,
	p.soil_texture,
	p.erosion,
	p.grazing,
	p.catchment,
	p.shrubs_coverage,	
	p.canopy_cover_class,
	s.expf * sum(p.volume) as volume,
	s.expf * sum(p.aboveground_biomass) as aboveground_biomass,
	s.expf * sum(p.belowground_biomass) as belowground_biomass,
	s.expf * sum(p.carbon) as carbon,
	s.expf * sum(p.est_cnt) as est_cnt,
	s.expf * sum(p.basal_area) as basal_area,
	count(*) as cnt,
    count(*) as agg_cnt
from
	_plot_tree_agg p    
INNER JOIN
    naforma1._zone_stratum s
ON    
    p.stratum = s.stratum
AND
    p.zone_id = s.zone_id
where    
    s.obs_plot_cnt > 0
GROUP BY
    p.country_id,
    p.zone_id,
    p.stratum,	
	p.dbh_class,
	p.health,
	p.origin,
	p.species,
	p.species_group,
	p.commercial_tree,
	p.commercial_class,
	p.growing_stock,
	p.vegetation_type,
	p.land_use,
	p.ownership_type,
	p.undergrowth_type,
	p.soil_structure,
	p.soil_texture,
	p.erosion,
	p.grazing,
	p.catchment,
	p.shrubs_coverage,
	p.canopy_cover_class,
    s.expf
;    
    
    

drop table if exists naforma1._zone_tree_agg cascade;

create table
	naforma1._zone_tree_agg as
select
    p.country_id,
    p.zone_id,    
	p.dbh_class,
	p.health,
	p.origin,
	p.species,
	p.species_group,
	p.commercial_tree,
	p.commercial_class,
	p.growing_stock,
	p.vegetation_type,
	p.land_use,
	p.ownership_type,
	p.undergrowth_type,
	p.soil_structure,
	p.soil_texture,
	p.erosion,
	p.grazing,
	p.catchment,
	p.shrubs_coverage,	
	p.canopy_cover_class,
	sum(p.volume) as volume,
	sum(p.aboveground_biomass) as aboveground_biomass,
	sum(p.belowground_biomass) as belowground_biomass,
	sum(p.carbon) as carbon,
	sum(p.est_cnt) as est_cnt,
	sum(p.basal_area) as basal_area,
	sum(cnt) as cnt,
    sum(agg_cnt) as agg_cnt
from
	_zone_stratum_tree_agg p    
GROUP BY
    p.country_id,
    p.zone_id,    	
	p.dbh_class,
	p.health,
	p.origin,
	p.species,
	p.species_group,
	p.commercial_tree,
	p.commercial_class,
	p.growing_stock,
	p.vegetation_type,
	p.land_use,
	p.ownership_type,
	p.undergrowth_type,
	p.soil_structure,
	p.soil_texture,
	p.erosion,
	p.grazing,
	p.catchment,
	p.shrubs_coverage,
	p.canopy_cover_class
--	
--union	
--	
--select
--    a1.aoi_parent_id as country_id,
--    s.zone_id,   
--	p.dbh_class,
--	p.health,
--	p.origin,
--	p.species,
--	p.species_group,
--	p.commercial_tree,
--	p.commercial_class,
--	p.growing_stock,
--	p.vegetation_type,
--	p.land_use,
--	p.ownership_type,
--	p.undergrowth_type,
--	p.soil_structure,
--	p.soil_texture,
--	p.erosion,
--	p.grazing,
--	p.catchment,
--	p.shrubs_coverage,	
--	p.canopy_cover_class,
--	( s.phase1_cnt / c.phase1_cnt::double precision ) * p.volume as volume,
--	( s.phase1_cnt / c.phase1_cnt::double precision ) * p.aboveground_biomass as aboveground_biomass,
--	( s.phase1_cnt / c.phase1_cnt::double precision ) * p.belowground_biomass as belowground_biomass,
--	( s.phase1_cnt / c.phase1_cnt::double precision ) * p.carbon as carbon,
--	( s.phase1_cnt / c.phase1_cnt::double precision ) * p.est_cnt as est_cnt,	
--	( s.phase1_cnt / c.phase1_cnt::double precision ) * p.basal_area as basal_area,		
--	0 as cnt,
--    100 as agg_cnt
--from
--	_country_stratum_tree_agg p    
--join
--    naforma1._zone_stratum s
--    on s.stratum = p.stratum
--join
--    naforma1._country_stratum c
--    on c.stratum = p.stratum
--    and p.country_id = c.country_id
--join
--    calc.aoi a1
--    on s.zone_id = a1.aoi_id    
--where
--    s.obs_plot_cnt < 30        	
; 
	
	
	
	
---------------- REGION

drop table if exists naforma1._region_stratum_tree_agg cascade;

create table
	naforma1._region_stratum_tree_agg as
select
    p.country_id,
    p.zone_id,
    p.region_id,
    p.stratum,	
	p.dbh_class,
	p.health,
	p.origin,
	p.species,
	p.species_group,
	p.commercial_tree,
	p.commercial_class,
	p.growing_stock,
	p.vegetation_type,
	p.land_use,
	p.ownership_type,
	p.undergrowth_type,
	p.soil_structure,
	p.soil_texture,
	p.erosion,
	p.grazing,
	p.catchment,
	p.shrubs_coverage,	
	p.canopy_cover_class,
	s.expf * sum(p.volume) as volume,
	s.expf * sum(p.aboveground_biomass) as aboveground_biomass,
	s.expf * sum(p.belowground_biomass) as belowground_biomass,
	s.expf * sum(p.carbon) as carbon,
	s.expf * sum(p.est_cnt) as est_cnt,
	s.expf * sum(p.basal_area) as basal_area,
	count(*) as cnt,
    count(*) as agg_cnt
from
	_plot_tree_agg p    
INNER JOIN
    naforma1._region_stratum s
ON    
    p.stratum = s.stratum
AND
    p.region_id = s.region_id
where    
    s.obs_plot_cnt > 0
GROUP BY
    p.country_id,
    p.zone_id,
    p.stratum,	
    p.region_id,
	p.dbh_class,
	p.health,
	p.origin,
	p.species,
	p.species_group,
	p.commercial_tree,
	p.commercial_class,
	p.growing_stock,
	p.vegetation_type,
	p.land_use,
	p.ownership_type,
	p.undergrowth_type,
	p.soil_structure,
	p.soil_texture,
	p.erosion,
	p.grazing,
	p.catchment,
	p.shrubs_coverage,
	p.canopy_cover_class,
    s.expf;    	
    
    
drop table if exists naforma1._region_tree_agg cascade;

create table
	naforma1._region_tree_agg as
select
    p.country_id,
    p.zone_id,    
    p.region_id,
	p.dbh_class,
	p.health,
	p.origin,
	p.species,
	p.species_group,
	p.commercial_tree,
	p.commercial_class,
	p.growing_stock,
	p.vegetation_type,
	p.land_use,
	p.ownership_type,
	p.undergrowth_type,
	p.soil_structure,
	p.soil_texture,
	p.erosion,
	p.grazing,
	p.catchment,
	p.shrubs_coverage,	
	p.canopy_cover_class,
	sum(p.volume) as volume,
	sum(p.aboveground_biomass) as aboveground_biomass,
	sum(p.belowground_biomass) as belowground_biomass,
	sum(p.carbon) as carbon,		
	sum(p.est_cnt) as est_cnt,
	sum(p.basal_area) as basal_area,
	sum(cnt) as cnt,
    sum(agg_cnt) as agg_cnt
from
	_region_stratum_tree_agg p    
GROUP BY
    p.country_id,
    p.zone_id,    	
    p.region_id,
	p.dbh_class,
	p.health,
	p.origin,
	p.species,
	p.species_group,
	p.commercial_tree,
	p.commercial_class,
	p.growing_stock,
	p.vegetation_type,
	p.land_use,
	p.ownership_type,
	p.undergrowth_type,
	p.soil_structure,
	p.soil_texture,
	p.erosion,
	p.grazing,
	p.catchment,
	p.shrubs_coverage,
	p.canopy_cover_class
--	
--union	
--	
--select
--    a2.aoi_parent_id as country_id,
--    a1.aoi_parent_id as zone_id,
--    s.region_id,    
--	p.dbh_class,
--	p.health,
--	p.origin,
--	p.species,
--	p.species_group,
--	p.commercial_tree,
--	p.commercial_class,
--	p.growing_stock,
--	p.vegetation_type,
--	p.land_use,
--	p.ownership_type,
--	p.undergrowth_type,
--	p.soil_structure,
--	p.soil_texture,
--	p.erosion,
--	p.grazing,
--	p.catchment,
--	p.shrubs_coverage,	
--	p.canopy_cover_class,
--	( s.phase1_cnt / c.phase1_cnt::double precision ) * p.volume as volume,
--	( s.phase1_cnt / c.phase1_cnt::double precision ) * p.aboveground_biomass as aboveground_biomass,
--	( s.phase1_cnt / c.phase1_cnt::double precision ) * p.belowground_biomass as belowground_biomass,
--	( s.phase1_cnt / c.phase1_cnt::double precision ) * p.carbon as carbon,
--	( s.phase1_cnt / c.phase1_cnt::double precision ) * p.est_cnt as est_cnt,
--	( s.phase1_cnt / c.phase1_cnt::double precision ) * p.basal_area as basal_area,	
--	0 as cnt,
--    100 as agg_cnt
--from
--	_country_stratum_tree_agg p    
--join
--    naforma1._region_stratum s
--    on s.stratum = p.stratum
--join
--    naforma1._country_stratum c
--    on c.stratum = p.stratum
--    and p.country_id = c.country_id
--join
--    calc.aoi a1
--    on s.region_id = a1.aoi_id    
--join
--    calc.aoi a2
--    on a1.aoi_parent_id = a2.aoi_id
--where
--    s.obs_plot_cnt < 30        	
; 
	
	
	
	
---------------- DISTRICT

drop table if exists naforma1._district_stratum_tree_agg cascade;

create table
	naforma1._district_stratum_tree_agg as
select
    p.country_id,
    p.zone_id,
    p.region_id,
    p.district_id,
    p.stratum,	
	p.dbh_class,
	p.health,
	p.origin,
	p.species,
	p.species_group,
	p.commercial_tree,
	p.commercial_class,
	p.growing_stock,
	p.vegetation_type,
	p.land_use,
	p.ownership_type,
	p.undergrowth_type,
	p.soil_structure,
	p.soil_texture,
	p.erosion,
	p.grazing,
	p.catchment,
	p.shrubs_coverage,	
	p.canopy_cover_class,
	s.expf * sum(p.volume) as volume,
	s.expf * sum(p.aboveground_biomass) as aboveground_biomass,
	s.expf * sum(p.belowground_biomass) as belowground_biomass,
	s.expf * sum(p.carbon) as carbon,
	s.expf * sum(p.est_cnt) as est_cnt,
	s.expf * sum(p.basal_area) as basal_area,
	count(*) as cnt,
    count(*) as agg_cnt
from
	_plot_tree_agg p    
INNER JOIN
    naforma1._district_stratum s
ON    
    p.stratum = s.stratum
AND
    p.district_id = s.district_id
where    
    s.obs_plot_cnt > 0
GROUP BY
    p.country_id,
    p.zone_id,
    p.stratum,	
    p.region_id,
    p.district_id,
	p.dbh_class,
	p.health,
	p.origin,
	p.species,
	p.species_group,
	p.commercial_tree,
	p.commercial_class,
	p.growing_stock,
	p.vegetation_type,
	p.land_use,
	p.ownership_type,
	p.undergrowth_type,
	p.soil_structure,
	p.soil_texture,
	p.erosion,
	p.grazing,
	p.catchment,
	p.shrubs_coverage,
	p.canopy_cover_class,
    s.expf;    	
    
    
drop table if exists naforma1._district_tree_agg cascade;

create table
	naforma1._district_tree_agg as
select
    p.country_id,
    p.zone_id,    
    p.region_id,
    p.district_id,
	p.dbh_class,
	p.health,
	p.origin,
	p.species,
	p.species_group,
	p.commercial_tree,
	p.commercial_class,
	p.growing_stock,
	p.vegetation_type,
	p.land_use,
	p.ownership_type,
	p.undergrowth_type,
	p.soil_structure,
	p.soil_texture,
	p.erosion,
	p.grazing,
	p.catchment,
	p.shrubs_coverage,	
	p.canopy_cover_class,
	sum(p.volume) as volume,
	sum(p.aboveground_biomass) as aboveground_biomass,
	sum(p.belowground_biomass) as belowground_biomass,
	sum(p.carbon) as carbon,
	sum(p.est_cnt) as est_cnt,
	sum(p.basal_area) as basal_area,
	sum(cnt) as cnt,
    sum(agg_cnt) as agg_cnt
from
	_district_stratum_tree_agg p    
GROUP BY
    p.country_id,
    p.zone_id,    	
    p.region_id,
    p.district_id,
	p.dbh_class,
	p.health,
	p.origin,
	p.species,
	p.species_group,
	p.commercial_tree,
	p.commercial_class,
	p.growing_stock,
	p.vegetation_type,
	p.land_use,
	p.ownership_type,
	p.undergrowth_type,
	p.soil_structure,
	p.soil_texture,
	p.erosion,
	p.grazing,
	p.catchment,
	p.shrubs_coverage,
	p.canopy_cover_class
;	
--
--insert into _district_tree_agg	
--select
--    a3.aoi_parent_id as country_id,
--    a2.aoi_parent_id as zone_id,
--    a1.aoi_parent_id as region_id,
--    s.district_id, 
--	p.dbh_class,
--	p.health,
--	p.origin,
--	p.species,
--	p.species_group,
--	p.commercial_tree,
--	p.commercial_class,
--	p.growing_stock,
--	p.vegetation_type,
--	p.land_use,
--	p.ownership_type,
--	p.undergrowth_type,
--	p.soil_structure,
--	p.soil_texture,
--	p.erosion,
--	p.grazing,
--	p.catchment,
--	p.shrubs_coverage,	
--	p.canopy_cover_class,
--	( s.phase1_cnt / c.phase1_cnt::double precision ) * p.volume as volume,
--	( s.phase1_cnt / c.phase1_cnt::double precision ) * p.aboveground_biomass as aboveground_biomass,
--	( s.phase1_cnt / c.phase1_cnt::double precision ) * p.belowground_biomass as belowground_biomass,
--	( s.phase1_cnt / c.phase1_cnt::double precision ) * p.carbon as carbon,
--	( s.phase1_cnt / c.phase1_cnt::double precision ) * p.est_cnt as est_cnt,
--	( s.phase1_cnt / c.phase1_cnt::double precision ) * p.basal_area as basal_area,	
--	0 as cnt,
--    100 as agg_cnt
--from
--	_country_stratum_tree_agg p    
--join
--    naforma1._district_stratum s
--    on s.stratum = p.stratum
--join
--    naforma1._country_stratum c
--    on c.stratum = p.stratum
--    and p.country_id = c.country_id
--join
--    calc.aoi a1
--    on s.district_id = a1.aoi_id    
--join
--    calc.aoi a2
--    on a1.aoi_parent_id = a2.aoi_id
--join
--    calc.aoi a3
--    on a2.aoi_parent_id = a3.aoi_id    
--where
--    s.obs_plot_cnt < 30    
; 	
	
	    