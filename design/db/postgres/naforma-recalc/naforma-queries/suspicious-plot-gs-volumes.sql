SET search_path TO naforma1;

-- Agriculture, Montane Forest
SELECT
    cluster.id as cluster,
    _plot.no as plot,
    _plot.subplot,
    _tree.species,
    _tree.species_scientific_name,
    _tree.dbh,
    _tree.total_height as obs_height,
    _tree.est_height,
    _tree.volume
FROM
    _plot
INNER JOIN
    cluster
ON
    _plot.cluster_id = cluster.cluster_id
INNER JOIN
    _tree
ON
    _plot.plot_id = _tree.plot_id
WHERE
    _plot.land_use = '5'
AND _plot.vegetation_type = '101'
AND cluster.measurement = 'P'
AND growing_stock = 1 ;
    
SELECT
    cluster.id,
    _plot.no,
    _plot.subplot,
    s.stratum,
    s.expf,
    sum(_tree.volume) as vpa
FROM
    _plot
INNER JOIN
    cluster
ON
    _plot.cluster_id = cluster.cluster_id
INNER JOIN
    _tree
ON
    _plot.plot_id = _tree.plot_id
inner join
    _country_stratum s
    on s.stratum = _plot.stratum
WHERE
    _plot.land_use = '5'
AND _plot.vegetation_type = '101'
AND cluster.measurement = 'P'
AND growing_stock = 1 
group by
    cluster.id,
    _plot.no,
    _plot.subplot,
    s.stratum,
    s.expf;
    
    select avg( volume ) from _tree;
    
    select * from _country_stratum;