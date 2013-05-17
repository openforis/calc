SET search_path TO naforma1, public;

DROP TABLE IF EXISTS _district_plot_weights;

CREATE TABLE _district_plot_weights AS
SELECT
    c.cluster_id,
    c.id AS cluster,
    p.no AS plot,
    p.stratum,
    p.country_id,
    p.zone_id,
    p.region_id,
    p.district_id,
    p.plot_id,
    p.subplot,
    p.time_study_date,
    p.time_study_start_time,
    p.time_study_end_time,
    p.share,
    p.permanent,
    p.group_leader,
    p.accessibility,
    p.accessibility_code_id,
    p.region_code_id,
    p.district_code_id,
    p.division,
    p.ward,
    p.village,
    p.forest,
    p.slope,
    p.location_x,
    p.location_y,
    p.location_srs,
    p.centre_dir,
    p.centre_dist,
    p.centre_desc,
    p.land_use,
    p.land_use_code_id,
    p.land_use_other,
    p.vegetation_type,
    p.vegetation_type_code_id,
    p.vegetation_type_other,
    p.ownership_type,
    p.ownership_code_id,
    p.canopy_cover,
    p.canopy_coverage_centre,
    p.canopy_coverage_north,
    p.canopy_coverage_east,
    p.canopy_coverage_south,
    p.canopy_coverage_west,
    p.undergrowth_type,
    p.undergrowth_code_id,
    p.undergrowth_type_other,
    p.planting_year,
    p.soil_structure,
    p.soil_structure_code_id,
    p.soil_depth,
    p.soil_colour,
    p.soil_texture,
    p.soil_texture_code_id,
    p.soil_texture_other,
    p.soil_sample,
    p.erosion,
    p.erosion_intensity_code_id,
    p.grazing,
    p.grazing_activity_code_id,
    p.catchment,
    p.catchment_value_code_id,
    p.remarks,
    p.slope_correction_applied,
    p.shrubs_coverage,
    p.shrub_coverage_code_id,
    p.shrubs_avg_height,
    p.regen_remarks,
    p.bamboo_remarks,
    p.measurement,
    p.location,
    p.centre_location,
    p.canopy_cover_class,
    s.expf
FROM
    naforma1._plot p
INNER JOIN
    naforma1.cluster c
ON
    p.cluster_id = c.cluster_id
INNER JOIN
    naforma1._district_stratum s
ON
    p.stratum = s.stratum
AND 
    p.district_id = s.district_id
WHERE
    s.obs_plot_cnt >= 30 AND p.measurement ='P' and p.subplot = 'A';
    
DROP TABLE IF EXISTS _district_synth_plot_weights;

CREATE TABLE _district_synth_plot_weights AS
SELECT
    s.stratum_area,
    sd.location,
    s.obs_plot_cnt,
    s.stratum_weight,
    s.stratum,
    s.district_id,
    s.phase1_cnt,
    stratum_area / phase1_cnt AS phase1_weight 
FROM
    naforma1._district_stratum s
INNER JOIN
    naforma1._sampling_design sd
ON
    s.stratum = sd.stratum AND s.district_id = sd.district_id
WHERE
    s.obs_plot_cnt < 30 ;