library( "RPostgreSQL" );
library( "sqldf" );

source( "error-point-estimators.R" );

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

# ============================== Read data =========================
# ==== Input parameters (must be passed by CALC)
workspaceId <- 1;
#aoiId <- 1;
aoiId <- 821;

# ======= strata
select <- "select s.id , s.stratum_no as stratum, s.caption, e.area 
      from
      calc.stratum s
      join
      naforma1._level_1_expf    e
      on e.stratum = s.stratum_no";
select <- paste( select , "and e._administrative_unit_level_1_id =", aoiId , sep = " " );
select <- paste( select , "and s.workspace_id = ", workspaceId , sep = " " );
strata <- dbGetQuery( conn=connection , statement=select);

# ====== aoi
select <- paste( "select id, land_area, caption from calc.aoi where id =" , aoiId , sep=" ");
aoi <- dbGetQuery( conn=connection , statement=select ) ;

# ======= plots in area of interest
select <-  'select distinct _plot_id as plot_id, _stratum as stratum , _cluster as cluster , weight';
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



# ==== select trees
select <- "SELECT distinct
    t._plot_id as plot_id,    
    t._stratum AS stratum ,
    t._cluster AS cluster ,
    sum( t.volume / t.plot_area ) as quantity,
    CASE 
        WHEN t.vegetation_type_code_id IN(957,958,959,960,961,962,963) 
        THEN 1
        ELSE 0
    END AS class
FROM
    _tree_fact t 
JOIN
    naforma1._plot_aoi a
ON
    t._plot_id = a.id
WHERE
    a._administrative_unit_level_1_id = ";
select <- paste( select, aoiId , sep = " " );
select <- paste( select , "group by
                            t._plot_id,    
                            t._stratum ,
                            t._cluster, 
                            t.vegetation_type_code_id", 
                 sep = " " );
data <- dbGetQuery(conn=connection, statement=select);
#== add weight to data from join with plots
data <- sqldf( "select d.*, p.weight from data d join plots p on d.plot_id = p.plot_id" );


results <- calcualteAreaVariance(plots=plots , strata=strata);
strata <-sqldf( "select s.* , 
                    r.areaInClass , 
                    r.areaVar, 
                    r.seArea 
                  from 
                    strata s 
                  join 
                    results as r 
                  on 
                    r.stratum = s.stratum" )

results <- calculateQuantityVariance(data=data , plots=plots , strata=strata);
strata <-sqldf( "select 
                    s.* , 
                    r.varMeanQuantity , 
                    r.varTotalQuantity , 
                    r.totalQuantity ,
                    r.seMeanQuantity,
                    r.seTotalQuantity
                  from 
                    strata s 
                  join 
                    results as r 
                  on 
                    r.stratum = s.stratum" );


#== error
propInClass <- sum(strata$totalQuantity)/sum(strata$areaInClass);
# == (12)
varMeanQuantity <- ( sum( strata$varTotalQuantity ) - propInClass^2 * sum(strata$areaVar) ) / sum( strata$areaInClass ) ^ 2 ;
sqrt( varMeanQuantity )

# === Close db connection
dbDisconnect(connection);
