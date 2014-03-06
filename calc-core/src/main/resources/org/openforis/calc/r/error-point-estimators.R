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
  gsubfn.engine = "R" , 
  sqldf.driver = "SQLite"
);

# === Open db connection
driver <- dbDriver("PostgreSQL");
connection <- dbConnect(driver, host="localhost", dbname="calc", user="calc", password="calc", port=5432);
dbSendQuery(conn=connection, statement='set search_path to "naforma1", "public"');

# === Input parameters (must be passed by CALC)
workspaceId <- 1;
aoiId <- 821;
entityId <- 13;
categories <- c( "land_use_code_id" );
quantity = "volume";

# === Read data 
# == TODO create unique combinations of strata/categories (use merge function)

# strata
select <- "SELECT s.id , s.stratum_no AS stratum, s.caption, e.area 
            FROM
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
select <- paste( select , categories , sep = " , " );
select <- paste( select , "from _plot_fact p join naforma1._plot_aoi a on p._plot_id = a.id" , sep = " " );
select <- paste( select , "where a._administrative_unit_level_1_id =" , sep = " " );
select <- paste( select , aoiId , sep = " " );
plots <- dbGetQuery( conn=connection , statement = select );

# clusters
clusters <- sqldf( "select distinct 
                      stratum ,  
                      cluster ,
                      sum(weight) as noPlots 
                   from 
                    plots
                   group by
                    stratum ,
                    cluster" );

# add no of plots to strata
strata <- sqldf( "select 
                      s.*, 
                      p.noPlots 
                  from 
                    strata s 
                  left outer join 
                    (select stratum,  sum(weight) as noPlots from plots group by stratum) as p
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


# create unique combinations of each category/strata/cluster 
select <- "select stratum , cluster, sum(weight) as noPlots";
select <- paste( select , categories , sep = " , " );
select <- paste( select , "from plots group by stratum, cluster" , sep = "  " );
select <- paste( select , categories , sep = " , " );
strataCategory <- sqldf( select );

# add total number of plots per cluster
strataCategory <- sqldf("select s.*, c.noPlots as noPlotsInCluster 
                          from 
                            strataCategory as s
                          join 
                            clusters as c
                          on c.cluster = s.cluster");

# c.noPlots = Pi(f)
# c.prop = (1)
# c.area = (2)
# == (1) AND (2)
strataCategory <- sqldf( "select 
                            c.* , 
                            c.noPlots / s.noPlots as prop ,
                            s.area * (c.noPlots / s.noPlots) as area
                         from 
                            strataCategory c 
                         join 
                            strata s 
                         on c.stratum = s.stratum" );

#== sql lite doesn't support neither ^ operator nor pow() function
select <- "select             
            stratum , 
            sum( area ) as area ,
            sum( (noPlots - prop *  noPlotsInCluster) * (noPlots - prop *  noPlotsInCluster) ) as x";
select <- paste( select, categories , sep = " , " );
select <- paste( select,  
                "from  
                    strataCategory 
                 group by             
                    stratum"
                , sep = " ");
select <- paste( select, categories , sep = " , " );
tmp <- sqldf( select );

#== (3)
#select <- "select s.*,  1/cast(s.noPlots as real) , s.noClusters, s.noClusters - 1, ( s.noClusters / cast(s.noClusters-1 as real) ), t.x";
select <- "select s.stratum , t.area";
for( c in categories ) {
  select <- paste( select , " ,  t." , c , sep = "");
}
select <- paste( select , "1/cast(s.noPlots as real) * ( s.noClusters / cast(s.noClusters-1 as real) ) * t.x as var" , sep = " , " );
select <- paste( select , "from 
                            tmp t
                          join strata s
                            on t.stratum = s.stratum
                          ");  
stratumVariance <- sqldf( select );

# === (4)
select <- "select v.stratum , v.area";
select <- paste( select , categories , sep =" , ");
select <- paste( select , " , ( s.area * s.area ) * v.var as var                  
                            from 
                              stratumVariance v
                           join strata s
                            on v.stratum = s.stratum" ,
                sep = " ");

areaVariance <- sqldf( select );
( sqrt( areaVariance$var) * 100 ) / areaVariance$area
# === Close db connection
dbDisconnect(connection);