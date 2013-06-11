create table "calc"."aoi" (
  "id"             serial not null, 
  "aoi_level_id"  int4, 
  "parent_aoi_id" int4, 
  "code"          varchar(255), 
  "caption"       varchar(255), 
  "shape"         Geometry(Multipolygon,4326), 
  "total_area"    numeric(15, 5), 
  "land_area"     numeric(15, 5), 
  constraint "aoi_pkey" 
    primary key ("id"));
comment on table "calc"."aoi" is 'Each area of interest (AOI) may be divided into sub-parts such that the sub-parts add up to the area of the whole (i.e. a compositional containment hierarchy)';
create table "calc"."aoi_hierarchy" (
  "id"            serial not null, 
  "workspace_id" int4 not null, 
  "caption"      varchar(255), 
  "description"  varchar(1024), 
  constraint "aoi_hierarchy_pkey" 
    primary key ("id"));
comment on table "calc"."aoi_hierarchy" is 'A particular AOI hierarchy, such as "Administrative Units" or "Ecological Zones".';
create table "calc"."aoi_level" (
  "id"                serial not null, 
  "aoi_hierarchy_id" int4 not null unique, 
  "caption"          varchar(255) not null, 
  "rank"             int4 not null unique, 
  constraint "aoi_hierarchy_level_pkey" 
    primary key ("id"));
create table "calc"."category" (
  "id"                     serial not null, 
  "variable_id"           int4 not null, 
  "code"                  varchar(255), 
  "caption"               varchar(255), 
  "description"           varchar(1024), 
  "overrideInputMetadata" bool default 'false' not null, 
  "sort_order"            int4 not null, 
  primary key ("id"));
create table "calc"."entity" (
  "id"                serial not null, 
  "workspace_id"     int4 not null unique, 
  "data_table_id"    int4 not null, 
  "parent_entity_id" int4, 
  "name"             varchar(255) not null unique, 
  "caption"          varchar(255), 
  "description"      varchar(1024), 
  "sort_order"       int4 not null, 
  primary key ("id"));
comment on column "calc"."entity"."name" is 'Free text. Examples: "microplot", "tree", "bamboo"';
create table "calc"."calculation_step" (
  "id"              serial not null, 
  "chain_id"       int4 not null unique, 
  "step_no"        int4 not null unique, 
  "module_name"    varchar(255) not null, 
  "module_version" varchar(255) not null, 
  "operation_name" varchar(255) not null, 
  "caption"        varchar(255), 
  "description"    varchar(1024), 
  "parameters"     text, 
  primary key ("id"));
create table "calc"."processing_chain" (
  "id"            serial not null, 
  "workspace_id" int4 not null, 
  "caption"      varchar(255), 
  "description"  varchar(1024), 
  "parameters"   text, 
  primary key ("id"));
create table "calc"."sampling_unit" (
  "id"                 int4 not null, 
  "entity_id"          int4, 
  "stratum"            int4, 
  "panel"              int4, 
  "cluster"            varchar(255) unique, 
  "unit_no"            int4 not null unique, 
  "location"           Geometry(Point, 4326), 
  "shape"              Geometry(Multipolygon,4326), 
  "sampling_phase"     int4, 
  "permanent"          bool not null, 
  "entityworkspace_id" int4, 
  constraint "sample_plot_pkey" 
    primary key ("id"));
comment on column "calc"."sampling_unit"."cluster" is 'May be null if clusters are not used';
create table "calc"."sampling_unit_aoi" (
  "sampling_unit_id" int4 not null, 
  "aoi_id"           int4 not null, 
  constraint "sample_plot_aoi_pkey" 
    primary key ("sampling_unit_id", 
  "aoi_id"));
create table "calc"."workspace" (
  "id"             serial not null, 
  "caption"       varchar(255) not null, 
  "description"   varchar(1024), 
  "input_schema"  varchar(255) not null, 
  "output_schema" varchar(255) not null, 
  primary key ("id"));
comment on table "calc"."workspace" is 'One cycle of an inventory.';
create table "calc"."variable" (
  "id"                   serial not null, 
  "entity_id"           int4 unique, 
  "value_column_id"     int4 not null, 
  "code_list_table_id"  int4, 
  "default_category_id" int4 not null, 
  "scale"               char(25) not null, 
  "name"                varchar(255) not null unique, 
  "caption"             varchar(255), 
  "description"         varchar(1024), 
  "default_value"       float8, 
  "cube_member"         bool default 'true' not null, 
  "sort_order"          int4 not null, 
  primary key ("id"));
comment on column "calc"."variable"."scale" is 'Quantitative: RATIO|INTERVAL|OTHER
Categorical: NOMINAL|ORDINAL|BINARY';
create table "calc"."hierarchy" (
  "id"           serial not null, 
  "variable_id" int4 not null, 
  "caption"     varchar(255), 
  "description" varchar(1024), 
  primary key ("id"));
create table "calc"."hierarchy_level" (
  "id"            serial not null, 
  "hierarchy_id" int4 not null unique, 
  "caption"      varchar(255) not null, 
  "description"  varchar(1024), 
  "valueColumn"  varchar(255), 
  "rank"         int4 not null unique, 
  primary key ("id"));
create table "calc"."group" (
  "id"               serial not null, 
  "level_id"        int4 not null, 
  "parent_group_id" int4 not null, 
  "code"            varchar(255), 
  "caption"         varchar(255), 
  "description"     varchar(1024), 
  "sort_order"      int4 not null, 
  primary key ("id"));
create table "calc"."category_group" (
  "group_id"    int4 not null, 
  "category_id" int4 not null);
create table "calc"."table_metadata" (
  "id"            serial not null, 
  "workspace_id" int4 not null unique, 
  "table_name"   varchar(255) not null unique, 
  "table_type"   varchar(255) not null, 
  "input_table"  bool not null, 
  primary key ("id"));
comment on column "calc"."table_metadata"."table_type" is 'data, code_list';
create table "calc"."column_metadata" (
  "id"            serial not null, 
  "table_id"     int4 not null unique, 
  "column_name"  varchar(255) not null unique, 
  "column_type"  varchar(255) not null, 
  "input_column" bool not null, 
  primary key ("id"));
comment on column "calc"."column_metadata"."column_type" is 'e.g. id, parent_id, date, location, location_x, location_y, srsid, remarks, shape, etc.';
alter table "calc"."aoi" add constraint "parent_aoi_fkey" foreign key ("parent_aoi_id") references "calc"."aoi" ("id") on update No action on delete No action;
alter table "calc"."aoi" add constraint "aoi_level_fkey" foreign key ("aoi_level_id") references "calc"."aoi_level" ("id") on update No action on delete No action;
alter table "calc"."aoi_hierarchy" add constraint "aoi_hierarchy_workspace_fkey" foreign key ("workspace_id") references "calc"."workspace" ("id") on update No action on delete No action;
alter table "calc"."category" add constraint "category_variable_fkey" foreign key ("variable_id") references "calc"."variable" ("id") on update No action on delete No action;
alter table "calc"."entity" add constraint "parent_entity_fkey" foreign key ("parent_entity_id") references "calc"."entity" ("id") on update No action on delete No action;
alter table "calc"."calculation_step" add constraint "step_processing_chain_fkey" foreign key ("chain_id") references "calc"."processing_chain" ("id") on update No action on delete No action;
alter table "calc"."processing_chain" add constraint "processing_chain_workspace_fkey" foreign key ("workspace_id") references "calc"."workspace" ("id") on update No action on delete No action;
alter table "calc"."sampling_unit" add constraint "sampling_unit_entity_fkey" foreign key ("entity_id") references "calc"."entity" ("id") on update No action on delete No action;
alter table "calc"."sampling_unit_aoi" add constraint "aoi_sampling_unit_fkey" foreign key ("sampling_unit_id") references "calc"."sampling_unit" ("id") on update No action on delete No action;
alter table "calc"."sampling_unit_aoi" add constraint "sampling_unit_aoi_fkey" foreign key ("aoi_id") references "calc"."aoi" ("id") on update No action on delete No action;
alter table "calc"."variable" add constraint "variable_entity_fkey" foreign key ("entity_id") references "calc"."entity" ("id") on update No action on delete No action;
alter table "calc"."hierarchy_level" add constraint "level_hierarchy_fkey" foreign key ("hierarchy_id") references "calc"."hierarchy" ("id");
alter table "calc"."hierarchy" add constraint "hierarchy_variable_fkey" foreign key ("variable_id") references "calc"."variable" ("id");
alter table "calc"."group" add constraint "parent_group_fkey" foreign key ("parent_group_id") references "calc"."group" ("id");
alter table "calc"."group" add constraint "group_level_fkey" foreign key ("level_id") references "calc"."hierarchy_level" ("id");
alter table "calc"."category_group" add constraint "category_group_fkey" foreign key ("group_id") references "calc"."group" ("id");
alter table "calc"."category_group" add constraint "group_category_fkey" foreign key ("category_id") references "calc"."category" ("id");
alter table "calc"."aoi_level" add constraint "aoi_level_hierarchy_fkey" foreign key ("aoi_hierarchy_id") references "calc"."aoi_hierarchy" ("id") on update No action on delete No action;
alter table "calc"."column_metadata" add constraint "column_metadata_table_fkey" foreign key ("table_id") references "calc"."table_metadata" ("id");
alter table "calc"."entity" add constraint "entity_data_table_fkey" foreign key ("data_table_id") references "calc"."table_metadata" ("id");
alter table "calc"."variable" add constraint "variable_code_list_table_fkey" foreign key ("code_list_table_id") references "calc"."table_metadata" ("id");
alter table "calc"."variable" add constraint "FKvariable152814" foreign key ("value_column_id") references "calc"."column_metadata" ("id");
alter table "calc"."variable" add constraint "variable_default_category_fkey" foreign key ("default_category_id") references "calc"."category" ("id");
alter table "calc"."entity" add constraint "entity_workspace_fkey" foreign key ("workspace_id") references "calc"."workspace" ("id");
alter table "calc"."table_metadata" add constraint "FKtable_meta279210" foreign key ("workspace_id") references "calc"."workspace" ("id");
