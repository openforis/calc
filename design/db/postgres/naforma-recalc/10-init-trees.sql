SET search_path TO naforma1, public;

alter table tree 
add column dbh_class integer;

update tree
set dbh_class = case 
                    when dbh < 5 then 1
                    when dbh < 10 then 2
                    when dbh < 20 then 3
                    else 4
                end;    


-- 1 baobab
-- 99 others
alter table tree 
add column species_group integer;

update tree
set species_group =
    case
        when species like 'ADA%' then 1
        else 99
    end;    