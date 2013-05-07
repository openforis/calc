-- GPS reading = theoretical location but distance recorded
select
   pv.cluster_code, 
   pv.plot_no,
   s.x as planned_x,
   s.y as planned_y,
   s.zone as planned_zone,
   t.location_x::integer as gps_x,
   t.location_y::integer as gps_y,
   substring(t.location_srs_id from '[0-9]+$')::integer - 21000 as gps_zone,
   pv.plot_direction as reported_direction,
   pv.plot_distance as reported_distance
from
   plot_section_view pv
inner join
   sdtmp s on s.cluster_id = pv.cluster_code and s.plot_no = pv.plot_no
inner join
   plottmp t on t.cluster_id = pv.cluster_code and t.no = pv.plot_no
where
   pv.primary_section and pv.accessible and s.x = t.location_x and s.y = t.location_y and  
        (substring(t.location_srs_id from '[0-9]+$')::integer - 21000) = s.zone and
         pv.plot_distance > 0;
   
-- Other plots over 10m away
select
   pv.cluster_code, 
   pv.plot_no,
   s.x as planned_x,
   s.y as planned_y,
   s.zone as planned_zone,
   t.location_x::integer as gps_x,
   t.location_y::integer as gps_y,
   substring(t.location_srs_id from '[0-9]+$')::integer - 21000 as gps_zone,
   pv.plot_direction as reported_direction,
   pv.plot_distance as reported_distance,
   round(ST_Distance_Sphere(pv.plot_gps_reading, pv.plot_location)::numeric,2) as dist_gps_to_planned,
   round(ST_Distance_Sphere(pv.plot_actual_location, pv.plot_location)::numeric,2) as dist_corrected_to_planned
from
   plot_section_view pv
inner join
   sdtmp s on s.cluster_id = pv.cluster_code and s.plot_no = pv.plot_no
inner join
   plottmp t on t.cluster_id = pv.cluster_code and t.no = pv.plot_no
where
   pv.primary_section and ST_Distance_Sphere(pv.plot_actual_location, pv.plot_location) >= 11 and pv.accessible
   and (s.x != t.location_x or s.y != t.location_y or (substring(t.location_srs_id from '[0-9]+$')::integer - 21000) != s.zone or pv.plot_distance = 0);