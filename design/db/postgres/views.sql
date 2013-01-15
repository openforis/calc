//drop view if exists calc.specimen_numeric_value_view;
drop view if exists calc.sample_plot_cnt_view;
drop view if exists calc.specimen_categorical_value_view;
drop view if exists calc.specimen_view;
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

CREATE OR REPLACE VIEW calc.plot_categorical_value_view
AS
  SELECT pc.value_id,
         pc.plot_section_id,
         pc.category_id,
         pc.computed,
         cat.variable_id,
         cat.variable_type,
         cat.variable_name,
         cat.variable_order,
         cat.category_code,
         cat.category_order
  FROM plot_categorical_value pc
       JOIN category_view cat ON pc.category_id = cat.category_id;
	   
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
         p.sample_plot_location,
         p.sample_plot_shape,
         p.sampling_phase,
         p.ground_plot,
         p.permanent_plot
  FROM sample_plot p
       JOIN cluster c ON p.cluster_id = c.cluster_id
       JOIN stratum str ON p.stratum_id = str.stratum_id
       JOIN observation_unit u ON p.obs_unit_id = u.obs_unit_id;
	   
	   
	   
CREATE OR REPLACE VIEW calc.ground_plot_view
AS
  SELECT sample_plot_view.survey_id,        
         sample_plot_view.plot_obs_unit_id,
         sample_plot_view.plot_obs_unit_name,
         sample_plot_view.stratum_id,
         sample_plot_view.stratum_no,
         sample_plot_view.cluster_id,
         sample_plot_view.cluster_code,
         sample_plot_view.cluster_x,
         sample_plot_view.cluster_y,
         sample_plot_view.sample_plot_id,
         sample_plot_view.plot_no,
         sample_plot_view.sample_plot_location,
         sample_plot_view.sample_plot_shape,
         sample_plot_view.sampling_phase,
         sample_plot_view.ground_plot,
         sample_plot_view.permanent_plot
  FROM sample_plot_view 
  WHERE sample_plot_view.ground_plot;
  
CREATE OR REPLACE VIEW calc.plot_section_view
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
         p.sample_plot_location,
         p.sample_plot_shape,
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
         ps.plot_location,
         ps.accessible,
         ps.step,
         ps.plot_section_shape,
         ps.plot_section_area,
         ps.plot_share
  FROM plot_section ps
       JOIN ground_plot_view p ON ps.sample_plot_id = p.sample_plot_id;
	   
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
         ps.sample_plot_location,
         ps.sample_plot_shape,
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
         ps.plot_location,
         ps.accessible,
         ps.step,
         ps.plot_section_shape,
         ps.plot_section_area,
         ps.plot_share,
         v.variable_name,
         pm.value,
         pm.computed
  FROM plot_numeric_value pm
       JOIN plot_section_view ps ON pm.plot_section_id = ps.plot_section_id
       JOIN variable v ON pm.variable_id = v.variable_id;


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
    s.specimen_id,
    s.obs_unit_id as specimen_obs_unit_id,
    u.obs_unit_name as specimen_obs_unit_name,
    s.specimen_no,
    s.specimen_taxon_id,
    s.specimen_survey_date    
from
    calc.specimen s
inner join
    calc.plot_section_view p on s.plot_section_id = p.plot_section_id
inner join
    calc.observation_unit u on s.obs_unit_id = u.obs_unit_id;


create or replace view calc.specimen_categorical_value_view
as
select 
    cv.value_id,
    cv.specimen_id,
    cv.category_id,
    cv.computed,
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
    calc.category c on cv.category_id = c.category_id ;


//create or replace view calc.specimen_numeric_value_view
//as
//select 
//    v.value_id,
//    v.specimen_id,
//    v.variable_id,
//    v.value,
//    v.computed,
//    va.obs_unit_id,
//    va.variable_name,
//    va.variable_type,
//    va.variable_order,
//    va.variable_label,
//    va.variable_description
//from
//    calc.specimen_numeric_value v
//inner join
//    calc.variable va on va.variable_id = v.variable_id;

create or replace view calc.sample_plot_cnt_view
as
select
    s.stratum_no,
    s.stratum_id,
    1 as aoi_id,    
    p.plot_obs_unit_id,
    count(p.sample_plot_id) as plot1_cnt
    -- count(p.sample_plot_id) / (select count(sample_plot_id) from calc.sample_plot)::numeric as prop,
    -- count(p.sample_plot_id) / (select count(sample_plot_id) from calc.sample_plot)::numeric * (select aoi_area from calc.aoi where aoi_id = 1)  as area    
from
    calc.sample_plot_view p
inner join
    calc.stratum s on p.stratum_id = s.stratum_id
//where 
//    p.survey_id = 2
//    and 
//    p.plot_obs_unit_id = 2
group by
    s.stratum_id,
    s.stratum_no,
    p.plot_obs_unit_id
order by
    s.stratum_no;
