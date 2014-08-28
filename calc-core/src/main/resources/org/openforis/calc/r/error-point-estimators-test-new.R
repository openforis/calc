rm( list=ls() );
source('error-point-estimators.R');
library( "RPostgreSQL" );
library( "sqldf" );
options (
  #gsubfn.engine = "R" , 
  sqldf.driver = "SQLite"
);

# === Open db connection
driver <- dbDriver("PostgreSQL");
connection <- dbConnect(driver, host="localhost", dbname="calc", user="calc", password="calc", port=5432);
dbSendQuery(conn=connection, statement='set search_path to "atlantis", "public"');


# ============================== Read data =========================
# ==== Input parameters (must be passed by CALC)
workspaceId <- dbGetQuery(conn=connection, statement="select w.id from calc.workspace w where w.name = 'atlantis'")$id;
aoiId <- dbGetQuery(conn=connection, statement="select a.id from calc.aoi a where lower(a.caption) = 'atlantis'")$id;
categoryName <- 'vegetation_type';
categoryColumnName <- 'vegetation_type_code_id';
quantityName <- 'volume';

# ======= strata
select <- "select  s.stratum_no as stratum, e.area 
      from
      calc.stratum s
      join
      atlantis._country_expf    e
      on e.stratum = s.stratum_no";
select <- paste( select , "and e._administrative_unit_country_id =", aoiId , sep = " " );
select <- paste( select , "and s.workspace_id = ", workspaceId , sep = " " );
strata <- dbGetQuery( conn=connection , statement=select);

# ====== aoi
select <- paste( "select id, land_area, caption from calc.aoi where id =" , aoiId , sep=" ");
aoi <- dbGetQuery( conn=connection , statement=select ) ;



# ======= plots in area of interest
select <-  'select distinct _plot_id as plot_id, _stratum as stratum , _cluster as cluster , weight , vegetation_type_code_id as class_id';
# select <- paste( select , "case 
#                  when vegetation_type_code_id in(957,958,959,960,961,962,963) 
#                  then 1
#                  else 
#                  0
#                  end as class   " , 
#                  sep = " , " );
select <- paste( select , "from _plot_fact p join atlantis._plot_aoi a on p._plot_id = a.id" , sep = " " );
select <- paste( select , "where a._administrative_unit_country_id =" , sep = " " );
select <- paste( select , aoiId , sep = " " );
plots <- dbGetQuery( conn=connection , statement = select );


# ==== select trees
select <- "SELECT distinct
    t._plot_id as plot_id,    
    t._stratum AS stratum ,
    t._cluster AS cluster ,
    sum( t.volume ) as quantity,
    t.vegetation_type_code_id as class_id
FROM
    _tree_plot_agg t 
JOIN
    atlantis._plot_aoi a
ON
    t._plot_id = a.id
WHERE
    a._administrative_unit_country_id = ";
select <- paste( select, aoiId , sep = " " );
select <- paste( select , "group by
                            t._plot_id,    
                            t._stratum ,
                            t._cluster, 
                            t.vegetation_type_code_id", 
                 sep = " " );
data <- dbGetQuery(conn=connection, statement=select);


# ========================== Start Calc R code =================================
tableName <- "_error_";
tableName <- paste( tableName , quantityName , sep="" );
tableName <- paste( tableName , "_" , sep="" );
tableName <- paste( tableName , categoryName , sep="" );
tableName <- paste( tableName , "_" , sep="" );
tableName <- paste( tableName , aoi$caption , sep="" );

#drop error table
query <- "drop table if exists";
query <- paste( query , tableName , sep=" ");
query <- paste( query , "cascade" , sep=" ");
dbSendQuery(conn=connection, statement=query);

#create error table
query <- "CREATE TABLE";
query <- paste( query , "atlantis." , sep=" ");
query <- paste( query , tableName , sep="");
query <- paste( query , "(
        mean_volume_vegetation_type_atlantis_absolute_error DOUBLE PRECISION NOT NULL,
        mean_volume_vegetation_type_atlantis_relative_error DOUBLE PRECISION NOT NULL,
        mean_volume_vegetation_type_atlantis_variance DOUBLE PRECISION NOT NULL,
        total_volume_vegetation_type_atlantis_absolute_error DOUBLE PRECISION NOT NULL,
        total_volume_vegetation_type_atlantis_relative_error DOUBLE PRECISION NOT NULL,
        total_volume_vegetation_type_atlantis_variance DOUBLE PRECISION NOT NULL," , sep =" ");
query <- paste( query , "_administrative_unit_country_id" ,sep=" ");
query <- paste( query , " bigint NOT NULL," ,sep=" ");
query <- paste( query , "vegetation_type_code_id" ,sep=" ");
query <- paste( query , " bigint NOT NULL" ,sep=" ");
query <- paste( query , ")", sep=" " );
dbSendQuery(conn = connection , statement = query );



classes <- sqldf( "select distinct class_id from data" );
classes <- classes$class_id;
# testing  
#cls <- 962;
#cls <- 976;
for( cls in classes ){
  
  #print( cls )
  
  select <- "select *, case when class_id ==";
  select <- paste( select , cls , sep=" " );
  select  <- paste( select , "then 1 else 0 end as class from plots" , sep=" " );
  plotsClone <-  sqldf( select );
    
  select <- "select *, case when class_id ==";
  select <- paste( select , cls , sep=" " );
  select  <- paste( select , "then 1 else 0 end as class from data" , sep=" " );
  dataClone <-  sqldf( select );
  
  
  #errors <- calculateAreaError( plots = plotsClone , strata = strata )
  #errors <- calcualteAreaErrorStratified ( plots = plotsClone , strata = strata )
  errors <- calculateQuantityErrorStratified( data = dataClone , plots = plotsClone , strata = strata ) ;
  #errors <- calculateQuantityError( data = dataClone , plots = plotsClone , strata = strata ) ;
  #errors
  
  # insert values
  query <- "INSERT
INTO
    _error_volume_vegetation_type_atlantis
    (
        mean_volume_vegetation_type_atlantis_absolute_error,
        mean_volume_vegetation_type_atlantis_relative_error,
        mean_volume_vegetation_type_atlantis_variance,
        total_volume_vegetation_type_atlantis_absolute_error,
        total_volume_vegetation_type_atlantis_relative_error,
        total_volume_vegetation_type_atlantis_variance,
        _administrative_unit_country_id,
        vegetation_type_code_id
    )
    VALUES
    (";
  
  query <- paste( query , ifelse( is.na(errors$meanQuantityAbsolute) , -1 , errors$meanQuantityAbsolute) , sep="" );
  query <- paste( query , ifelse( is.na(errors$meanQuantityRelative) , -1 , errors$meanQuantityRelative ) , sep="," );
  query <- paste( query , ifelse( is.na(errors$meanQuantityVariance) , -1 , errors$meanQuantityVariance) , sep="," );
  query <- paste( query , ifelse( is.na(errors$totalQuantityAbsolute) , -1 , errors$totalQuantityAbsolute) , sep="," );
  query <- paste( query , ifelse( is.na(errors$totalQuantityRelative) , -1 , errors$totalQuantityRelative) , sep="," );
  query <- paste( query , ifelse( is.na(errors$totalQuantityVariance) , -1 , errors$totalQuantityVariance) , sep="," );
  query <- paste( query , aoiId , sep=","  );
  query <- paste( query , cls , sep=","  );
  
  query <- paste( query , ")" , sep=""  );
  
  #print( query )
  dbSendQuery( conn = connection , statement = query );
}
 