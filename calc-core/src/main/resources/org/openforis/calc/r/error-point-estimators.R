### ============================================================== 
# Error calculation script based on 
# "Formulas for estimators and their variances in NFI 28.2.2014 K.T. Korhonen & Olli Salmensuu, point estimators"
#
# @author Mino Togna
### ==============================================================

library( "sqldf" );

# sqldf options. 
# driver is SQLLite in order to read from dataframe , otherwise it uses PostgreSQL which is the default driver used by Calc
# https://code.google.com/p/sqldf/#Troubleshooting
options (
  #gsubfn.engine = "R" , 
  sqldf.driver = "SQLite"
);


# ====
# extract a dataframe of unique clusters included in the data argument
# ====
getClusters <- function( data ) {
  # clusters
  clusters <- sqldf( "select distinct 
                        stratum ,  
                        cluster ,
                        sum(weight) as noPlots ,
                        sum(class * weight) as noPlotsInClass
                       from 
                        data
                       group by
                        stratum ,
                        cluster" );
  
  return ( clusters );
};

# ====
# add no. plots and no. clusters to all strata 
# ====
addStratumCounts <- function( strata , clusters , plots ) {
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
  return ( strata );
};

calcualteStratumAreaVariances <- function( plots , strata ){
  clusters <- getClusters( plots );
  strata <- addStratumCounts( strata , clusters , plots );
  
  # == (1)
  strata$propInClass <- strata$noPlotsInClass / strata$noPlots;
  
  # == (2)
  strata$areaInClass <- strata$propInClass * strata$area;
  
  # == (3)
  clusters <- sqldf("select c.*, s.propInClass 
                    from clusters c
                    join strata s
                    on s.stratum = c.stratum");
  
  clusters$x <- (clusters$noPlotsInClass - clusters$propInClass * clusters$noPlots ) ^ 2;
  
  strata <- sqldf( "select 
                        s.*, 
                        sum(c.x) as x 
                     from strata s
                     left outer join clusters c
                     on s.stratum = c.stratum
                    group by s.stratum");
  strata$var <- 1 / (strata$noPlots^2) * strata$noClusters / (strata$noClusters -1 ) * strata$x ;
  # == (4)
  strata$areaVar <- strata$area^2 * strata$var;
  
  # add se%(A(f))
  strata$seArea <- 100 * sqrt( strata$areaVar ) / strata$areaInClass;
  
  return (strata);
};

calculateAreaError <- function(  plots , strata  ) {  
  results <- calcualteStratumAreaVariances(plots=plots , strata=strata);
  strata <-sqldf( "select s.* , 
                    r.areaInClass , 
                    r.areaVar, 
                    r.seArea 
                  from 
                    strata s 
                  join 
                    results as r 
                  on 
                    r.stratum = s.stratum" );
  
  
  
  errors <- data.frame( matrix(nrow=1, ncol=3) );
  names(errors) <- c( 'variance' , 'absolute' , 'relative' );
  
  errors$variance <- sum( strata$areaVar );
  errors$absolute <- sqrt( errors$variance );
  errors$relative <- 100 * errors$absolute / sum( strata$areaInClass );
  
  return ( errors );
};

calculateStratumQuantityVariances <- function( data , plots , strata ) {  
  # add plot weight to data
  data <- sqldf( "select d.*, p.weight from data d inner join plots p on d.plot_id = p.plot_id" );
  
  results <- calcualteStratumAreaVariances(plots=data , strata=strata);
  strata <-sqldf( "select s.* , 
                    r.areaInClass , 
                    r.areaVar, 
                    r.seArea 
                  from 
                    strata s 
                  join 
                    results as r 
                  on 
                    r.stratum = s.stratum" );
  
  
  clusters <- getClusters( data );  
  strata <- addStratumCounts( strata , clusters , data );
  
  #== add sum of quantity per ha to strata and clusters
  strata <- sqldf(" select s.*, d.quantity from strata s join ( select stratum, sum(quantity * weight * class) as quantity from data group by stratum ) as d on s.stratum = d.stratum");
  clusters <- sqldf(" select c.*, d.quantity from clusters c join ( select cluster, sum(quantity * weight * class) as quantity from data group by cluster ) as d on c.cluster = d.cluster");
  
  #== (5) = (7) / (6)
  strata$meanQuantity <- strata$quantity / strata$noPlotsInClass ;
  
  clusters <- sqldf("select c.*, s.meanQuantity 
                  from clusters c
                  join strata s
                  on s.stratum = c.stratum");
  
  clusters$x <- (clusters$quantity - clusters$meanQuantity * clusters$noPlotsInClass ) ^ 2;
  
  strata <- sqldf( "select 
                 s.*, 
                 sum(c.x) as x 
                 from strata s
                 left outer join clusters c
                 on s.stratum = c.stratum
                 group by s.stratum" );
  #== (8)
  strata$varMeanQuantity <- 1 / (strata$noPlotsInClass^2) * strata$noClusters / (strata$noClusters - 1 ) * strata$x;  
  
  # == (9)
  strata$totalQuantity <- strata$areaInClass * strata$meanQuantity ;
  
  # == (10)
  strata$varTotalQuantity <- strata$areaInClass^2 * strata$varMeanQuantity + strata$meanQuantity^2 * strata$areaVar;
  
  # add se for mean quantity se%(x(f))
  strata$seMeanQuantity <- 100 * sqrt( strata$varMeanQuantity ) / strata$meanQuantity;
  
  strata$seTotalQuantity <- sqrt( strata$seMeanQuantity^2 + strata$seArea^2 );
  
  return (strata);
};



calculateQuantityError <- function( data , plots , strata ) {  
    
  results <- calculateStratumQuantityVariances(data=data , plots=plots , strata=strata);
  strata <-sqldf( "select 
                    s.* , 
                    r.areaInClass,
                    r.areaVar,
                    r.meanQuantity,                    
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
  
  errors <- data.frame( matrix(nrow=1, ncol=6) );
  names(errors) <- c( 'variance_total' , 'variance_mean' ,'absolute_total' , 'relative_total' , 'absolute_mean' , 'relative_mean');
  
  errors$variance_total <- sum( strata$varTotalQuantity );
  
  meanQuantity <- sum(strata$totalQuantity) / sum(strata$areaInClass) ;
  errors$variance_mean <- ( errors$variance_total - meanQuantity^2 * sum(strata$areaVar) ) / sum( strata$areaInClass ) ^ 2 ;
  
  errors$absolute_mean <- sqrt( errors$variance_mean );  
  errors$relative_mean <- 100 * sqrt( errors$variance_mean ) / meanQuantity;
                                                            
  errors$absolute_total <- sqrt( errors$variance_total );
  
  areaError <- 100 * sqrt( sum( strata$areaVar ) ) / sum( strata$areaInClass );
  errors$relative_total <- sqrt( errors$relative_mean^2 + areaError^2 );
  
  return ( errors );
};