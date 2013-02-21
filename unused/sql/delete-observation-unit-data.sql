DROP TABLE "calc"."plot_survey" CASCADE
GO 
CREATE TABLE "calc"."plot_survey"  ( 
	"id"           	serial NOT NULL,
	"survey_id"    	integer NOT NULL,
	"plot_id"      	integer NOT NULL,
	"obs_unit_id"  	integer NOT NULL,
	"section_no"   	integer NOT NULL,
	"survey_date"  	date NULL,
	"gps_reading"  	geometry(Point,4326) NULL,
	"direction"    	double precision NULL,
	"distance"     	double precision NULL,
	"location"     	geometry(Point,4326) NULL,
	"accessible"   	boolean NOT NULL,
	"parent_id"    	integer NULL,
	"survey_type"  	char(1) NOT NULL DEFAULT 'P',
	"step"         	integer NOT NULL,
	"source_id"    	integer NULL,
	"shape"        	geometry(Multipolygon,4326) NULL,
	"area"         	integer NULL,
	"percent_share"	double precision NULL,
	PRIMARY KEY("id")
)
GO
COMMENT ON COLUMN "calc"."plot_survey"."parent_id" IS 'For subplots, refers to the id of the main plot'
GO
COMMENT ON COLUMN "calc"."plot_survey"."survey_type" IS 'P|R|U|QA - Planned/Remeasurement/Unplanned/QA'
GO
COMMENT ON COLUMN "calc"."plot_survey"."step" IS '1=Incomplete 2=Dirty 3=Clean'
GO
COMMENT ON COLUMN "calc"."plot_survey"."source_id" IS 'Id in IDML or other data source'
GO

ALTER TABLE "calc"."plot_survey"
	ADD CONSTRAINT "uk_plot_survey"
	UNIQUE ("survey_id", "plot_id", "section_no", "survey_type")
GO

ALTER TABLE "calc"."specimen"
	ADD CONSTRAINT "fk_specimen_plot"
	FOREIGN KEY("plot_id")
	REFERENCES "calc"."plot_survey"("id")
GO

ALTER TABLE "calc"."plot_survey"
	ADD CONSTRAINT "fk_parent_plot_survey"
	FOREIGN KEY("parent_id")
	REFERENCES "calc"."plot_survey"("id")
	ON DELETE NO ACTION 
	ON UPDATE NO ACTION 
GO

ALTER TABLE "calc"."plot_category"
	ADD CONSTRAINT "fk_plot_category_value_plot_survey"
	FOREIGN KEY("plot_obs_id")
	REFERENCES "calc"."plot_survey"("id")
	ON DELETE NO ACTION 
	ON UPDATE NO ACTION 
GO

ALTER TABLE "calc"."plot_numeric_value"
	ADD CONSTRAINT "fk_plot_numeric_value_plot_survey"
	FOREIGN KEY("plot_obs_id")
	REFERENCES "calc"."plot_survey"("id")
	ON DELETE NO ACTION 
	ON UPDATE NO ACTION 
GO

