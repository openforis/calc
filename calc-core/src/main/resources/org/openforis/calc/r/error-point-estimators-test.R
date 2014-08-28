library( "RPostgreSQL" );

# === Open db connection
driver <- dbDriver("PostgreSQL");
connection <- dbConnect(driver, host="localhost", dbname="calc", user="calc", password="calc", port=5432);
dbSendQuery(conn=connection, statement='set search_path to "atlantis", "public"');

# ============================== Read data =========================
# ==== Input parameters (must be passed by CALC)
workspaceId <- dbGetQuery(conn=connection, statement="select w.id from calc.workspace w where w.name = 'atlantis'")$id;
aoiId <- dbGetQuery(conn=connection, statement="select a.id from calc.aoi a where lower(a.caption) = 'atlantis'")$id;

# ======= strata
select <- "select s.id , s.stratum_no as stratum, s.caption, e.area 
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
select <-  'select distinct _plot_id as plot_id, _stratum as stratum , _cluster as cluster , weight';
select <- paste( select , "case 
                              when vegetation_type_code_id in(957,958,959,960,961,962,963) 
                                then 1
                              else 
                                0
                            end as class   " , 
                 sep = " , " );
select <- paste( select , "from _plot_fact p join atlantis._plot_aoi a on p._plot_id = a.id" , sep = " " );
select <- paste( select , "where a._administrative_unit_country_id =" , sep = " " );
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
#== add weight to data from join with plots
data <- sqldf("select d.*, p.weight from data d join plots p on d.plot_id = p.plot_id" );


# write.csv( plots,file="~/Desktop/metla-data/calc/plots.csv",row.names=F )
# write.csv( data , file="~/Desktop/metla-data/calc/trees.csv", row.names=F )
# write.csv( strata , file="~/Desktop/metla-data/calc/strata.csv", row.names=F )

sum(strata$area)
p <- read.csv( file='~/Desktop/metla-data/calc-test-data/plots.csv')
p$stratum <- 1;
t <- read.csv( file='~/Desktop/metla-data/calc-test-data/trees.csv')
t$stratum <- 1;
s <- data.frame( stratum=1, area<-1000000)
data<-t;plots<-p;strata<-s;
qtyError <- calculateStratumQuantityVariances( data=d , plots=p , strata=s );

areaError <- calculateAreaError( plots=plots , strata=strata );
qtyError <- calculateQuantityError( data=data , plots=plots , strata=strata );

# === Close db connection
dbDisconnect(connection);


# test_bpdata <- read.table( file="~/Desktop/metla-data/test_bpdata.txt" );
# test_bpdata$plot_id <- test_bpdata$plotid;
# test_bpdata$stratum <- 1;
# test_bpdata$class <- ifelse( test_bpdata$cl==12 , 1, 0 );
# test_bpdata$weight <- 1;
# strata <- data.frame( stratum=c(1), area=c(234234) );
# 
# calcualteStratumAreaVariances( plots=test_bpdata, strata=strata );
# 
# calculateAreaError( plots=test_bpdata, strata=strata );
