-- Update Horvitz–Thompson tree volume
update 
    calc.tree_obs t
set
	hte_volume = est_volume / inclusion_probability
;

