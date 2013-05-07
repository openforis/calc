SET search_path TO naforma1, public;

SELECT
    p.cluster,
    p.no AS plot_no,
    p.subplot,
    p.share,
    p.accessibility,
    p.location_x,
    p.location_y,
    p.location_srs,
    p.centre_dir,
    p.centre_dist,
    p.land_use,
    p.vegetation_type,
    p.ownership_type,
    p.stratum,
    z.aoi_label AS zone,
    r.aoi_label AS region,
    d.aoi_label AS district,
    p.zone_id,
    p.region_id,
    p.district_id,
    z.aoi_land_area AS zone_land_area,
    r.aoi_land_area AS region_land_area,
    d.aoi_land_area AS district_land_area
FROM
    naforma1._plot p
INNER JOIN
    calc.aoi d
ON
    (
        p.district_id = d.aoi_id)
INNER JOIN
    calc.aoi r
ON
    (
        p.region_id = r.aoi_id)
INNER JOIN
    calc.aoi z
ON
    (
        p.zone_id = z.aoi_id)
WHERE
    p.accessibility = '0' AND p.measurement = 'P';
    
SELECT
    naforma1._plot.cluster,
    naforma1._plot.no AS plot,
    naforma1._plot.subplot,
    naforma1.tree.tree_no,
    naforma1.tree.stem_no,
    naforma1.tree.species,
    naforma1.tree.species_scientific_name,
    naforma1.tree.dbh,
    naforma1.tree.health,
    naforma1.tree.origin,
    naforma1.tree.stump_diameter,
    naforma1.tree.stump_height,
    naforma1.tree.total_height,
    naforma1.tree.bole_height,
    naforma1._plot.land_use,
    naforma1._plot.vegetation_type,
    naforma1._plot.ownership_type,
    naforma1.tree.remarks
FROM
    naforma1.tree
INNER JOIN
    naforma1._plot
ON
    (
        naforma1.tree.plot_id = naforma1._plot.plot_id) 
WHERE _plot.measurement = 'P';
        
-- And sampling_design...


