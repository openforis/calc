select
    naforma1.cluster.id          as cluster,
    naforma1.cluster.measurement as measurement,
    naforma1.plot.no             as plot,
    naforma1.plot.subplot,
    naforma1.tree.tree_no,
    naforma1.tree.stem_no,
    naforma1.tree.species                 as species_code,
    naforma1.tree.species_scientific_name as scientific_name,
    naforma1.tree.total_height,
    naforma1.tree.bole_height
from
    naforma1.tree
inner join
    naforma1.plot
on
    (
        naforma1.tree.plot_id = naforma1.plot.plot_id)
inner join
    naforma1.cluster
on
    (
        naforma1.plot.cluster_id = naforma1.cluster.cluster_id)
WHERE  total_height >= 40 or bole_height > total_height