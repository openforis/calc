SET search_path TO naforma1, public;

-- Unique species in each plot

drop table if exists _plot_distinct_species_scientific_name;

create table _plot_distinct_species_scientific_name as
SELECT DISTINCT
--    t.species,
    c.id as cluster,    
    p.no as plot,
    t.species_scientific_name
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
    c.measurement = 'P'  and p.accessibility='0';
    
drop table if exists _plot_distinct_species_code;

create table _plot_distinct_species_code as
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
    c.measurement = 'P' and p.accessibility = '0';
    
-- Number of unique species in each plot 
    
drop table if exists _plot_distinct_species_scientific_name_cnt;

create table _plot_distinct_species_scientific_name_cnt as
SELECT 
--    t.species,
    cluster,
    plot,
    count(*) as cnt
FROM
    _plot_distinct_species_scientific_name
GROUP BY
    cluster, plot;
    
drop table if exists _plot_distinct_species_code_cnt;

create table _plot_distinct_species_code_cnt as
SELECT 
--    t.species,
    cluster,
    plot,
    count(*) as cnt
FROM
    _plot_distinct_species_code
GROUP BY
    cluster, plot;  
   
 -- Max # species/plot in each cluster
     
drop table if exists _cluster_max_plot_distinct_species_scientific_name_cnt;

create table _cluster_max_plot_distinct_species_scientific_name_cnt as
SELECT 
--    t.species,
    cluster,
    cl.center_location,
    max(cnt) as maxcnt
FROM
    _plot_distinct_species_scientific_name_cnt c
inner join calc.cluster_location cl
    on c.cluster = cl.code
GROUP BY
    cluster, 
    cl.center_location;

drop table if exists _cluster_max_plot_distinct_species_code_cnt;

create table _cluster_max_plot_distinct_species_code_cnt as
SELECT 
--    t.species,
    cluster,
    cl.center_location,
    max(cnt) as maxcnt
FROM
    _plot_distinct_species_code_cnt c
inner join calc.cluster_location cl
    on c.cluster = cl.code
GROUP BY
    cluster, 
    cl.center_location;


drop table if exists _cluster_tree_cnt;

create table _cluster_tree_cnt as
SELECT
    cluster_id,
    count(*) as tree_cnt
FROM
    naforma1._plot p
INNER JOIN
    naforma1._tree t
ON
     p.plot_id = t.plot_id
group by
    p.cluster_id;

drop table if exists _cluster_distinct_species_code;

create table _cluster_distinct_species_code as
SELECT DISTINCT
    c.id as cluster,
    c.cluster_id as cluster_id,
    t.species
FROM
    naforma1._tree t
INNER JOIN
    naforma1._plot p
ON
    t.plot_id = p.plot_id
INNER JOIN
    naforma1.cluster c
ON
    p.cluster_id = c.cluster_id
WHERE
    c.measurement = 'P';

drop table if exists _cluster_distinct_species_code_cnt;

create table _cluster_distinct_species_code_cnt as
SELECT 
--    t.species,
    cluster,
    cluster_id,
    count(*) as cnt
FROM
    _cluster_distinct_species_code
GROUP BY
    cluster,
    cluster_id;    
    
drop table if exists _cluster_species_richness;

create table _cluster_species_richness as
SELECT 
    c.cluster_id,
    c.cluster,
    cl.center_location,
    c.cnt as distinct_species,
    tc.tree_cnt as tree_cnt,
    c.cnt::float / tc.tree_cnt::float as species_per_tree
FROM
    _cluster_distinct_species_code_cnt c
inner join 
    _cluster_tree_cnt tc
on
    c.cluster_id = tc.cluster_id        
inner join calc.cluster_location cl
    on c.cluster = cl.code;
    