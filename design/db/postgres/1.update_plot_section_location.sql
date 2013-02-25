with t as (
        select
                p.plot_section_id,
                case 
                        when 
                                p.plot_distance > 0 and p.plot_direction is not null
                                then
                                        st_project( Geography(p.plot_gps_reading), p.plot_distance, radians(p.plot_direction) )::geometry
                        else 
                                p.plot_gps_reading
               end as plot_location
        from
                calc.plot_section p
)
update
        calc.plot_section p
set    
        plot_actual_location = t.plot_location
from
        t 
where  p.plot_section_id = t.plot_section_id;                     
        
