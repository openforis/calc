SET search_path TO naforma1, public;

select
    t.plot_id,    
    t.dbh_class,    
    t.health,
    t.origin,
    t.species,
    t.species_group,
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
    sum(t.aboveground_biomass / t.inclusion_area) as aboveground_biomass,
    sum(t.volume / t.inclusion_area) as volume
from
    naforma1._tree t
join
    naforma1._plot p
    on t.plot_id = p.plot_id    
group by
    t.plot_id,    
    t.dbh_class,    
    t.health,
    t.origin,
    t.species,
    t.species_group,
    p.vegetation_type,
    p.land_use,
    p.ownership_type,
    p.undergrowth_type,
    p.soil_structure,
    p.soil_texture,    
    p.erosion,
    p.grazing,
    p.catchment,
    p.shrubs_coverage