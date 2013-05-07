SET search_path TO naforma1, public;

drop view if exists _country_area_view cascade;

create view _country_area_view as
SELECT
    p.country_id,
    p.vegetation_type,
    p.land_use AS land_use_code,
    p.ownership_type AS ownership_type_code,
    p.undergrowth_type AS undergrowth_type_code,
    p.erosion AS erosion_code,
    p.grazing AS grazing_code,
    p.catchment AS catchment_code,
    p.shrubs_coverage AS shrubs_coverage_code,
    p.est_area,
    c1.land_use_label_en   AS land_use,
    c2.ownership_label_en AS ownership_type,
    c3.undergrowth_label_en AS undergrowth,
    c4.erosion_intensity_label_en AS erosion,
    c5.grazing_activity_label_en AS grazing_activity,
    c6.catchment_value_label_en AS catchment,
    c7.shrub_coverage_label_en AS shrub_coverage
FROM
    _country_plot_agg p
LEFT OUTER JOIN land_use_code c1 ON p.land_use = c1.land_use
LEFT OUTER JOIN ownership_code c2 ON p.ownership_type = c2.ownership
LEFT OUTER JOIN undergrowth_code c3 ON p.undergrowth_type = c3.undergrowth
LEFT OUTER JOIN erosion_intensity_code c4 ON p.erosion = c4.erosion_intensity
LEFT OUTER JOIN grazing_activity_code c5 ON p.grazing = c5.grazing_activity
LEFT OUTER JOIN catchment_value_code c6 ON p.catchment = c6.catchment_value
LEFT OUTER JOIN naforma1.shrub_coverage_code c7 ON p.shrubs_coverage = c7.shrub_coverage
;