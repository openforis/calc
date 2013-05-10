SET search_path TO naforma1;



-- Household income matrix by source and AOI

DROP TABLE IF EXISTS _income_source_used CASCADE;

CREATE TABLE _income_source_used AS
SELECT DISTINCT
    src.household_id,
    src.income_source_code_id,
    src.type as income_source_code
FROM
    income_source src
WHERE
    src.cash_income;


DROP TABLE IF EXISTS _income_source_used_by_aoi CASCADE;

CREATE TABLE _income_source_used_by_aoi AS
SELECT 
    src.income_source_code,
    cat.income_source_label_en AS income_source,
    hh.district_id,
    hh.region_id,
    hh.zone_id,
    hh.country_id,
    count(*) as household_cnt
FROM
    _income_source_used src
INNER JOIN
    _household hh
ON
    src.household_id = hh.household_id
INNER JOIN
    income_source_code cat
ON
    src.income_source_code_id = cat.income_source_code_id
WHERE
    hh.distance_to_forest >= 10
GROUP BY
    src.income_source_code,
    cat.income_source_label_en,
    hh.district_id,
    hh.region_id,
    hh.zone_id,
    hh.country_id
    ;
    
COMMENT ON TABLE _income_source_used_by_aoi IS 'No. of households within at least 10km of forest using each income source by AOI';



DROP TABLE IF EXISTS _income_source_used_by_region CASCADE;

CREATE TABLE _income_source_used_by_region AS
SELECT 
    src.income_source_code,
    src.income_source,
    src.region_id,
    aoi.aoi_label AS region_name,
    sum(src.household_cnt) as household_cnt,
    sum(src.household_cnt) / cnt.household_cnt::double precision as proportion
FROM
    _income_source_used_by_aoi src
INNER JOIN
    calc.aoi
ON
    src.region_id = calc.aoi.aoi_id
INNER JOIN
    _households_per_region cnt
ON
    src.region_id = cnt.region_id
GROUP BY
    src.income_source_code,
    src.income_source,
    src.region_id,
    aoi.aoi_label,
    cnt.household_cnt 
    ;
COMMENT ON TABLE _income_source_used_by_region IS 'No. and % of households within at least 10km of forest using each income source, by region';
   

