-- Default direction and distance
update plot_section
set plot_direction = 0, plot_distance = 0
where (plot_section.plot_direction=99 and plot_section.plot_distance=99) 
        or (plot_section.plot_direction is null) or (plot_section.plot_distance is null)
        or (plot_section.plot_distance=0);

-- Set plots with observations as accessible
update 
        calc.plot_section
set
        accessible = true        
where 
        not(accessible)
and
        exists (select specimen_id from calc.specimen_view sv where sv.plot_section_id = plot_section.plot_section_id);