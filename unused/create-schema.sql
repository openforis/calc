CREATE SCHEMA "calc";﻿

CREATE TABLE "calc"."admin_unit"  ( 
	"id"         	int4 NOT NULL,
	"full_code"  	varchar(25) NULL,
	"level1_name"	varchar(255) NOT NULL,
	"level2_name"	varchar(255) NULL,
	"area"       	numeric(15,5) NULL,
	"level1_code"	varchar(25) NULL,
	"level2_code"	varchar(25) NULL,
	PRIMARY KEY("id")
);

CREATE TABLE "calc"."cluster"  ( 
	"id"           	int4 NOT NULL,
	"code"         	varchar(25) NULL,
	"label"        	varchar(255) NULL,
	"stratum_id"   	int4 NULL,
	"admin_unit_id"	int4 NULL,
	"phase"        	int4 NULL DEFAULT 1,
	PRIMARY KEY("id")
);

CREATE TABLE "calc"."plot"  ( 
	"id"        	int4 NOT NULL,
	"code"      	varchar(25) NOT NULL,
	"label"     	varchar(255) NULL,
	"cluster_id"	int4 NOT NULL,
	"location"  	varchar(255) NULL,
	"phase"     	int4 NOT NULL DEFAULT 1,
	PRIMARY KEY("id")
);

CREATE TABLE "calc"."plot_class1"  ( 
	"id"         	int4 NOT NULL,
	"full_code"  	varchar(25) NULL,
	"level1_name"	varchar(255) NULL,
	"level2_name"	varchar(255) NULL,
	"level3_name"	varchar(255) NULL,
	"level1_code"	varchar(25) NULL,
	"level2_code"	varchar(25) NULL,
	"level3_code"	varchar(25) NULL,
	PRIMARY KEY("id")
);

CREATE TABLE "calc"."plot_class2"  ( 
	"id"         	int4 NOT NULL,
	"full_code"  	varchar(25) NULL,
	"level1_name"	varchar(255) NULL,
	"level2_name"	varchar(255) NULL,
	"level3_name"	varchar(255) NULL,
	"level1_code"	varchar(25) NULL,
	"level2_code"	varchar(25) NULL,
	"level3_code"	varchar(25) NULL,
	PRIMARY KEY("id")
);

CREATE TABLE "calc"."plot_class3"  ( 
	"id"         	int4 NOT NULL,
	"full_code"  	varchar(25) NULL,
	"level1_name"	varchar(255) NULL,
	"level2_name"	varchar(255) NULL,
	"level3_name"	varchar(255) NULL,
	"level1_code"	varchar(25) NULL,
	"level2_code"	varchar(25) NULL,
	"level3_code"	varchar(25) NULL,
	PRIMARY KEY("id")
);

CREATE TABLE "calc"."plot_class4"  ( 
	"id"         	int4 NOT NULL,
	"full_code"  	varchar(25) NULL,
	"level1_name"	varchar(255) NULL,
	"level2_name"	varchar(255) NULL,
	"level3_name"	varchar(255) NULL,
	"level1_code"	varchar(25) NULL,
	"level2_code"	varchar(25) NULL,
	"level3_code"	varchar(25) NULL,
	PRIMARY KEY("id")
);

CREATE TABLE "calc"."plot_class5"  ( 
	"id"         	int4 NOT NULL,
	"full_code"  	varchar(25) NULL,
	"level1_name"	varchar(255) NULL,
	"level2_name"	varchar(255) NULL,
	"level3_name"	varchar(255) NULL,
	"level1_code"	varchar(25) NOT NULL,
	"level2_code"	varchar(25) NULL,
	"level3_code"	varchar(25) NULL,
	PRIMARY KEY("id")
);

CREATE TABLE "calc"."plot_obs"  ( 
	"id"                      	int4 NOT NULL,
	"plot_id"                 	int4 NOT NULL,
	"section"                 	varchar(25) NULL,
	"share"                   	numeric(15,5) NULL,
	"est_area"                	numeric(15,5) NULL,
	"accessibility_id"        	int4 NULL,
	"admin_unit_id"           	int4 NULL,
	"class1_id"               	int4 NULL,
	"class2_id"               	int4 NULL,
	"class3_id"               	int4 NULL,
	"class4_id"               	int4 NULL,
	"class5_id"               	int4 NULL,
	"center"                  	int4 NULL,
	"slope"                   	numeric(15,5) NULL,
	"slope_correction_applied"	int4 NULL,
	"inclusion_probability"   	double precision NULL,
	"location"                	varchar(25) NULL,
	"hte_area"                	double precision NULL,
	PRIMARY KEY("id")
);

CREATE TABLE "calc"."stratum"  ( 
	"id"      	int4 NOT NULL,
	"code"    	varchar(25) NULL,
	"label"   	varchar(255) NULL,
	"est_area"	numeric(15,5) NULL,
	PRIMARY KEY("id")
);

CREATE TABLE "calc"."tree_health"  ( 
	"id"  	int4 NOT NULL,
	"code"	varchar(25) NULL,
	"name"	varchar(255) NULL,
	PRIMARY KEY("id")
);

CREATE TABLE "calc"."tree_obs"  ( 
	"id"                   	int4 NOT NULL,
	"plot_obs_id"          	int4 NOT NULL,
	"taxon_id"             	int4 NULL,
	"health_id"            	int4 NULL,
	"origin_id"            	int4 NULL,
	"dbh"                  	numeric(15,5) NULL,
	"dbh_height"           	numeric(15,5) NULL,
	"top_height"           	numeric(15,5) NULL,
	"bole_height"          	numeric(15,5) NULL,
	"stump_diameter"       	numeric(15,5) NULL,
	"stump_height"         	numeric(15,5) NULL,
	"inclusion_area"       	numeric(15,5) NULL,
	"inclusion_probability"	float8 NULL,
	"est_top_height"       	numeric(15,5) NULL,
	"est_basal_area"       	numeric(15,5) NULL,
	"est_stem_volume"      	numeric(15,5) NULL,
	"est_bole_volume"      	numeric(15,5) NULL,
	"est_top_volume"       	numeric(15,5) NULL,
	"est_volume"           	numeric(15,5) NULL,
	"est_stem_biomass"     	numeric(15,5) NULL,
	"est_ag_biomass"       	numeric(15,5) NULL,
	"est_bg_biomass"       	numeric(15,5) NULL,
	"est_biomass"          	numeric(15,5) NULL,
	"est_stem_carbon"      	numeric(15,5) NULL,
	"est_ag_carbon"        	numeric(15,5) NULL,
	"est_bg_carbon"        	numeric(15,5) NULL,
	"est_carbon"           	numeric(15,5) NULL,
	PRIMARY KEY("id")
);
COMMENT ON COLUMN "calc"."tree_obs"."bole_height" IS 'a.k.a. merchantable';
COMMENT ON COLUMN "calc"."tree_obs"."est_bole_volume" IS 'a.k.a. merchantable';
COMMENT ON COLUMN "calc"."tree_obs"."est_ag_biomass" IS 'Estimated above-ground biomass';
COMMENT ON COLUMN "calc"."tree_obs"."est_bg_biomass" IS 'Estimated below-ground biomass';

ALTER TABLE "calc"."cluster"
	ADD CONSTRAINT "cluster_admin_unit_fkey"
	FOREIGN KEY("admin_unit_id")
	REFERENCES "calc"."admin_unit"("id");

ALTER TABLE "calc"."plot"
	ADD CONSTRAINT "plot_cluster_fkey"
	FOREIGN KEY("cluster_id")
	REFERENCES "calc"."cluster"("id");

ALTER TABLE "calc"."plot_obs"
	ADD CONSTRAINT "plot_obs_plot_fkey"
	FOREIGN KEY("plot_id")
	REFERENCES "calc"."plot"("id");

ALTER TABLE "calc"."plot_obs"
	ADD CONSTRAINT "plot_obs_class1_fkey"
	FOREIGN KEY("class1_id")
	REFERENCES "calc"."plot_class1"("id");

ALTER TABLE "calc"."plot_obs"
	ADD CONSTRAINT "plot_obs_class2_fkey"
	FOREIGN KEY("class2_id")
	REFERENCES "calc"."plot_class2"("id");

ALTER TABLE "calc"."plot_obs"
	ADD CONSTRAINT "plot_obs_class3_fkey"
	FOREIGN KEY("class3_id")
	REFERENCES "calc"."plot_class3"("id");

ALTER TABLE "calc"."plot_obs"
	ADD CONSTRAINT "plot_obs_class4_fkey"
	FOREIGN KEY("class4_id")
	REFERENCES "calc"."plot_class4"("id");

ALTER TABLE "calc"."plot_obs"
	ADD CONSTRAINT "plot_obs_class5_fkey"
	FOREIGN KEY("class5_id")
	REFERENCES "calc"."plot_class5"("id");

ALTER TABLE "calc"."tree_obs"
	ADD CONSTRAINT "plot_obs_tree_obs_fkey"
	FOREIGN KEY("plot_obs_id")
	REFERENCES "calc"."plot_obs"("id")
	ON DELETE NO ACTION 
	ON UPDATE NO ACTION ;

ALTER TABLE "calc"."cluster"
	ADD CONSTRAINT "cluster_stratum_fkey"
	FOREIGN KEY("stratum_id")
	REFERENCES "calc"."stratum"("id");

ALTER TABLE "calc"."tree_obs"
	ADD CONSTRAINT "tree_obs_health_fkey"
	FOREIGN KEY("health_id")
	REFERENCES "calc"."tree_health"("id");

