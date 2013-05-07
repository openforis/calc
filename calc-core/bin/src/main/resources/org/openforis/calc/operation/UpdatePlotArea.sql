--update
--    calc.plot_obs p
--set
--	inclusion_probability = null,
--    hte_area = null;

update
    calc.plot_obs p
set
    inclusion_probability =  d.plot_density * pi() * 15.00 ^ 2 / 10000.00
from
    calc.stratum_plot_density d
inner join
    calc.plot_obs_view o on d.stratum_id = o.stratum_id
where
    o.plot_id = p.plot_id
and
    p.section = '1';


update 
    calc.plot_obs p
set
    hte_area = (pi() * 15.00 ^ 2 / 10000.00) / inclusion_probability;