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
        p.location,
        sd.x as original_theoretical_location_x,
        sd.y as original_theoretical_location_y,
        sd.srid as original_theoretical_location_srs,
        sd.location as theoretical_location,
        ST_X(ST_TRANSFORM(sd.location, case when p.location_srs = 'EPSG:21037' then 32737 when p.location_srs = 'EPSG:21036' then 32736 when p.location_srs = 'EPSG:21035' then 32735 else 0 end)) as theoretical_location_x,
        ST_Y(ST_TRANSFORM(sd.location, case when p.location_srs = 'EPSG:21037' then 32737 when p.location_srs = 'EPSG:21036' then 32736 when p.location_srs = 'EPSG:21035' then 32735 else 0 end)) as theoretical_location_y,
        --ST_Y(ST_TRANSFORM(sd.location, substring(p.location_srs from '[0-9]+$')::integer)) as theoretical_location_y,
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
left join naforma1._sampling_design sd on 
	(p.cluster = sd.cluster and 
	((trim(p.no) = '' and sd.plot is null) 
		or (trim(p.no) != '' and to_number(p.no, '9') = sd.plot)))
left outer join        
        naforma1._plot_tree_agg r
        on p.plot_id_ = r.plot_id_
left outer join
        h
        on h.plot_id_ = p.plot_id_   
left outer join
	naforma1.cluster c on p.cluster_id_ = c.cluster_id_
where
        p.stratum is not null
and        
        p.accessibility = '0'
and
        p.subplot = 'A'   
and 	
	c.measurement = 'P'     
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
        p.location,
        sd.x,
        sd.y,
        sd.srid,
        sd.location,
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