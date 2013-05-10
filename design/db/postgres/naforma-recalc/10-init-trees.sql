SET search_path TO naforma1;

---- DImension
DROP TABLE if exists _commercial_tree_code;
CREATE TABLE _commercial_tree_code (id INTEGER, code INTEGER, label CHARACTER VARYING);
INSERT INTO _commercial_tree_code (id, code, label) VALUES (1, 0, 'Not Commercial');
INSERT INTO _commercial_tree_code (id, code, label) VALUES (2, 1, 'Commercial');

DROP TABLE if exists _growing_stock_tree_code;
CREATE TABLE _growing_stock_tree_code (id INTEGER, code INTEGER, label CHARACTER VARYING);
INSERT INTO _growing_stock_tree_code (id, code, label) VALUES (1, 0, 'Not Growing Stock');
INSERT INTO _growing_stock_tree_code (id, code, label) VALUES (2, 1, 'Growing Stock');



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
    
-- 1 commercial tree, 0 otherwise    
alter table tree 
add column commercial_tree integer;

update tree
set commercial_tree =
    case
        when species_group != 1 and dbh_class > 2 and health != '7' then 1
        else 0
    end;        
    
alter table tree 
add column growing_stock integer;

update tree
set growing_stock =
    case
        when species_group != 1 and health != '7' then 1
        else 0
    end;            