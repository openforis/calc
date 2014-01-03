--select distinct
--r.commercial_tree
--from
--naforma1._plot_tree_agg r
--;
select 1 + null;

//30783 vs 30583
select count(*) 
from
        naforma1._plot o 
where
        o.accessibility = '0'
and
        o.subplot = 'A'                 
;        


--select count (*) from (

with h as (
        select 
                t._plot_id,
                min(t.est_height) as min_est_height,
                max(t.est_height) as max_est_height,
                avg(t.est_height) as avg_est_height
        from
                naforma1._tree t
        group by
                t._plot_id
)
select
        p.stratum, 
        p.cluster,
        p.no as plot_no,
        p.land_use,
        p.vegetation_type,
        p.canopy_cover_tmp as canopy_cover,
        p.location_x,
        p.location_y,
        p.location_srs,
        sum(r.basal_area) as basal_area,
        h.min_est_height,
        h.max_est_height,
        h.avg_est_height,
        sum(r.volume) as volume,
        sum(
                case 
                        when r.commercial_tree = 1 then r.volume
                        else 0
                end
        ) as commercial_volume,
        sum(r.aboveground_biomass) as aboveground_biomass,
        sum(r.belowground_biomass) as belowground_biomass
from
        naforma1._plot p
left outer join        
        naforma1._plot_tree_agg r
        on p._plot_id = r._plot_id
left outer join
        h
        on h._plot_id = p._plot_id        
where
        p.stratum is not null
and        
        p.accessibility = '0'
and
        p.subplot = 'A'        
group by
        p.stratum, 
        p.cluster,
        p.no,
        p.land_use,
        p.vegetation_type,
        p.canopy_cover_tmp,
        p.location_x,
        p.location_y,
        p.location_srs,
        h.min_est_height,
        h.max_est_height,
        h.avg_est_height
ORDER BY
        p.stratum, 
        p.cluster,
        p.no::numeric,
        p.land_use,
        p.vegetation_type,
        p.location_x,
        p.location_y,
        p.location_srs

--) as a        
;  


-- plus AOI


with h as (
        select 
                t._plot_id,
                min(t.est_height) as min_est_height,
                max(t.est_height) as max_est_height,
                avg(t.est_height) as avg_est_height
        from
                naforma1._tree t
        group by
                t._plot_id
)
select
        z.aoi_label as zone,
        re.aoi_label as region,
        d.aoi_label as district,
        p.stratum, 
        p.cluster,
        p.no as plot_no,
        p.land_use,
        p.vegetation_type,
        p.canopy_cover_tmp as canopy_cover,
        p.location_x,
        p.location_y,
        p.location_srs,
        sum(r.basal_area) as basal_area_per_ha,
        h.min_est_height,
        h.max_est_height,
        h.avg_est_height,
        sum(r.volume) as volume_per_ha,
        sum(
                case 
                        when r.commercial_tree = 1 then r.volume
                        else 0
                end
        ) as commercial_volume_per_ha,
        sum(r.aboveground_biomass) as aboveground_biomass_per_ha,
        sum(r.belowground_biomass) as belowground_biomass_per_ha,
        sum( r.est_cnt ) as stems_per_ha
from
        naforma1._plot p
left outer join        
        naforma1._plot_tree_agg r
        on p._plot_id = r._plot_id
left outer join
        h
        on h._plot_id = p._plot_id        
join         
        calc.aoi z
        on z.aoi_id = p.zone_id        
join         
        calc.aoi re
        on re.aoi_id = p.region_id    
join         
        calc.aoi d
        on d.aoi_id = p.district_id
where
        p.stratum is not null
and        
        p.accessibility = '0'
and
        p.subplot = 'A'
group by
        z.aoi_label,
        re.aoi_label,
        d.aoi_label,
        p.stratum, 
        p.cluster,
        p.no,
        p.land_use,
        p.vegetation_type,
        p.canopy_cover_tmp,
        p.location_x,
        p.location_y,
        p.location_srs,
        h.min_est_height,
        h.max_est_height,
        h.avg_est_height
ORDER BY
        p.stratum, 
        p.cluster,
        p.no::numeric,
        p.land_use,
        p.vegetation_type,
        p.location_x,
        p.location_y,
        p.location_srs

--) as a        
;  



