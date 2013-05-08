SET search_path TO naforma1-- Forest product usage matrix by AOI

DROP TABLE IF EXISTS _product_used CASCADE;

CREATE TABLE _product_used AS
SELECT DISTINCT
    p.household_id,
    p.category,
    p.product_category_code_id
FROM
    product_used p;


DROP TABLE IF EXISTS _product_used_by_aoi CASCADE;

CREATE TABLE _product_used_by_aoi AS
SELECT 
    p.category AS category_code,
    c.product_category_label_en AS category,
    h.district_id,
    h.region_id,
    h.zone_id,
    h.country_id,
    count(*) as household_cnt
FROM
    _product_used p
INNER JOIN
    _household h
ON
    p.household_id = h.household_id
INNER JOIN
    product_category_code c
ON
    p.product_category_code_id = c.product_category_code_id
WHERE
    h.distance_to_forest >= 10
GROUP BY
    p.category,
    c.product_category_label_en,
    h.district_id,
    h.region_id,
    h.zone_id,
    h.country_id
    ;
    
COMMENT ON TABLE _product_used_by_aoi IS 'No. of households within at least 10km of forest using each forest product by AOI';
   



DROP TABLE IF EXISTS _product_used_by_district CASCADE;

CREATE TABLE _product_used_by_district AS
SELECT 
    p.category_code,
    p.category,
    p.district_id,
    aoi.aoi_label AS district_name,
    sum(p.household_cnt) as household_cnt,
    sum(p.household_cnt) / cnt.household_cnt::double precision as proportion
FROM
    _product_used_by_aoi p
INNER JOIN
    calc.aoi
ON
    p.district_id = calc.aoi.aoi_id
INNER JOIN
    _households_per_district cnt
ON
    p.district_id = cnt.district_id        
GROUP BY
    p.category_code,
    p.category,
    p.district_id,
    aoi.aoi_label,
    cnt.household_cnt 
    ;

COMMENT ON TABLE _product_used_by_district IS 'No. and % of households within at least 10km of forest using each forest product, by district';
   

DROP TABLE IF EXISTS _product_used_by_region CASCADE;

CREATE TABLE _product_used_by_region AS
SELECT 
    p.category_code,
    p.category,
    p.region_id,
    aoi.aoi_label AS region_name,
    sum(p.household_cnt) as household_cnt,
    sum(p.household_cnt) / cnt.household_cnt::double precision as proportion
FROM
    _product_used_by_aoi p
INNER JOIN
    calc.aoi
ON
    p.region_id = calc.aoi.aoi_id
INNER JOIN
    _households_per_region cnt
ON
    p.region_id = cnt.region_id
GROUP BY
    p.category_code,
    p.category,
    p.region_id,
    aoi.aoi_label,
    cnt.household_cnt 
    ;
COMMENT ON TABLE _product_used_by_region IS 'No. and % of households within at least 10km of forest using each forest product, by region';
   


DROP TABLE IF EXISTS _product_used_by_zone CASCADE;

CREATE TABLE _product_used_by_zone AS
SELECT 
    p.category_code,
    p.category,
    p.zone_id,
    aoi.aoi_label AS zone_name,
    sum(p.household_cnt) as household_cnt,
    sum(p.household_cnt) / cnt.household_cnt::double precision as proportion
FROM
    _product_used_by_aoi p
INNER JOIN
    calc.aoi
ON
    p.zone_id = calc.aoi.aoi_id
INNER JOIN
    _households_per_zone cnt
ON
    p.zone_id = cnt.zone_id
GROUP BY
    p.category_code,
    p.category,
    p.zone_id,
    aoi.aoi_label,
    cnt.household_cnt 
    ;
COMMENT ON TABLE _product_used_by_zone IS 'No. and % of households within at least 10km of forest using each forest product, by zone';
   




DROP TABLE IF EXISTS _product_used_by_country CASCADE;

CREATE TABLE _product_used_by_country AS
SELECT 
    p.category_code,
    p.category,
    sum(p.household_cnt) as household_cnt,
    sum(p.household_cnt) / cnt.household_cnt::double precision as proportion
FROM
    _product_used_by_aoi p
INNER JOIN
    _households_per_country cnt
ON
    p.country_id = cnt.country_id
GROUP BY
    p.category_code,
    p.category,
    cnt.household_cnt 
    ;
COMMENT ON TABLE _product_used_by_country IS 'Total no. and % of households within at least 10km of forest using each forest product';
   

;
