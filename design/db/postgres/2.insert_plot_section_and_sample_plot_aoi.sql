truncate table calc.plot_section_aoi;

with recursive ah(aoi_parent, aoi, plot_section_id) as (
        select
                a.aoi_parent_id, a.aoi_id, p.plot_section_id
        from 
                calc.aoi a
        join
                calc.plot_section_aoi_view p
                on a.aoi_id = p.aoi_id                
        join 
                calc.aoi_hierarchy_level l
                on l.aoi_hierarchy_level_id = a.aoi_hierarchy_level_id
        where 
                l.aoi_hierarchy_level_rank = (select max(aoi_hierarchy_level_rank) from calc.aoi_hierarchy_level)
        
        union all
        
        select
                a.aoi_parent_id, a.aoi_id, ah.plot_section_id
        from
                calc.aoi a, ah
        where 
                a.aoi_id = ah.aoi_parent
) 
insert into
        calc.plot_section_aoi
(plot_section_id, aoi_id)
select
        plot_section_id, aoi
from
        ah;



truncate table calc.sample_plot_aoi;

with recursive ah(aoi_parent, aoi, sample_plot_id) as (
        select
                a.aoi_parent_id, a.aoi_id, p.sample_plot_id
        from 
                calc.aoi a
        join
                calc.sample_plot_aoi_view p
                on a.aoi_id = p.aoi_id                
        join 
                calc.aoi_hierarchy_level l
                on l.aoi_hierarchy_level_id = a.aoi_hierarchy_level_id
        where 
                l.aoi_hierarchy_level_rank = (select max(aoi_hierarchy_level_rank) from calc.aoi_hierarchy_level)
        
        union all
        
        select
                a.aoi_parent_id, a.aoi_id, ah.sample_plot_id
        from
                calc.aoi a, ah
        where 
                a.aoi_id = ah.aoi_parent
--        and 
--                a.aoi_parent_id is not null
) 
insert into
        calc.sample_plot_aoi
(sample_plot_id, aoi_id)
select
        sample_plot_id, aoi
from
        ah;