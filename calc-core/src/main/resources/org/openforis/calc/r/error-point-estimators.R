### ============================================================== 
# Error calculation script based on 
# "Formulas for estimators and their variances in NFI 
# 28.2.2014 K.T. Korhonen & Olli Salmensuu, point estimators"
#
# @author Mino Togna
### ==============================================================


library("RPostgreSQL");
library("sqldf");
# sqldf options. 
# driver is SQLLite in order to read from dataframe , otherwise it uses PostgreSQL
# https://code.google.com/p/sqldf/#Troubleshooting
options(
  #gsubfn.engine = "R" , 
  sqldf.driver = "SQLite"
);

# === Open db connection
driver <- dbDriver("PostgreSQL");
connection <- dbConnect(driver, host="localhost", dbname="calc", user="calc", password="calc", port=5432);
dbSendQuery(conn=connection, statement='set search_path to "naforma1", "public"');

# === Input parameters (must be passed by CALC)
workspaceId <- 1;
aoiId <- 821;

category <- "vegetation_type_code_id";
classes <- c(957,958,959,960,961,962,963)

# categories <- c( "land_use_code_id" );
#entityId <- 13;
# quantity = "volume";

# === Read data 

# strata
select <- "select s.id , s.stratum_no as stratum, s.caption, e.area 
            from
              calc.stratum s
            join
              naforma1._level_1_expf    e
              on e.stratum = s.stratum_no";
select <- paste( select , "and e._administrative_unit_level_1_id =", aoiId , sep = " " );
select <- paste( select , "and s.workspace_id = ", workspaceId , sep = " " );
strata <- dbGetQuery( conn=connection , statement=select);

# aoi
select <- paste( "select id, land_area, caption from calc.aoi where id =" , aoiId , sep=" ");
aoi <- dbGetQuery( conn=connection , statement=select ) ;

# plots in area of interest
select <-  'select _stratum as stratum , _cluster as cluster , weight';
select <- paste( select , "case 
                              when vegetation_type_code_id in(957,958,959,960,961,962,963) 
                                then 1
                              else 
                                0
                            end as class   " , 
                 sep = " , " );
select <- paste( select , "from _plot_fact p join naforma1._plot_aoi a on p._plot_id = a.id" , sep = " " );
select <- paste( select , "where a._administrative_unit_level_1_id =" , sep = " " );
select <- paste( select , aoiId , sep = " " );
plots <- dbGetQuery( conn=connection , statement = select );

# clusters
clusters <- sqldf( "select distinct 
                      stratum ,  
                      cluster ,
                      sum(weight) as noPlots ,
                      sum(class * weight) as noPlotsInClass
                   from 
                    plots
                   group by
                    stratum ,
                    cluster" );

# add no of plots to strata
strata <- sqldf( "select 
                      s.*, 
                      p.noPlots,
                      p.noPlotsInClass
                  from 
                    strata s 
                  left outer join 
                    (select stratum,  sum(weight) as noPlots, sum(class * weight) as noPlotsInClass from plots group by stratum) as p
                  on 
                    p.stratum = s.stratum");

# add no of clusters to strata
strata <- sqldf( "select 
                    s.*, 
                    c.noClusters 
                  from 
                    strata s 
                  left outer join 
                    (select stratum, count(*) as noClusters from clusters group by stratum) as c
                  on 
                    c.stratum = s.stratum");
# == (1)
strata$propInClass <- strata$noPlotsInClass / strata$noPlots;

# == (2)
strata$areaInClass <- strata$propInClass * strata$area;

# == (3)
clusters <- sqldf("select c.*, s.propInClass 
                  from clusters c
                  join strata s
                  on s.stratum = c.stratum");
#clusters$propInClass <-  (clusters$noPlotsInClass / clusters$noPlots);
clusters$x <- (clusters$noPlotsInClass - clusters$propInClass * clusters$noPlots ) ^ 2;

strata <- sqldf( "select 
                      s.*, 
                      sum(c.x) as x 
                   from strata s
                   left outer join clusters c
                   on s.stratum = c.stratum
                  group by s.stratum");
strata$var <- strata$noClusters / (strata$noClusters -1 ) * strata$x / strata$noPlots
# == (4)
strata$areaVar <- strata$area^2 * strata$var;
round( 100 * sqrt( strata$var ) / strata$areaInClass , 6)




# === Close db connection
dbDisconnect(connection);