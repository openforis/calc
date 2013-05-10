SET search_path TO naforma1;

-- E1.

DROP TABLE IF EXISTS _food_source_occurence;

CREATE TABLE _food_source_occurence AS
SELECT DISTINCT
    fs.food_source_id,
    fs.household_id,
    fsc.food_source,
    fsc.food_source_label_en
FROM
    naforma1.food_source fs
INNER JOIN
    naforma1.food_source_code fsc
ON
    fs.food_source_code_id = fsc.food_source_code_id
WHERE
    fs.duration > 0;

DROP TABLE IF EXISTS _food_source_agg;

CREATE TABLE _food_source_agg AS  
SELECT
--    naforma1._household.country_id,
--    naforma1._household.zone_id,
--    naforma1._household.region_id,
--    naforma1._household.district_id,
    cnt.aoi_label  AS country,
    zone.aoi_label AS zone,
    reg.aoi_label  AS region,
    dst.aoi_label  AS dist,
    round(naforma1._household.distance_to_forest) as distance_to_forest,
    naforma1._food_source_occurence.food_source,
    naforma1._food_source_occurence.food_source_label_en,
    COUNT(naforma1._food_source_occurence.food_source_id)
FROM
    naforma1._household
INNER JOIN
    naforma1._food_source_occurence
ON
    naforma1._household.household_id = naforma1._food_source_occurence.household_id
INNER JOIN
    calc.aoi cnt
ON
     naforma1._household.country_id = cnt.aoi_id
INNER JOIN
    calc.aoi zone
ON
    naforma1._household.zone_id = zone.aoi_id
INNER JOIN
    calc.aoi reg
ON
    naforma1._household.region_id = reg.aoi_id
INNER JOIN
    calc.aoi dst
ON
    naforma1._household.district_id = dst.aoi_id
GROUP BY
--    naforma1._household.country_id,
--    naforma1._household.zone_id,
--    naforma1._household.region_id,
--    naforma1._household.district_id,
    cnt.aoi_label,
    zone.aoi_label,
    reg.aoi_label,
    dst.aoi_label,
    round(naforma1._household.distance_to_forest),
    naforma1._food_source_occurence.food_source,
    naforma1._food_source_occurence.food_source_label_en ;
    


-- E2.

DROP TABLE IF EXISTS _food_shortage_month_agg;

CREATE TABLE _food_shortage_month_agg AS  
SELECT
--    naforma1._household.country_id,
--    naforma1._household.zone_id,
--    naforma1._household.region_id,
--    naforma1._household.district_id,
    cnt.aoi_label  AS country,
    zone.aoi_label AS zone,
    reg.aoi_label  AS region,
    dst.aoi_label  AS dist,
    round(naforma1._household.distance_to_forest) as distance_to_forest,
    naforma1.household_food_shortage_month.code as month_no,
    naforma1.month_code.month_label_en as month,
    COUNT(naforma1.household_food_shortage_month.household_food_shortage_month_id)
FROM
    naforma1._household
INNER JOIN
    naforma1.household_food_shortage_month
ON
    naforma1._household.household_id = household_food_shortage_month.household_id
INNER JOIN
    naforma1.month_code
ON
    household_food_shortage_month.month_code_id = month_code.month_code_id
INNER JOIN
    calc.aoi cnt
ON
     naforma1._household.country_id = cnt.aoi_id
INNER JOIN
    calc.aoi zone
ON
    naforma1._household.zone_id = zone.aoi_id
INNER JOIN
    calc.aoi reg
ON
    naforma1._household.region_id = reg.aoi_id
INNER JOIN
    calc.aoi dst
ON
    naforma1._household.district_id = dst.aoi_id
GROUP BY
--    naforma1._household.country_id,
--    naforma1._household.zone_id,
--    naforma1._household.region_id,
--    naforma1._household.district_id,
    cnt.aoi_label,
    zone.aoi_label,
    reg.aoi_label,
    dst.aoi_label,
    round(naforma1._household.distance_to_forest),
    naforma1.household_food_shortage_month.code,
    naforma1.month_code.month_label_en
    ;








-- E3.

DROP TABLE IF EXISTS _food_shortage_month_agg;

CREATE TABLE _food_shortage_month_agg AS  
SELECT
--    naforma1._household.country_id,
--    naforma1._household.zone_id,
--    naforma1._household.region_id,
--    naforma1._household.district_id,
    cnt.aoi_label  AS country,
    zone.aoi_label AS zone,
    reg.aoi_label  AS region,
    dst.aoi_label  AS dist,
    round(naforma1._household.distance_to_forest) as distance_to_forest,
    naforma1.household_food_shortage_month.code as month_no,
    naforma1.month_code.month_label_en as month,
    COUNT(naforma1.household_food_shortage_month.household_food_shortage_month_id)
FROM
    naforma1._household
INNER JOIN
    naforma1.household_food_shortage_month
ON
    naforma1._household.household_id = household_food_shortage_month.household_id
INNER JOIN
    naforma1.month_code
ON
    household_food_shortage_month.month_code_id = month_code.month_code_id
INNER JOIN
    calc.aoi cnt
ON
     naforma1._household.country_id = cnt.aoi_id
INNER JOIN
    calc.aoi zone
ON
    naforma1._household.zone_id = zone.aoi_id
INNER JOIN
    calc.aoi reg
ON
    naforma1._household.region_id = reg.aoi_id
INNER JOIN
    calc.aoi dst
ON
    naforma1._household.district_id = dst.aoi_id
GROUP BY
--    naforma1._household.country_id,
--    naforma1._household.zone_id,
--    naforma1._household.region_id,
--    naforma1._household.district_id,
    cnt.aoi_label,
    zone.aoi_label,
    reg.aoi_label,
    dst.aoi_label,
    round(naforma1._household.distance_to_forest),
    naforma1.household_food_shortage_month.code,
    naforma1.month_code.month_label_en
    ;


