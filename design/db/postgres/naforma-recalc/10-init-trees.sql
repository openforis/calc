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

DROP TABLE if exists _dbh_class;
CREATE TABLE _dbh_class ( code CHARACTER VARYING(254), label CHARACTER VARYING(254) );

INSERT INTO _dbh_class (code, label) VALUES ( '1', '0cm - 4.9cm');
INSERT INTO _dbh_class ( code, label) VALUES ( '2', '5cm - 9.9cm');
INSERT INTO _dbh_class ( code, label) VALUES ( '3', '10cm - 14.9cm');
INSERT INTO _dbh_class ( code, label) VALUES ( '4', '15cm - 19.9cm');
INSERT INTO _dbh_class ( code, label) VALUES ( '5', '20cm - 24.9cm');
INSERT INTO _dbh_class ( code, label) VALUES ( '6', '25cm - 29.9cm');
INSERT INTO _dbh_class ( code, label) VALUES ( '7', '30cm - 34.9cm');
INSERT INTO _dbh_class ( code, label) VALUES ( '8', '35cm - 39.9cm');
INSERT INTO _dbh_class ( code, label) VALUES ( '9', '>= 40cm');

INSERT INTO _dbh_class ( code, label) VALUES ( '-1', 'No Data');

--
DROP TABLE if exists _commercial_tree_class;
CREATE TABLE _commercial_tree_class (code CHARACTER VARYING, label CHARACTER VARYING);
INSERT INTO _commercial_tree_class (code, label) VALUES ('1', 'IA');
INSERT INTO _commercial_tree_class (code, label) VALUES ('2', 'IB');
INSERT INTO _commercial_tree_class (code, label) VALUES ('3', 'II');
INSERT INTO _commercial_tree_class (code, label) VALUES ('4', 'III');
INSERT INTO _commercial_tree_class (code, label) VALUES ('5', 'IV');



alter table tree 
add column dbh_class varchar;

update tree
set dbh_class = case 
                    when dbh < 5 then '1'
                    when dbh < 10 then '2'
                    when dbh < 15 then '3'
                    when dbh < 20 then '4'
                    when dbh < 25 then '5'
                    when dbh < 30 then '6'
                    when dbh < 35 then '7'
                    when dbh < 40 then '8'
                    else '9'
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
    

alter table tree 
add column commercial_class varchar;
    
update tree
set commercial_class =
    case
        when 
            species in ('DAL/MEL','DSP/MES','CMB/SCH') or species like 'OLE%' then '1'
        when 
            species in ('MLT/STU','MIL/EXC','BEI/KWE','BRC/HUI','SWA/MAD','KHA/ANT','ALK/STU','BRE/SAL','AFZ/QUA','CEP/USA','FAG/ANG','HAG/ABY','BRH/TAM','JUN/PRO','MRK','OCO/USA')                
            or species like 'NEW%' or species like 'ENN%' or species like 'RHI%'or species like 'PTR%' 
            then '2'
        when     
            species in ('PRN/CUR','SYZ/CUM','VIT/KEN','BRM/DIS','PRC/ANG','BUR/AFR','BAP/KIR', 'ERH/GUI','SPI/AFR','ACA/NIG') 
            or species like 'POD%' or species like 'CHP%' or species like 'BRH%' or species like 'JUL%' 
            or species like 'ALB%' or species like 'STE%'
            then '3'
        when     
            species in ('FCL/LAU','CSP/MAL','LOV/SWY','LOV/BRO','CRL/AFR','XYM/MON','RPN/RHO','PTL/MYR','BOM/RHO','MSP/EMI')             
            then '4'
        --when species_group != 1 and dbh_class = '4' and health != '7' then 1
        else '5'
    end;        
        
    
    
    
-- 1 commercial tree, 0 otherwise    
alter table tree 
add column commercial_tree integer;

update tree
set commercial_tree =
    case
        --when
            --species in ('DAL/MEL','DSP/MES','CMB/SCH','MLT/STU','MIL/EXC','BEI/KWE','BRC/HUI','SWA/MAD','KHA/ANT','ALK/STU','BRE/SAL','AFZ/QUA','CEP/USA','FAG/ANG','HAG/ABY','BRH/TAM','JUN/PRO','MRK','OCO/USA')
            --or species like 'NEW%' or species like 'ENN%' or species like 'RHI%'or species like 'PTR%' or species like 'OLE%'
            --then 1
        when commercial_class in ('1','2','3','4') and dbh_class in ('5','6','7','8','9') and health != '7' then 1
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
    
    