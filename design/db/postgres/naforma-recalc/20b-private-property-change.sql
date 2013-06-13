SET search_path TO naforma1_se;

--alter table _household 
--drop column private_property_change;
--
alter table _household 
add column private_property_change_amount_ha numeric;
--
update _household
set private_property_change_amount_ha = private_property_change_amount
where private_property_change_amount_unit_name='ha';
--
update _household
set private_property_change_amount_ha = private_property_change_amount * 0.404686 -- convert to ha
where private_property_change_amount_unit_name='ac';
--
--alter table _household 
--add column private_property_increase_ha numeric;
--
--update _household
--set private_property_increase_ha = private_property_change_amount_ha
--where private_property_change_type = 'A';
--
--alter table _household 
--add column private_property_decrease_ha numeric;
--
--update _household
--set private_property_decrease_ha = private_property_change_amount_ha
--where private_property_change_type = 'B';
--
--
--alter table _household 
--add column private_property_change numeric;
--
--update _household
--set private_property_change = private_property_change_amount
--where private_property_change_amount_unit_name='ha'
--    and private_property_change_type IN ('A','B');
--
--update _household
--set private_property_change = private_property_change_amount * 0.404686 -- convert to ha
--where private_property_change_amount_unit_name='ac'
--    and private_property_change_type IN ('A','B');
--
--update _household
--set private_property_change = -private_property_change
--where private_property_change_type = 'B';
--
--update _household
--set private_property_change = 0
--where private_property_change_type = 'C';
