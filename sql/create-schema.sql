/*
Script generated by Aqua Data Studio 12.0.14 on Jan-02-2013 06:18:38 PM
Database: null
Schema: <All Schemas>
*/

DROP INDEX "calc"."idx_plot_ground_plot"
GO

DROP VIEW "calc"."aoi_view"
GO

DROP VIEW "calc"."cluster_view"
GO

DROP VIEW "calc"."plot_view"
GO

DROP VIEW "calc"."sampling_design_view"
GO

DROP VIEW "calc"."stratum_view"
GO

DROP VIEW "calc"."tree_view"
GO

ALTER TABLE "calc"."aoi"
	DROP CONSTRAINT "fk_aoi_parent" CASCADE 
GO

ALTER TABLE "calc"."aoi"
	DROP CONSTRAINT "fk_aoi_hierarchy" CASCADE 
GO

ALTER TABLE "calc"."category"
	DROP CONSTRAINT "fk_category_variable" CASCADE 
GO

ALTER TABLE "calc"."plot_category"
	DROP CONSTRAINT "fk_plot_category_category" CASCADE 
GO

ALTER TABLE "calc"."numeric_band"
	DROP CONSTRAINT "fk_numeric_band_category" CASCADE 
GO

ALTER TABLE "calc"."specimen_category"
	DROP CONSTRAINT "fk_specimen_category_category" CASCADE 
GO

ALTER TABLE "calc"."plot"
	DROP CONSTRAINT "fk_plot_cluster" CASCADE 
GO

ALTER TABLE "calc"."surveyed_cluster"
	DROP CONSTRAINT "fk_surveyed_cluster_cluster" CASCADE 
GO

ALTER TABLE "calc"."plot_numeric_value"
	DROP CONSTRAINT "fk_plot_numeric_value_variable" CASCADE 
GO

ALTER TABLE "calc"."specimen_numeric_value"
	DROP CONSTRAINT "fk_specimen_numeric_value_variable" CASCADE 
GO

ALTER TABLE "calc"."numeric_variable_banding"
	DROP CONSTRAINT "fk_banding_numeric_variable" CASCADE 
GO

ALTER TABLE "calc"."numeric_band"
	DROP CONSTRAINT "fk_numeric_band_banding" CASCADE 
GO

ALTER TABLE "calc"."categorical_variable"
	DROP CONSTRAINT "fk_categorical_variable_banding" CASCADE 
GO

ALTER TABLE "calc"."numeric_variable"
	DROP CONSTRAINT "fk_numeric_variable_obs_unit" CASCADE 
GO

ALTER TABLE "calc"."categorical_variable"
	DROP CONSTRAINT "fk_categorical_variable_obs_unit" CASCADE 
GO

ALTER TABLE "calc"."observation_unit"
	DROP CONSTRAINT "fk_survey_unit_parent" CASCADE 
GO

ALTER TABLE "calc"."surveyed_plot"
	DROP CONSTRAINT "fk_surveyed_plot_obs_unit" CASCADE 
GO

ALTER TABLE "calc"."specimen"
	DROP CONSTRAINT "fk_specimen_obs_unit" CASCADE 
GO

ALTER TABLE "calc"."process"
	DROP CONSTRAINT "fk_process_operation" CASCADE 
GO

ALTER TABLE "calc"."surveyed_plot"
	DROP CONSTRAINT "fk_surveyed_plot_plot" CASCADE 
GO

ALTER TABLE "calc"."operation_parameter"
	DROP CONSTRAINT "fk_operation_parameter_process" CASCADE 
GO

ALTER TABLE "calc"."process"
	DROP CONSTRAINT "fk_process_processing_chain" CASCADE 
GO

ALTER TABLE "calc"."specimen_numeric_value"
	DROP CONSTRAINT "fk_specimen_numeric_value_specimen" CASCADE 
GO

ALTER TABLE "calc"."specimen_category"
	DROP CONSTRAINT "fk_specimen_category_specimen" CASCADE 
GO

ALTER TABLE "calc"."specimen"
	DROP CONSTRAINT "fk_specimen_parent" CASCADE 
GO

ALTER TABLE "calc"."plot"
	DROP CONSTRAINT "fk_plot_stratum" CASCADE 
GO

ALTER TABLE "calc"."aoi_hierarchy"
	DROP CONSTRAINT "fk_aoi_hierarchy_survey" CASCADE 
GO

ALTER TABLE "calc"."observation_unit"
	DROP CONSTRAINT "fk_survey_unit_survey" CASCADE 
GO

ALTER TABLE "calc"."processing_chain"
	DROP CONSTRAINT "fk_processing_chain_survey" CASCADE 
GO

ALTER TABLE "calc"."stratum"
	DROP CONSTRAINT "fk_stratum_survey" CASCADE 
GO

ALTER TABLE "calc"."taxonomic_checklist"
	DROP CONSTRAINT "fk_taxonomic_checklist_survey" CASCADE 
GO

ALTER TABLE "calc"."plot"
	DROP CONSTRAINT "fk_plot_survey" CASCADE 
GO

ALTER TABLE "calc"."cluster"
	DROP CONSTRAINT "fk_cluster_survey" CASCADE 
GO

ALTER TABLE "calc"."surveyed_cluster"
	DROP CONSTRAINT "fk_surveyed_cluster_survey" CASCADE 
GO

ALTER TABLE "calc"."surveyed_plot"
	DROP CONSTRAINT "fk_surveyed_plot_survey" CASCADE 
GO

ALTER TABLE "calc"."surveyed_plot"
	DROP CONSTRAINT "fk_surveyed_plot_cluster" CASCADE 
GO

ALTER TABLE "calc"."plot_numeric_value"
	DROP CONSTRAINT "fk_plot_numeric_value_surveyed_plot" CASCADE 
GO

ALTER TABLE "calc"."plot_category"
	DROP CONSTRAINT "fk_plot_category_value_surveyed_plot" CASCADE 
GO

ALTER TABLE "calc"."specimen"
	DROP CONSTRAINT "fk_specimen_plot" CASCADE 
GO

ALTER TABLE "calc"."surveyed_plot"
	DROP CONSTRAINT "fk_parent_surveyed_plot" CASCADE 
GO

ALTER TABLE "calc"."specimen"
	DROP CONSTRAINT "fk_specimen_taxon" CASCADE 
GO

ALTER TABLE "calc"."taxon"
	DROP CONSTRAINT "fk_taxon_accepted_name" CASCADE 
GO

ALTER TABLE "calc"."taxon"
	DROP CONSTRAINT "fk_taxon_original_name" CASCADE 
GO

ALTER TABLE "calc"."taxon"
	DROP CONSTRAINT "fk_taxon_parent" CASCADE 
GO

ALTER TABLE "calc"."taxon_vernacular_name"
	DROP CONSTRAINT "fk_taxon_vernacular_name_accepted_usage" CASCADE 
GO

ALTER TABLE "calc"."taxon"
	DROP CONSTRAINT "fk_taxon_checklist" CASCADE 
GO

ALTER TABLE "calc"."category"
	DROP CONSTRAINT "uk_plot_class_code" CASCADE 
GO

ALTER TABLE "calc"."category"
	DROP CONSTRAINT "uk_plot_class_order" CASCADE 
GO

ALTER TABLE "calc"."cluster"
	DROP CONSTRAINT "uk_cluster_code" CASCADE 
GO

ALTER TABLE "calc"."numeric_band"
	DROP CONSTRAINT "uk_plot_derived_class_class" CASCADE 
GO

ALTER TABLE "calc"."plot"
	DROP CONSTRAINT "uk_plot_code" CASCADE 
GO

ALTER TABLE "calc"."stratum"
	DROP CONSTRAINT "uk_stratum_code" CASCADE 
GO

ALTER TABLE "calc"."survey"
	DROP CONSTRAINT "uk_survey_uri" CASCADE 
GO

ALTER TABLE "calc"."surveyed_cluster"
	DROP CONSTRAINT "uk_plot_obs_1" CASCADE 
GO

ALTER TABLE "calc"."surveyed_plot"
	DROP CONSTRAINT "uk_plot_obs" CASCADE 
GO

DROP TABLE IF EXISTS "calc"."aoi"
GO

DROP TABLE IF EXISTS "calc"."aoi_hierarchy"
GO

DROP TABLE IF EXISTS "calc"."categorical_variable"
GO

DROP TABLE IF EXISTS "calc"."category"
GO

DROP TABLE IF EXISTS "calc"."cluster"
GO

DROP TABLE IF EXISTS "calc"."numeric_band"
GO

DROP TABLE IF EXISTS "calc"."numeric_variable"
GO

DROP TABLE IF EXISTS "calc"."numeric_variable_banding"
GO

DROP TABLE IF EXISTS "calc"."observation_unit"
GO

DROP TABLE IF EXISTS "calc"."operation"
GO

DROP TABLE IF EXISTS "calc"."operation_parameter"
GO

DROP TABLE IF EXISTS "calc"."plot"
GO

DROP TABLE IF EXISTS "calc"."plot_category"
GO

DROP TABLE IF EXISTS "calc"."plot_numeric_value"
GO

DROP TABLE IF EXISTS "calc"."process"
GO

DROP TABLE IF EXISTS "calc"."processing_chain"
GO

DROP TABLE IF EXISTS "calc"."specimen"
GO

DROP TABLE IF EXISTS "calc"."specimen_category"
GO

DROP TABLE IF EXISTS "calc"."specimen_numeric_value"
GO

DROP TABLE IF EXISTS "calc"."stratum"
GO

DROP TABLE IF EXISTS "calc"."survey"
GO

DROP TABLE IF EXISTS "calc"."surveyed_cluster"
GO

DROP TABLE IF EXISTS "calc"."surveyed_plot"
GO

DROP TABLE IF EXISTS "calc"."taxon"
GO

DROP TABLE IF EXISTS "calc"."taxon_vernacular_name"
GO

DROP TABLE IF EXISTS "calc"."taxonomic_checklist"
GO

CREATE TABLE "calc"."aoi"  ( 
	"id"          	serial NOT NULL,
	"hierarchy_id"	integer NOT NULL,
	"code"        	varchar(255) NULL,
	"name"        	varchar(255) NOT NULL,
	"geometry"    	geometry(Multipolygon,4326)  NOT NULL,
	"area"        	numeric(15,5) NOT NULL,
	"parent_id"   	integer NULL,
	"level"       	integer NOT NULL,
	PRIMARY KEY("id")
)
GO
COMMENT ON TABLE "calc"."aoi" IS 'Each areas of interest (AOI) may be divided into sub-parts such that the sub-parts add up to the area of the whole (i.e. a compositional containment hierarchy)'
GO

CREATE TABLE "calc"."aoi_hierarchy"  ( 
	"id"       	serial NOT NULL,
	"survey_id"	integer NOT NULL,
	"name"     	varchar(255) NULL,
	PRIMARY KEY("id")
)
GO
COMMENT ON TABLE "calc"."aoi_hierarchy" IS 'A particular AOI hierarchy, such as "Administrative Units" or "Ecological Zones".'
GO

CREATE TABLE "calc"."categorical_variable"  ( 
	"id"               	serial NOT NULL,
	"obs_unit_id"      	integer NOT NULL,
	"name"             	varchar(255) NULL,
	"multiple_response"	boolean NOT NULL,
	"banding_id"       	integer NULL,
	"type"             	varchar(25) NULL,
	"source_id"        	integer NULL,
	PRIMARY KEY("id")
)
GO
COMMENT ON COLUMN "calc"."categorical_variable"."type" IS 'ordinal|nominal|interval|binomial'
GO
COMMENT ON COLUMN "calc"."categorical_variable"."source_id" IS 'Id in IDML or other data source'
GO

CREATE TABLE "calc"."category"  ( 
	"id"         	serial NOT NULL,
	"variable_id"	integer NOT NULL,
	"code"       	varchar(255) NULL,
	"name"       	varchar(255) NULL,
	"order"      	integer NOT NULL,
	"source_id"  	integer NULL,
	PRIMARY KEY("id")
)
GO

CREATE TABLE "calc"."cluster"  ( 
	"id"       	serial NOT NULL,
	"survey_id"	integer NOT NULL,
	"no"       	integer NULL,
	"code"     	varchar(25) NOT NULL,
	"x_index"  	integer NULL,
	"y_index"  	integer NULL,
	PRIMARY KEY("id")
)
GO
COMMENT ON TABLE "calc"."cluster" IS 'A grouping of sample plots.'
GO

CREATE TABLE "calc"."numeric_band"  ( 
	"id"         	serial NOT NULL,
	"category_id"	integer NOT NULL,
	"banding_id" 	integer NOT NULL,
	"minimum"    	integer NULL,
	"left_open"  	boolean NOT NULL,
	"maximum"    	integer NULL,
	"right_open" 	boolean NOT NULL,
	PRIMARY KEY("id")
)
GO

CREATE TABLE "calc"."numeric_variable"  ( 
	"id"         	serial NOT NULL,
	"obs_unit_id"	integer NOT NULL,
	"name"       	varchar(255) NULL,
	"type"       	varchar(25) NULL,
	"source_id"  	integer NULL,
	PRIMARY KEY("id")
)
GO
COMMENT ON COLUMN "calc"."numeric_variable"."type" IS 'count|additive|multiplicative'
GO
COMMENT ON COLUMN "calc"."numeric_variable"."source_id" IS 'Id in IDML or other data source'
GO

CREATE TABLE "calc"."numeric_variable_banding"  ( 
	"id"                 	serial NOT NULL,
	"numeric_variable_id"	integer NOT NULL,
	PRIMARY KEY("id")
)
GO

CREATE TABLE "calc"."observation_unit"  ( 
	"id"       	serial NOT NULL,
	"survey_id"	integer NOT NULL,
	"name"     	varchar(255) NULL,
	"type"     	varchar(25) NULL,
	"parent_id"	integer NULL,
	"source_id"	integer NULL,
	PRIMARY KEY("id")
)
GO
COMMENT ON COLUMN "calc"."observation_unit"."name" IS 'Free text. Examples: "microplot", "tree", "bamboo"'
GO
COMMENT ON COLUMN "calc"."observation_unit"."type" IS 'plot|specimen'
GO
COMMENT ON COLUMN "calc"."observation_unit"."source_id" IS 'Id in IDML or other data source'
GO

CREATE TABLE "calc"."operation"  ( 
	"id"         	serial NOT NULL,
	"name"       	varchar(255) NULL,
	"description"	varchar(255) NULL,
	"class"      	varchar(255) NULL,
	PRIMARY KEY("id")
)
GO

CREATE TABLE "calc"."operation_parameter"  ( 
	"id"        	serial NOT NULL,
	"process_id"	integer NOT NULL,
	"name"      	varchar(255) NULL,
	"value"     	varchar(255) NOT NULL,
	PRIMARY KEY("id")
)
GO

CREATE TABLE "calc"."plot"  ( 
	"id"            	serial NOT NULL,
	"survey_id"     	integer NOT NULL,
	"cluster_id"    	integer NULL,
	"no"            	integer NULL,
	"code"          	varchar(25) NOT NULL,
	"location"      	geometry(Point, 4326) NULL,
	"shape"         	geometry(Multipolygon,4326) NULL,
	"phase"         	integer NULL,
	"permanent_plot"	boolean NOT NULL,
	"ground_plot"   	boolean NOT NULL,
	"stratum_id"    	integer NULL,
	PRIMARY KEY("id")
)
GO
COMMENT ON TABLE "calc"."plot" IS 'A pre-defined, spatially contiguous area in which observations are to made.'
GO
COMMENT ON COLUMN "calc"."plot"."cluster_id" IS 'May be null if clusters are not used'
GO

CREATE TABLE "calc"."plot_category"  ( 
	"id"         	serial NOT NULL,
	"plot_obs_id"	integer NOT NULL,
	"category_id"	integer NOT NULL,
	"computed"   	boolean NOT NULL,
	PRIMARY KEY("id")
)
GO

CREATE TABLE "calc"."plot_numeric_value"  ( 
	"id"         	serial NOT NULL,
	"plot_obs_id"	integer NOT NULL,
	"variable_id"	integer NOT NULL,
	"value"      	double precision NOT NULL,
	"computed"   	boolean NOT NULL,
	PRIMARY KEY("id")
)
GO

CREATE TABLE "calc"."process"  ( 
	"id"          	serial NOT NULL,
	"chain_id"    	integer NOT NULL,
	"operation_id"	integer NOT NULL,
	"name"        	varchar(255) NULL,
	"description" 	varchar(1024) NULL,
	"order"       	integer NOT NULL,
	PRIMARY KEY("id")
)
GO

CREATE TABLE "calc"."processing_chain"  ( 
	"id"         	serial NOT NULL,
	"survey_id"  	integer NOT NULL,
	"name"       	varchar(255) NULL,
	"description"	varchar(255) NULL,
	PRIMARY KEY("id")
)
GO

CREATE TABLE "calc"."specimen"  ( 
	"id"         	serial NOT NULL,
	"plot_id"    	integer NOT NULL,
	"obs_unit_id"	integer NOT NULL,
	"taxon_id"   	integer NULL,
	"survey_date"	date NULL,
	"source_id"  	integer NULL,
	"parent_id"  	integer NULL,
	PRIMARY KEY("id")
)
GO
COMMENT ON COLUMN "calc"."specimen"."source_id" IS 'Id in IDML or other data source'
GO

CREATE TABLE "calc"."specimen_category"  ( 
	"id"         	serial NOT NULL,
	"specimen_id"	integer NOT NULL,
	"category_id"	integer NOT NULL,
	"calculated" 	boolean NOT NULL,
	PRIMARY KEY("id")
)
GO

CREATE TABLE "calc"."specimen_numeric_value"  ( 
	"id"                 	serial NOT NULL,
	"specimen_id"        	integer NOT NULL,
	"numeric_variable_id"	integer NOT NULL,
	"value"              	double precision NOT NULL,
	"calculated"         	boolean NOT NULL,
	PRIMARY KEY("id")
)
GO

CREATE TABLE "calc"."stratum"  ( 
	"id"         	serial NOT NULL,
	"survey_id"  	integer NOT NULL,
	"no"         	integer NULL,
	"code"       	varchar(25) NOT NULL,
	"description"	varchar(255) NULL,
	PRIMARY KEY("id")
)
GO
COMMENT ON TABLE "calc"."stratum" IS 'In stratified sampling, a homogenous subgroup of the population of sampling units.  For simple random or systematic sampling, this will contain one record.'
GO

CREATE TABLE "calc"."survey"  ( 
	"id"         	serial NOT NULL,
	"uri"        	varchar(255) NOT NULL,
	"name"       	varchar(255) NULL,
	"description"	varchar(255) NULL,
	"cycle"      	varchar(255) NULL,
	"from_date"  	date NULL,
	"to_date"    	date NULL,
	PRIMARY KEY("id")
)
GO
COMMENT ON TABLE "calc"."survey" IS 'An inventory or inventory cycle.'
GO

CREATE TABLE "calc"."surveyed_cluster"  ( 
	"id"         	serial NOT NULL,
	"survey_id"  	integer NOT NULL,
	"cluster_id" 	integer NULL,
	"survey_date"	date NOT NULL,
	"step"       	integer NOT NULL,
	"source_id"  	integer NULL,
	PRIMARY KEY("id")
)
GO
COMMENT ON COLUMN "calc"."surveyed_cluster"."step" IS '1=Incomplete 2=Dirty 3=Clean'
GO
COMMENT ON COLUMN "calc"."surveyed_cluster"."source_id" IS 'Id in IDML or other data source'
GO

CREATE TABLE "calc"."surveyed_plot"  ( 
	"id"                 	serial NOT NULL,
	"survey_id"          	integer NOT NULL,
	"plot_id"            	integer NULL,
	"obs_unit_id"        	integer NOT NULL,
	"surveyed_cluster_id"	integer NULL,
	"section_no"         	integer NOT NULL,
	"survey_date"        	date NOT NULL,
	"gps_reading"        	geometry(Point, 4326) NOT NULL,
	"location"           	geometry(Point, 4326) NOT NULL,
	"accessible"         	boolean NOT NULL,
	"parent_id"          	integer NULL,
	"survey_type"        	char(1) NOT NULL DEFAULT 'P',
	"step"               	integer NOT NULL,
	"source_id"          	integer NULL,
	PRIMARY KEY("id")
)
GO
COMMENT ON COLUMN "calc"."surveyed_plot"."parent_id" IS 'For subplots, refers to the id of the main plot'
GO
COMMENT ON COLUMN "calc"."surveyed_plot"."survey_type" IS 'P|R|U|QA - Planned/Remeasurement/Unplanned/QA'
GO
COMMENT ON COLUMN "calc"."surveyed_plot"."step" IS '1=Incomplete 2=Dirty 3=Clean'
GO
COMMENT ON COLUMN "calc"."surveyed_plot"."source_id" IS 'Id in IDML or other data source'
GO

CREATE TABLE "calc"."taxon"  ( 
	"id"                   	serial NOT NULL,
	"checklist_id"         	integer NOT NULL,
	"code"                 	varchar(255) NULL,
	"scientific_name_id"   	varchar(255) NULL,
	"scientific_name"      	varchar(255) NOT NULL,
	"according_to"         	varchar(255) NULL,
	"published_in"         	varchar(255) NULL,
	"published_in_year"    	integer NULL,
	"specific_epithet"     	varchar(255) NULL,
	"infraspecific_epithet"	varchar(255) NULL,
	"rank"                 	varchar(25) NOT NULL,
	"authorship"           	varchar(255) NOT NULL,
	"nomenclatural_code"   	varchar(25) NULL,
	"taxonomic_status"     	varchar(25) NULL,
	"nomenclatural_status" 	varchar(255) NULL,
	"remarks"              	varchar(1024) NULL,
	"references"           	varchar(255) NULL,
	"parent_id"            	integer NULL,
	"original_name_id"     	integer NULL,
	"accepted_name_id"     	integer NULL,
	PRIMARY KEY("id")
)
GO
COMMENT ON TABLE "calc"."taxon" IS 'See:
http://www.gbif.org/orc/?doc_id=4752
http://rs.tdwg.org/dwc/terms/
http://rs.gbif.org/core/dwc_taxon.xml
http://code.google.com/p/darwincore/wiki/Taxon'
GO
COMMENT ON COLUMN "calc"."taxon"."scientific_name_id" IS 'Exclusively used to reference an external and resolvable identifier that returns nomenclatural (not taxonomic) details of a name. Use taxonID to refer to taxa. see also http://rs.tdwg.org/dwc/terms/index.htm#scientificNameID  Examples: urn:lsid:ipni.org:names:37829-1:1.3'
GO
COMMENT ON COLUMN "calc"."taxon"."scientific_name" IS 'The full scientific name, with authorship and date information if known. Examples: "Coleoptera" (order), "Vespertilionidae" (family), "Manis" (genus), "Ctenomys sociabilis" (genus + specificEpithet), "Ambystoma tigrinum diaboli" (genus + specificEpithet + infraspecificEpithet), "Roptrocerus typographi (Györfi, 1952)" (genus + specificEpithet + scientificNameAuthorship), "Quercus agrifolia var. oxyadenia (Torr.) J.T. Howell" (genus + specificEpithet + taxonRank + infraspecificEpithet + scientificNameAuthorship).'
GO
COMMENT ON COLUMN "calc"."taxon"."according_to" IS 'The reference to the source in which the specific taxon concept circumscription is defined or implied - traditionally signified by the Latin "sensu" or "sec." (from secundum, meaning "according to"). Example: "McCranie, J. R., D. B. Wake, and L. D. Wilson. 1996. The taxonomic status of Bolitoglossa schmidti, with comments on the biology of the Mesoamerican salamander Bolitoglossa dofleini (Caudata: Plethodontidae). Carib. J. Sci. 32:395-398.", "Werner Greuter 2008", "Lilljeborg 1861, Upsala Univ. Arsskrift, Math. Naturvet., pp. 4, 5".'
GO
COMMENT ON COLUMN "calc"."taxon"."published_in" IS 'A reference for the publication in which the scientificName was originally established under the rules of the associated nomenclaturalCode. Examples: "Pearson O. P., and M. I. Christie. 1985. Historia Natural, 5(37):388", "Forel, Auguste, Diagnosies provisoires de quelques espèces nouvelles de fourmis de Madagascar, récoltées par M. Grandidier., Annales de la Societe Entomologique de Belgique, Comptes-rendus des Seances 30, 1886".'
GO
COMMENT ON COLUMN "calc"."taxon"."published_in_year" IS 'The four-digit year in which the scientificName was published.'
GO
COMMENT ON COLUMN "calc"."taxon"."specific_epithet" IS 'The name of the first or species epithet of the scientificName. Example: "concolor", "gottschei". '
GO
COMMENT ON COLUMN "calc"."taxon"."infraspecific_epithet" IS 'The name of the lowest or terminal infraspecific epithet of the scientificName, excluding any rank designation. Example: "concolor", "oxyadenia", "sayi".'
GO
COMMENT ON COLUMN "calc"."taxon"."rank" IS 'The taxonomic rank of the most specific name in the scientificName. Recommended vocabulary: http://rs.gbif.org/vocabulary/gbif/rank.xml see also http://rs.tdwg.org/dwc/terms/index.htm#taxonRank'
GO
COMMENT ON COLUMN "calc"."taxon"."authorship" IS 'The authorship information for the scientificName formatted according to the conventions of the applicable nomenclaturalCode. see also http://rs.tdwg.org/dwc/terms/index.htm#scientificNameAuthorship.  Examples: "(Torr.) J.T. Howell", "(Martinovský) Tzvelev", "(Linnaeus 1768)"'
GO
COMMENT ON COLUMN "calc"."taxon"."nomenclatural_code" IS 'The nomenclatural code under which the scientificName is constructed. see also http://rs.tdwg.org/dwc/terms/index.htm#nomenclaturalCode Examples: ICBN; ICZN'
GO
COMMENT ON COLUMN "calc"."taxon"."taxonomic_status" IS 'The status of the use of the scientificName as a label for a taxon. Requires taxonomic opinion to define the scope of a taxon. Rules of priority then are used to define the taxonomic status of the nomenclature contained in that scope, combined with the experts opinion. It must be linked to a specific taxonomic reference that defines the concept. Recommended vocabulary: http://rs.gbif.org/vocabulary/gbif/taxonomic_status.xml see also http://rs.tdwg.org/dwc/terms/index.htm#taxonomicStatus Examples: "invalid", "misapplied", "homotypic synonym", "accepted"'
GO
COMMENT ON COLUMN "calc"."taxon"."nomenclatural_status" IS 'The status related to the original publication of the name and its conformance to the relevant rules of nomenclature. It is based essentially on an algorithm according to the business rules of the code. It requires no taxonomic opinion. Recommended vocabulary: http://rs.gbif.org/vocabulary/gbif/nomenclatural_status.xml see also http://rs.tdwg.org/dwc/terms/index.htm#nomenclaturalStatus Examples: "nom. ambig.", "nom. illeg.", "nom. subnud."'
GO
COMMENT ON COLUMN "calc"."taxon"."remarks" IS 'Comments or notes about the taxon or name. see also http://rs.tdwg.org/dwc/terms/index.htm#taxonRemarks'
GO
COMMENT ON COLUMN "calc"."taxon"."references" IS 'A URL to a related resource that is referenced, cited, or otherwise pointed to by the described resource. Often another webpage showing the same, but richer resource see also http://rs.tdwg.org/dwc/terms/index.htm#dcterms:references  Examples: http://www.itis.gov/servlet/SingleRpt/SingleRpt?search_topic=TSN&search_value=552479'
GO

CREATE TABLE "calc"."taxon_vernacular_name"  ( 
	"id"              	serial NOT NULL,
	"accepted_name_id"	integer NOT NULL,
	"name"            	varchar(255) NULL,
	"language_code"   	varchar(2) NULL,
	"language_variant"	varchar(255) NULL,
	"qualifier"       	varchar(25) NULL,
	"remarks"         	varchar(1024) NULL,
	PRIMARY KEY("id")
)
GO
COMMENT ON COLUMN "calc"."taxon_vernacular_name"."qualifier" IS 'For qualifying usage by region or other parameters'
GO

CREATE TABLE "calc"."taxonomic_checklist"  ( 
	"id"       	serial NOT NULL,
	"survey_id"	integer NOT NULL,
	"name"     	varchar(255) NULL,
	PRIMARY KEY("id")
)
GO

ALTER TABLE "calc"."category"
	ADD CONSTRAINT "uk_plot_class_code"
	UNIQUE ("variable_id", "code")
GO

ALTER TABLE "calc"."category"
	ADD CONSTRAINT "uk_plot_class_order"
	UNIQUE ("variable_id", "order")
GO

ALTER TABLE "calc"."cluster"
	ADD CONSTRAINT "uk_cluster_code"
	UNIQUE ("survey_id", "code")
GO

ALTER TABLE "calc"."numeric_band"
	ADD CONSTRAINT "uk_plot_derived_class_class"
	UNIQUE ("category_id")
GO

ALTER TABLE "calc"."plot"
	ADD CONSTRAINT "uk_plot_code"
	UNIQUE ("survey_id", "code")
GO

ALTER TABLE "calc"."stratum"
	ADD CONSTRAINT "uk_stratum_code"
	UNIQUE ("survey_id", "code")
GO

ALTER TABLE "calc"."survey"
	ADD CONSTRAINT "uk_survey_uri"
	UNIQUE ("uri")
GO

ALTER TABLE "calc"."surveyed_cluster"
	ADD CONSTRAINT "uk_plot_obs_1"
	UNIQUE ("cluster_id")
GO

ALTER TABLE "calc"."surveyed_plot"
	ADD CONSTRAINT "uk_plot_obs"
	UNIQUE ("plot_id", "section_no")
GO

ALTER TABLE "calc"."aoi"
	ADD CONSTRAINT "fk_aoi_parent"
	FOREIGN KEY("parent_id")
	REFERENCES "calc"."aoi"("id")
GO

ALTER TABLE "calc"."aoi"
	ADD CONSTRAINT "fk_aoi_hierarchy"
	FOREIGN KEY("hierarchy_id")
	REFERENCES "calc"."aoi_hierarchy"("id")
GO

ALTER TABLE "calc"."category"
	ADD CONSTRAINT "fk_category_variable"
	FOREIGN KEY("variable_id")
	REFERENCES "calc"."categorical_variable"("id")
GO

ALTER TABLE "calc"."plot_category"
	ADD CONSTRAINT "fk_plot_category_category"
	FOREIGN KEY("category_id")
	REFERENCES "calc"."category"("id")
GO

ALTER TABLE "calc"."numeric_band"
	ADD CONSTRAINT "fk_numeric_band_category"
	FOREIGN KEY("category_id")
	REFERENCES "calc"."category"("id")
GO

ALTER TABLE "calc"."specimen_category"
	ADD CONSTRAINT "fk_specimen_category_category"
	FOREIGN KEY("category_id")
	REFERENCES "calc"."category"("id")
GO

ALTER TABLE "calc"."plot"
	ADD CONSTRAINT "fk_plot_cluster"
	FOREIGN KEY("cluster_id")
	REFERENCES "calc"."cluster"("id")
GO

ALTER TABLE "calc"."surveyed_cluster"
	ADD CONSTRAINT "fk_surveyed_cluster_cluster"
	FOREIGN KEY("cluster_id")
	REFERENCES "calc"."cluster"("id")
GO

ALTER TABLE "calc"."plot_numeric_value"
	ADD CONSTRAINT "fk_plot_numeric_value_variable"
	FOREIGN KEY("variable_id")
	REFERENCES "calc"."numeric_variable"("id")
GO

ALTER TABLE "calc"."specimen_numeric_value"
	ADD CONSTRAINT "fk_specimen_numeric_value_variable"
	FOREIGN KEY("numeric_variable_id")
	REFERENCES "calc"."numeric_variable"("id")
GO

ALTER TABLE "calc"."numeric_variable_banding"
	ADD CONSTRAINT "fk_banding_numeric_variable"
	FOREIGN KEY("numeric_variable_id")
	REFERENCES "calc"."numeric_variable"("id")
GO

ALTER TABLE "calc"."numeric_band"
	ADD CONSTRAINT "fk_numeric_band_banding"
	FOREIGN KEY("banding_id")
	REFERENCES "calc"."numeric_variable_banding"("id")
GO

ALTER TABLE "calc"."categorical_variable"
	ADD CONSTRAINT "fk_categorical_variable_banding"
	FOREIGN KEY("banding_id")
	REFERENCES "calc"."numeric_variable_banding"("id")
GO

ALTER TABLE "calc"."numeric_variable"
	ADD CONSTRAINT "fk_numeric_variable_obs_unit"
	FOREIGN KEY("obs_unit_id")
	REFERENCES "calc"."observation_unit"("id")
GO

ALTER TABLE "calc"."categorical_variable"
	ADD CONSTRAINT "fk_categorical_variable_obs_unit"
	FOREIGN KEY("obs_unit_id")
	REFERENCES "calc"."observation_unit"("id")
GO

ALTER TABLE "calc"."observation_unit"
	ADD CONSTRAINT "fk_survey_unit_parent"
	FOREIGN KEY("parent_id")
	REFERENCES "calc"."observation_unit"("id")
GO

ALTER TABLE "calc"."surveyed_plot"
	ADD CONSTRAINT "fk_surveyed_plot_obs_unit"
	FOREIGN KEY("obs_unit_id")
	REFERENCES "calc"."observation_unit"("id")
GO

ALTER TABLE "calc"."specimen"
	ADD CONSTRAINT "fk_specimen_obs_unit"
	FOREIGN KEY("obs_unit_id")
	REFERENCES "calc"."observation_unit"("id")
GO

ALTER TABLE "calc"."process"
	ADD CONSTRAINT "fk_process_operation"
	FOREIGN KEY("operation_id")
	REFERENCES "calc"."operation"("id")
GO

ALTER TABLE "calc"."surveyed_plot"
	ADD CONSTRAINT "fk_surveyed_plot_plot"
	FOREIGN KEY("plot_id")
	REFERENCES "calc"."plot"("id")
GO

ALTER TABLE "calc"."operation_parameter"
	ADD CONSTRAINT "fk_operation_parameter_process"
	FOREIGN KEY("process_id")
	REFERENCES "calc"."process"("id")
GO

ALTER TABLE "calc"."process"
	ADD CONSTRAINT "fk_process_processing_chain"
	FOREIGN KEY("chain_id")
	REFERENCES "calc"."processing_chain"("id")
GO

ALTER TABLE "calc"."specimen_numeric_value"
	ADD CONSTRAINT "fk_specimen_numeric_value_specimen"
	FOREIGN KEY("specimen_id")
	REFERENCES "calc"."specimen"("id")
GO

ALTER TABLE "calc"."specimen_category"
	ADD CONSTRAINT "fk_specimen_category_specimen"
	FOREIGN KEY("specimen_id")
	REFERENCES "calc"."specimen"("id")
GO

ALTER TABLE "calc"."specimen"
	ADD CONSTRAINT "fk_specimen_parent"
	FOREIGN KEY("parent_id")
	REFERENCES "calc"."specimen"("id")
GO

ALTER TABLE "calc"."plot"
	ADD CONSTRAINT "fk_plot_stratum"
	FOREIGN KEY("stratum_id")
	REFERENCES "calc"."stratum"("id")
GO

ALTER TABLE "calc"."aoi_hierarchy"
	ADD CONSTRAINT "fk_aoi_hierarchy_survey"
	FOREIGN KEY("survey_id")
	REFERENCES "calc"."survey"("id")
GO

ALTER TABLE "calc"."observation_unit"
	ADD CONSTRAINT "fk_survey_unit_survey"
	FOREIGN KEY("survey_id")
	REFERENCES "calc"."survey"("id")
GO

ALTER TABLE "calc"."processing_chain"
	ADD CONSTRAINT "fk_processing_chain_survey"
	FOREIGN KEY("survey_id")
	REFERENCES "calc"."survey"("id")
GO

ALTER TABLE "calc"."stratum"
	ADD CONSTRAINT "fk_stratum_survey"
	FOREIGN KEY("survey_id")
	REFERENCES "calc"."survey"("id")
GO

ALTER TABLE "calc"."taxonomic_checklist"
	ADD CONSTRAINT "fk_taxonomic_checklist_survey"
	FOREIGN KEY("survey_id")
	REFERENCES "calc"."survey"("id")
GO

ALTER TABLE "calc"."plot"
	ADD CONSTRAINT "fk_plot_survey"
	FOREIGN KEY("survey_id")
	REFERENCES "calc"."survey"("id")
GO

ALTER TABLE "calc"."cluster"
	ADD CONSTRAINT "fk_cluster_survey"
	FOREIGN KEY("survey_id")
	REFERENCES "calc"."survey"("id")
GO

ALTER TABLE "calc"."surveyed_cluster"
	ADD CONSTRAINT "fk_surveyed_cluster_survey"
	FOREIGN KEY("survey_id")
	REFERENCES "calc"."survey"("id")
GO

ALTER TABLE "calc"."surveyed_plot"
	ADD CONSTRAINT "fk_surveyed_plot_survey"
	FOREIGN KEY("survey_id")
	REFERENCES "calc"."survey"("id")
GO

ALTER TABLE "calc"."surveyed_plot"
	ADD CONSTRAINT "fk_surveyed_plot_cluster"
	FOREIGN KEY("surveyed_cluster_id")
	REFERENCES "calc"."surveyed_cluster"("id")
GO

ALTER TABLE "calc"."plot_numeric_value"
	ADD CONSTRAINT "fk_plot_numeric_value_surveyed_plot"
	FOREIGN KEY("plot_obs_id")
	REFERENCES "calc"."surveyed_plot"("id")
GO

ALTER TABLE "calc"."plot_category"
	ADD CONSTRAINT "fk_plot_category_value_surveyed_plot"
	FOREIGN KEY("plot_obs_id")
	REFERENCES "calc"."surveyed_plot"("id")
GO

ALTER TABLE "calc"."specimen"
	ADD CONSTRAINT "fk_specimen_plot"
	FOREIGN KEY("plot_id")
	REFERENCES "calc"."surveyed_plot"("id")
GO

ALTER TABLE "calc"."surveyed_plot"
	ADD CONSTRAINT "fk_parent_surveyed_plot"
	FOREIGN KEY("parent_id")
	REFERENCES "calc"."surveyed_plot"("id")
GO

ALTER TABLE "calc"."specimen"
	ADD CONSTRAINT "fk_specimen_taxon"
	FOREIGN KEY("taxon_id")
	REFERENCES "calc"."taxon"("id")
GO

ALTER TABLE "calc"."taxon"
	ADD CONSTRAINT "fk_taxon_accepted_name"
	FOREIGN KEY("accepted_name_id")
	REFERENCES "calc"."taxon"("id")
GO

ALTER TABLE "calc"."taxon"
	ADD CONSTRAINT "fk_taxon_original_name"
	FOREIGN KEY("original_name_id")
	REFERENCES "calc"."taxon"("id")
GO

ALTER TABLE "calc"."taxon"
	ADD CONSTRAINT "fk_taxon_parent"
	FOREIGN KEY("parent_id")
	REFERENCES "calc"."taxon"("id")
GO

ALTER TABLE "calc"."taxon_vernacular_name"
	ADD CONSTRAINT "fk_taxon_vernacular_name_accepted_usage"
	FOREIGN KEY("accepted_name_id")
	REFERENCES "calc"."taxon"("id")
GO

ALTER TABLE "calc"."taxon"
	ADD CONSTRAINT "fk_taxon_checklist"
	FOREIGN KEY("checklist_id")
	REFERENCES "calc"."taxonomic_checklist"("id")
GO

CREATE VIEW "calc"."aoi_view"
AS
null
GO

CREATE VIEW "calc"."cluster_view"
AS
null
GO
COMMENT ON VIEW "calc"."cluster_view" IS 'Summary of data at cluster level'
GO

CREATE VIEW "calc"."plot_view"
AS
null
GO
COMMENT ON VIEW "calc"."plot_view" IS 'Observed plot sections and respective sampling design attributes'
GO

CREATE VIEW "calc"."sampling_design_view"
AS
SELECT
	plot.survey_id,
	plot.stratum_id,
    stratum.no AS stratum_no,
    stratum.code AS stratum_code,
	cluster.id AS cluster_id,
    cluster.no AS cluster_no,
    cluster.code AS cluster_code,
	plot.id AS plot_id,
	plot.no AS plot_no,
	plot.code AS plot_code,
	plot.location AS plot_location,
	plot.shape AS plot_shape,
	plot.phase,
	plot.permanent_plot,
	plot.ground_plot
FROM
	plot
LEFT OUTER JOIN 
    stratum ON stratum.id = plot.stratum_id
LEFT OUTER JOIN 
    cluster ON cluster.id = plot.cluster_id
GO
COMMENT ON VIEW "calc"."sampling_design_view" IS 'Plots designated in sampling design for field survey'
GO

CREATE VIEW "calc"."stratum_view"
AS
null
GO
COMMENT ON VIEW "calc"."stratum_view" IS 'Summary of data at stratum level'
GO

CREATE VIEW tree_view AS SELECT t.id, t.plot_id, t.taxon_id, t.health_id, t.origin_id, t.dbh, t.dbh_height, t.top_height, t.bole_height, t.stump_diameter, t.stump_height, t.inclusion_area, t.inclusion_probability, t.est_top_height, t.est_basal_area, t.est_stem_volume, t.est_bole_volume, t.est_top_volume, t.est_volume, t.est_stem_biomass, t.est_ag_biomass, t.est_bg_biomass, t.est_biomass, t.est_stem_carbon, t.est_ag_carbon, t.est_bg_carbon, t.est_carbon, p.admin_unit_id, p.class1_id, p.plot_id AS sampling_unit_id, p.section, p.class2_id, ((t.est_volume)::double precision / t.inclusion_probability) AS hte_volume, ((1)::double precision / t.inclusion_probability) AS hte_stem_count FROM (calc.tree_obs t JOIN calc.plot_obs p ON ((t.plot_id = p.id)));
GO

CREATE INDEX "idx_plot_ground_plot"
	ON "calc"."plot"("ground_plot")
GO

