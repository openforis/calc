alter table calc.sampling_unit_aoi
add column workspace_id integer null;

update calc.sampling_unit_aoi
set workspace_id = 1;
 
create table
    calc.stratum
    (
        id serial not null,
        workspace_id integer not null,
        stratum_no integer not null,
        caption character varying(255),
        description character varying(1024),
        area double precision,
        primary key (id),
        constraint stratum_workspace_id foreign key (workspace_id) references calc.workspace (id)
        on
    delete
        cascade
    on
    update
        cascade,
        unique (stratum_no, workspace_id)
    );
    
create table
    calc.stratum_aoi
    (
        id serial not null,
        stratum_id integer not null,
        aoi_id integer not null,
        weight double precision,
        area double precision,
        expf double precision,
        primary key (id),
        constraint stratum_aoi_stratum_fkey foreign key (stratum_id) references calc.stratum (id)
        on
    delete
        cascade
    on
    update
        cascade
    );
    
alter table
    calcdev.calc.sampling_unit add COLUMN stratum_id integer;
alter table
    calcdev.calc.sampling_unit add constraint sampling_unit_stratum_fkey foreign key (stratum_id)
    references calcdev.calc.stratum (id)
on
delete
    cascade
on
update
    cascade;

insert into calc.stratum
(workspace_id, stratum_no)
select distinct 1, stratum
from calc.sampling_unit;

update calc.sampling_unit u
set stratum_id = s.id
from  calc.stratum s 
where u.stratum = s.stratum_no;
            
alter table calc.sampling_unit
drop column stratum;

alter table calc.stratum
add column weight double precision;
