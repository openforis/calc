truncate table sample_plot_aoi_stratum_cnt;
truncate table plot_section_aoi;
truncate table plot_expansion_factor;
truncate table sample_plot_aoi;

delete 
from
calc.aoi
where
aoi.aoi_parent_id is null and aoi.aoi_hierarchy_level_id = 3;

delete 
from
calc.aoi
where
aoi.aoi_total_area is null;