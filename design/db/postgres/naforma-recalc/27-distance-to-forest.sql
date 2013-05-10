alter table naforma1._household_agg
add column distance_to_forest_km numeric;

update naforma1._household_agg
set distance_to_forest_km = round((distance_to_forest/1000.0)::numeric, 1);

alter table naforma1._household_agg
add column distance_to_forest_log10 numeric;

update naforma1._household_agg
set distance_to_forest_log10 = 0
where distance_to_forest_km = 0;

update naforma1._household_agg
set distance_to_forest_log10 = 10^round(log(distance_to_forest_km))
where distance_to_forest_km > 0;

drop table if exists _distance_to_forest_cum ;

create table _distance_to_forest_cum as
select distinct
    h1.distance_to_forest_km, 
    (select count(*) from naforma1._household_agg h2 where  h2.distance_to_forest_km <= h1.distance_to_forest_km) as freq
from 
    naforma1._household_agg h1;
         
         