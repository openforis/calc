SET search_path TO naforma1, public;

drop table if exists _tmp_distinct_species_in_forest;

create table _tmp_distinct_species_in_forest as
SELECT DISTINCT
    c.id as cluster,
    p.no as plot,
    t.species
FROM
    naforma1._tree t
INNER JOIN
    naforma1._plot p
ON
    (
        t.plot_id = p.plot_id)
INNER JOIN
    naforma1.cluster c
ON
    (
        p.cluster_id = c.cluster_id)
WHERE
    c.measurement = 'P' and p.accessibility = '0' and p.vegetation_type::integer <300;

drop table if exists _tmp_plot_distinct_species_in_forest_cnt;

create table _tmp_plot_distinct_species_in_forest_cnt as
SELECT 
    cluster,
    plot,
    count(*) as cnt
FROM
    _tmp_distinct_species_in_forest
GROUP BY
    cluster, plot;

select stddev_samp(cnt) from _tmp_plot_distinct_species_in_forest_cnt