drop view if exists calc.plot_section_aoi_view;
drop view if exists calc.sample_plot_aoi_view;
drop view if exists calc.aoi_stratum_view;
drop view if exists calc.primary_plot_section_cnt_view;
--drop view if exists calc.plot_exp_factor;
--drop view if exists calc.sample_plot_visited_cnt_view;
drop view if exists calc.sample_plot_cnt_view;
drop view if exists calc.interview_categorical_value_view;
drop view if exists calc.specimen_categorical_value_view;
drop view if exists calc.specimen_view;
--drop view if exists calc.interview_numeric_value_view;
drop view if exists calc.plot_numeric_value_view;
drop view if exists calc.plot_section_view;
drop view if exists calc.ground_plot_view;
drop view if exists calc.sample_plot_view;
drop view if exists calc.plot_categorical_value_view;
drop view if exists calc.category_view;

CREATE OR REPLACE VIEW calc.category_view
AS
  SELECT v.variable_id,
         v.variable_type,
         v.variable_name,
         v.variable_order,
         cat.category_id,
         cat.category_code,
         cat.category_order
  FROM category cat
       JOIN variable v ON cat.variable_id = v.variable_id;       
comment on view calc.category_view is 'Join between category and variable';

CREATE OR REPLACE VIEW calc.plot_categorical_value_view
AS
  SELECT pc.value_id,
         pc.plot_section_id,
         pc.category_id,
         pc.original,
         cat.variable_id,
         cat.variable_type,
         cat.variable_name,
         cat.variable_order,
         cat.category_code,
         cat.category_order
  FROM plot_categorical_value pc
       JOIN category_view cat ON pc.category_id = cat.category_id
  WHERE pc.current;
comment on view calc.plot_categorical_value_view is 'Join between plot_categorical_value and category_view';
	   
CREATE OR REPLACE VIEW calc.sample_plot_view
AS
  SELECT u.survey_id,  
        
         p.obs_unit_id as plot_obs_unit_id,
         u.obs_unit_name as plot_obs_unit_name,
         str.stratum_id,
         str.stratum_no,
         c.cluster_id,
         c.cluster_code,
         c.cluster_x,
         c.cluster_y,
         p.sample_plot_id,
         p.plot_no,
         p.plot_location,
         p.plot_shape,
         p.sampling_phase,
         p.ground_plot,
         p.permanent_plot
  FROM sample_plot p
       JOIN cluster c ON p.cluster_id = c.cluster_id
       JOIN stratum str ON p.stratum_id = str.stratum_id
       JOIN observation_unit u ON p.obs_unit_id = u.obs_unit_id;
--comment on view calc.plot_categorical_value_view is 'Used to get full sampling ';	   
	   
	   
CREATE OR REPLACE VIEW calc.ground_plot_view
AS
  SELECT p.survey_id,         
         p.plot_obs_unit_id,
         p.plot_obs_unit_name,
         p.stratum_id,
         p.stratum_no,
         p.cluster_id,
         p.cluster_code,
         p.cluster_x,
         p.cluster_y,
         p.sample_plot_id,
         p.plot_no,         
         p.plot_location,
         p.plot_shape,
         p.sampling_phase,
         p.ground_plot,
         p.permanent_plot,
         ps.plot_section_id,
         ps.plot_section,
         ps.visit_type,
         ps.plot_section_survey_date,
         ps.plot_gps_reading,
         ps.plot_direction,
         ps.plot_distance,
         ps.plot_actual_location,
         ps.accessible,
         ps.step,
         ps.plot_section_shape,
         ps.plot_section_area,
         ps.plot_share,
         ps.primary_section,
         st_distance( Geography(ps.plot_actual_location), Geography(p.plot_location) ) as plot_location_deviation         
  FROM 
        calc.sample_plot_view p  
left outer join 
        calc.plot_section ps ON ps.sample_plot_id = p.sample_plot_id 
where
        p.ground_plot;
  
CREATE OR REPLACE VIEW calc.plot_section_view
AS
SELECT 
        * 
from
        calc.ground_plot_view p
where p.plot_section_id is not null;
	   
CREATE OR REPLACE VIEW calc.plot_numeric_value_view
AS
  SELECT ps.survey_id,
         ps.plot_obs_unit_id,
         ps.plot_obs_unit_name,
         ps.stratum_id,
         ps.stratum_no,
         ps.cluster_id,
         ps.cluster_code,
         ps.cluster_x,
         ps.cluster_y,
         ps.sample_plot_id,
         ps.plot_no,
         ps.plot_location,
         ps.plot_shape,
         ps.sampling_phase,
         ps.ground_plot,
         ps.permanent_plot,
         ps.plot_section_id,
         ps.plot_section,
         ps.visit_type,
         ps.plot_section_survey_date,
         ps.plot_gps_reading,
         ps.plot_direction,
         ps.plot_distance,
         ps.plot_actual_location,
         ps.accessible,
         ps.step,
         ps.plot_section_shape,
         ps.plot_section_area,
         ps.plot_share,
         v.variable_name,
         v.variable_id,
         pm.value,
         pm.original
  FROM plot_numeric_value pm
       JOIN plot_section_view ps ON pm.plot_section_id = ps.plot_section_id
       JOIN variable v ON pm.variable_id = v.variable_id
  WHERE pm.current;


create or replace view calc.specimen_view
as
select    
    p.survey_id,
    p.stratum_id,
    p.stratum_no,
    p.plot_obs_unit_id,
    p.plot_obs_unit_name,
    p.visit_type,
    p.cluster_id,
    p.cluster_code,
    p.sample_plot_id,
    p.plot_no,
    p.plot_section_id,
    p.plot_section,
    p.plot_section_survey_date,
    s.specimen_id,
    s.obs_unit_id as specimen_obs_unit_id,
    u.obs_unit_name as specimen_obs_unit_name,
    s.specimen_no,
    s.specimen_taxon_id,
    s.specimen_survey_date,
    s.specimen_exp_factor,
    t.taxon_code,
    t.taxon_parent_id,
    t.scientific_name
from
    calc.specimen s
inner join
    calc.plot_section_view p on s.plot_section_id = p.plot_section_id
inner join
    calc.observation_unit u on s.obs_unit_id = u.obs_unit_id
left outer join
    calc.taxon t on s.specimen_taxon_id = t.taxon_id;


create or replace view calc.specimen_categorical_value_view
as
select 
    cv.value_id,
    cv.specimen_id,
    cv.category_id,
    cv.original,
    c.variable_id,
    c.category_code,
    c.category_label,
    c.category_order,
    c.banding_interval,
    c.banding_source_variable_id,
    c.category_description
from
    calc.specimen_categorical_value cv
inner join
    calc.category c on cv.category_id = c.category_id 
where
    cv.current;

CREATE OR REPLACE VIEW calc.interview_categorical_value_view
AS
  SELECT cv.value_id,
         cv.interview_id,
         cv.category_id,
         cv.original,
         cat.variable_id
  FROM interview_categorical_value cv
       JOIN category cat ON cv.category_id = cat.category_id
  WHERE cv.current;


//create or replace view calc.specimen_numeric_value_view
//as
//select 
//    v.value_id,
//    v.specimen_id,
//    v.variable_id,
//    v.value,
//    v.original,
//    va.obs_unit_id,
//    va.variable_name,
//    va.variable_type,
//    va.variable_order,
//    va.variable_label,
//    va.variable_description
//from
//    calc.specimen_numeric_value v
//inner join
//    calc.variable va on va.variable_id = v.variable_id
// where v.current;
--
--create or replace view calc.sample_plot_visited_cnt_view
--as
--select
--    s.stratum_no,
--    s.stratum_id,
--    a.aoi_id,    
--    p.plot_obs_unit_id,
--    count(p.sample_plot_id) as plot2_cnt
--    -- count(p.sample_plot_id) / (select count(sample_plot_id) from calc.sample_plot)::numeric as prop,
--    -- count(p.sample_plot_id) / (select count(sample_plot_id) from calc.sample_plot)::numeric * (select aoi_area from calc.aoi where aoi_id = 1)  as area    
--from
--    calc.sample_plot_view p
--inner join
--    calc.stratum s on p.stratum_id = s.stratum_id
--inner join
--    calc.plot_section ps on p.sample_plot_id = ps.sample_plot_id
--inner join
--    calc.aoi a on a.aoi_id = 1
--where
--    ps.visit_type = 'P'
--and
--    ps.primary_section
--//where 
--//    p.survey_id = 2
--//    and 
--//    p.plot_obs_unit_id = 2
--group by
--    s.stratum_id,
--    s.stratum_no,
--    a.aoi_id,
--    p.plot_obs_unit_id
--order by
--    s.stratum_no;
--
--create view calc.plot_exp_factor
--as
--select 
--    c.stratum_id,
--    c.stratum_no,
--    c.aoi_id,
--    (c1.plot1_cnt / 
--        (select 
--            sum(c2.plot1_cnt) 
--        from 
--            calc.sample_plot_cnt_view c2 
--        )
--    ) as prop,
--    a.aoi_area * 
--    (c1.plot1_cnt / 
--        (select 
--            sum(c2.plot1_cnt) 
--        from 
--            calc.sample_plot_cnt_view c2 
--        )
--    )
--    as area , 
--    c1.plot1_cnt,
--    c.plot2_cnt ,
--    a.aoi_area * 
--    (c1.plot1_cnt / 
--        (select 
--            sum(c2.plot1_cnt) 
--        from 
--            calc.sample_plot_cnt_view c2 
--        )
--    ) / c.plot2_cnt as expf
--from
--    calc.sample_plot_visited_cnt_view c
--inner join
--    calc.sample_plot_cnt_view c1 on c1.stratum_id = c.stratum_id 
--inner join 
--    calc.aoi a on c.aoi_id = a.aoi_id;

create view calc.sample_plot_cnt_view
as                                
select        
        p.stratum_id,
        a.aoi_id,
        count(p.sample_plot_id)
from
        calc.sample_plot_aoi pa
inner join
        calc.sample_plot_view p on pa.sample_plot_id = p.sample_plot_id
inner join
        calc.aoi a on pa.aoi_id = a.aoi_id
group by        
        p.stratum_id,
        a.aoi_id;
        
create view calc.primary_plot_section_cnt_view
as
        select        
                pa.aoi_id,
                p.stratum_id,        
                count(pa.plot_section_id)
        from
                calc.plot_section_aoi pa
        inner join
                calc.plot_section_view p on pa.plot_section_id = p.plot_section_id
--        inner join
--                calc.stratum s on p.stratum_id = s.stratum_id
        where
                p.primary_section
        and 
                p.visit_type = 'P'
        and 
                p.accessible
        group by
                pa.aoi_id,
                p.stratum_id;

create view calc.aoi_stratum_view
as
select 
        p.aoi_id,
        p.stratum_id,        
        p.count as sample_plot_cnt,        
        p.count / p1_aoi_cnt.cnt as aoi_share,
        coalesce( p2.count , 0) as obs_plot_cnt,
        a.aoi_land_area * ( p.count / p1_aoi_cnt.cnt ) as area
from
        calc.sample_plot_cnt_view p
inner join
        (
        select 
                c.aoi_id,
                sum(c.count) as cnt
        from 
                calc.sample_plot_cnt_view c 
        group by
                c.aoi_id 
        ) as p1_aoi_cnt
on
   p1_aoi_cnt.aoi_id = p.aoi_id               
left outer join 
        calc.primary_plot_section_cnt_view p2 on p.aoi_id = p2.aoi_id and p.stratum_id = p2.stratum_id
inner join
        calc.aoi a on p.aoi_id = a.aoi_id
;

create view
    calc.plot_section_aoi_view
    (
        plot_section_id,
        aoi_id
    ) as
select
    p.plot_section_id,
    a.aoi_id
from
    (aoi a
join
    plot_section p
on
    (
        st_contains(a.aoi_shape, p.plot_actual_location)))
join 
        calc.aoi_hierarchy_level l
        on l.aoi_hierarchy_level_id = a.aoi_hierarchy_level_id
where 
        l.aoi_hierarchy_level_rank = (select max(aoi_hierarchy_level_rank) from calc.aoi_hierarchy_level) 
    ;

create view
    calc.sample_plot_aoi_view
    (
        sample_plot_id,
        aoi_id
    ) as
select
    p.sample_plot_id,
    a.aoi_id
from
    aoi a
join
    sample_plot p
on   
        st_contains(a.aoi_shape, p.plot_location)
join 
        calc.aoi_hierarchy_level l
        on l.aoi_hierarchy_level_id = a.aoi_hierarchy_level_id
where 
        l.aoi_hierarchy_level_rank = (select max(aoi_hierarchy_level_rank) from calc.aoi_hierarchy_level)        
;