with h as (
        select 
                t.plot_id_,
                min(t.est_height) as min_est_height,
                max(t.est_height) as max_est_height,
                avg(t.est_height) as avg_est_height,
                count(distinct t.species ) as species_count,
                count( t.tree_id_ ) as tree_count
        from
                naforma1._tree t
        group by
                t.plot_id_
)
select
        p.stratum, 
        p.cluster,
        p.no as plot_no,
        p.land_use,
        p.vegetation_type,
        --p.canopy_cover_tmp as canopy_cover,
        p.location_x,
        p.location_y,
        p.location_srs,
        sum(r.basal_area) as basal_area,
        h.min_est_height,
        h.max_est_height,
        h.avg_est_height,
        h.species_count,
        h.tree_count,
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
        on p.plot_id_ = r.plot_id_
left outer join
        h
        on h.plot_id_ = p.plot_id_        
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
      --  p.canopy_cover_tmp,
        p.location_x,
        p.location_y,
        p.location_srs,
        h.min_est_height,
        h.max_est_height,
        h.avg_est_height,
        h.species_count,
        h.tree_count
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