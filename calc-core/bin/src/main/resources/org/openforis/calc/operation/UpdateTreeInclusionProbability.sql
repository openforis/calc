-- Update tree inclusion area (plot area) within the stratum
update 
    calc.tree_obs t
set
    inclusion_area = 
        case when t.dbh < 5 then (pi() * 1 ^ 2 / 10000)
            when t.dbh >= 5 and t.dbh < 10 then (pi() * 5 ^ 2 / 10000)
            when t.dbh >= 10 and t.dbh < 20 then (pi() * 10 ^ 2 / 10000)
            else (pi() * 15 ^ 2 / 10000)
        end
;

-- Update tree inclusion probability within the stratum
update 
    calc.tree_obs t
set
    inclusion_probability = d.plot_density * t.inclusion_area
from
    calc.stratum_plot_obs_density d   
inner join
    calc.tree_obs_view v on v.stratum_id = d.stratum_id
 where 
    v.id = t.id
;
