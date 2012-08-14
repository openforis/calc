ALTER TABLE "calc"."cluster"
	DROP CONSTRAINT "cluster_admin_unit_fkey" CASCADE ;

ALTER TABLE "calc"."plot"
	DROP CONSTRAINT "plot_cluster_fkey" CASCADE ;

ALTER TABLE "calc"."plot_obs"
	DROP CONSTRAINT "plot_obs_plot_fkey" CASCADE ;

ALTER TABLE "calc"."plot_obs"
	DROP CONSTRAINT "plot_obs_class1_fkey" CASCADE ;

ALTER TABLE "calc"."plot_obs"
	DROP CONSTRAINT "plot_obs_class2_fkey" CASCADE ;

ALTER TABLE "calc"."plot_obs"
	DROP CONSTRAINT "plot_obs_class3_fkey" CASCADE ;

ALTER TABLE "calc"."plot_obs"
	DROP CONSTRAINT "plot_obs_class4_fkey" CASCADE ;

ALTER TABLE "calc"."plot_obs"
	DROP CONSTRAINT "plot_obs_class5_fkey" CASCADE ;

ALTER TABLE "calc"."tree_obs"
	DROP CONSTRAINT "plot_obs_tree_obs_fkey" CASCADE ;

ALTER TABLE "calc"."cluster"
	DROP CONSTRAINT "cluster_stratum_fkey" CASCADE ;

ALTER TABLE "calc"."tree_obs"
	DROP CONSTRAINT "tree_obs_health_fkey" CASCADE ;

DROP TABLE IF EXISTS "calc"."admin_unit";

DROP TABLE IF EXISTS "calc"."cluster";

DROP TABLE IF EXISTS "calc"."plot";

DROP TABLE IF EXISTS "calc"."plot_class1";

DROP TABLE IF EXISTS "calc"."plot_class2";

DROP TABLE IF EXISTS "calc"."plot_class3";

DROP TABLE IF EXISTS "calc"."plot_class4";

DROP TABLE IF EXISTS "calc"."plot_class5";

DROP TABLE IF EXISTS "calc"."plot_obs";

DROP TABLE IF EXISTS "calc"."stratum";

DROP TABLE IF EXISTS "calc"."tree_health";

DROP TABLE IF EXISTS "calc"."tree_obs";

DROP SCHEMA if exists "calc";

