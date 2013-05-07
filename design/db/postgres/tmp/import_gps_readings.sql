with l as (
        SELECT
            plot_section_id,
            ST_Transform(ST_SetSRID(ST_Point(location_x, location_y), (substring(location_srs_id from '[0-9]+$')::integer)),4326) as loc
        FROM
            calc.plottmp p
        INNER JOIN
            calc.plot_section_view v
        ON
           p.cluster_id = v.cluster_code and p.no = v.plot_no
) 
update plot_section
set plot_gps_reading = l.loc
from l
where plot_section.plot_section_id = l.plot_section_id;