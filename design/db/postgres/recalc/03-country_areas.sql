update _plot
set vegetation_type = -1
where vegetation_type is null;


SELECT 
    a.land_use as land_use_code,
    a.vegetation_type as vt_code,
    vt.category_label as vegetation_type,
    lu.category_label as land_use,
    round(sum(est_area)) as area
FROM (
SELECT
    p.country_id,
    p.stratum,
    p.vegetation_type,
    p.land_use,
    s.expf * count(*) as est_area
FROM
    naforma1._plot p
INNER JOIN
    naforma1._country_stratum s
ON
    (
        p.stratum = s.stratum)
AND (
        p.country_id = s.country_id)
WHERE
     p.accessibility = '0' and p.measurement = 'P' and p.subplot = 'A'
GROUP BY
    p.country_id,
    p.stratum,
    p.vegetation_type,
    p.land_use,
    s.expf
) as a
left outer join calc.category vt on vt.category_code = vegetation_type and vt.variable_id = 10
left outer join calc.category lu on lu.category_code = land_use and lu.variable_id = 9
GROUP BY 
    a.land_use,
    a.vegetation_type,
    vt.category_label,
    lu.category_label
order by a.land_use, a.vegetation_type;