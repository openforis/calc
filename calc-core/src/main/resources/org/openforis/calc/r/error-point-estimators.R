### ============================================================== 
# Error calculation script based on 
# "Formulas for estimators and their variances in NFI 28.2.2014 K.T. Korhonen & Olli Salmensuu, point estimators"
#
# @author Mino Togna
### ==============================================================

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
calcualteAreaError <- function( plots , strata ){
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
  strata$areaVariance <- strata$area^2 * strata$var;  
  #absolute error
  strata$areaAbsoluteError <- sqrt( strata$areaVariance );
  # add se%(A(f)) - relative error
  strata$areaRelativeError <- 100 * strata$areaAbsoluteError / strata$areaInClass;  
  return (strata);
};
calcualteAreaErrorStratified <- function(  plots , strata  ) {  
  results <- calcualteAreaError( plots=plots , strata=strata );
  strata <-sqldf( "select s.* , 
                    r.areaInClass , 
                    r.areaVariance, 
                    r.areaRelativeError 
                  from 
                    strata s 
                  join 
                    results as r 
                  on 
                    r.stratum = s.stratum" );
  errors <- data.frame( matrix(nrow=1, ncol=3) );
  names(errors) <- c( 'areaVariance' , 'areaAbsoluteError' , 'areaRelativeError' );  
  errors$areaVariance <- sum( strata$areaVariance );
  errors$areaAbsoluteError <- sqrt( errors$areaVariance );
  errors$areaRelativeError <- 100 * errors$areaAbsoluteError / sum( strata$areaInClass );  
  return ( errors );
};
calculateQuantityError <- function( data , plots, strata ) {  
  # add plot weight to data
  data <- sqldf( "select d.*, p.weight from data d inner join plots p on d.plot_id = p.plot_id" );  
  results <- calcualteAreaError( plots=plots , strata=strata );
  strata <-sqldf( "select s.* , 
                    r.areaInClass , 
                    r.areaVariance, 
                    r.areaRelativeError
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
  strata$meanQuantityVariance <- 1 / (strata$noPlotsInClass^2) * strata$noClusters / (strata$noClusters - 1 ) * strata$x;    
  # == (9)
  strata$totalQuantity <- strata$areaInClass * strata$meanQuantity ;  
  # == (10)
  strata$totalQuantityVariance <- strata$areaInClass^2 * strata$meanQuantityVariance + strata$meanQuantity^2 * strata$areaVariance;  
  # add se for mean quantity se%(x(f))
  strata$meanQuantityAbsolute <- sqrt( strata$meanQuantityVariance );  
  strata$meanQuantityRelative <- 100 * strata$meanQuantityAbsolute / strata$meanQuantity;  
  strata$totalQuantityAbsolute <- sqrt( strata$totalQuantityVariance );
  strata$totalQuantityRelative <- sqrt( strata$meanQuantityRelative^2 + strata$areaRelativeError^2 );  
  return (strata);
};
calculateQuantityErrorStratified <- function( data , plots, strata ) {
  results <- calculateQuantityError(data=data , plots=plots, strata=strata);
  strata <-sqldf( "select 
                    s.* , 
                    r.areaInClass,
                    r.areaVariance,
                    r.meanQuantity,                    
                    r.meanQuantityVariance , 
                    r.totalQuantityVariance , 
                    r.totalQuantity ,
                    r.meanQuantityRelative,
                    r.totalQuantityRelative
                  from 
                    strata s 
                  join 
                    results as r 
                  on 
                    r.stratum = s.stratum" );  
  errors <- data.frame( matrix(nrow=1, ncol=6) );
  names(errors) <- c( 'totalQuantityVariance' , 'meanQuantityVariance' ,'totalQuantityAbsolute' , 'totalQuantityRelative' , 'meanQuantityAbsolute' , 'meanQuantityRelative');  
  errors$totalQuantityVariance <- sum( strata$totalQuantityVariance );  
  meanQuantity <- sum(strata$totalQuantity) / sum(strata$areaInClass) ;
  errors$meanQuantityVariance <- ( errors$totalQuantityVariance - meanQuantity^2 * sum(strata$areaVariance) ) / sum( strata$areaInClass ) ^ 2 ;  
  errors$meanQuantityAbsolute <- sqrt( errors$meanQuantityVariance );  
  errors$meanQuantityRelative <- 100 * sqrt( errors$meanQuantityVariance ) / meanQuantity;                                                            
  errors$totalQuantityAbsolute <- sqrt( errors$totalQuantityVariance );  
  areaError <- 100 * sqrt( sum( strata$areaVariance ) ) / sum( strata$areaInClass );
  errors$totalQuantityRelative <- sqrt( errors$meanQuantityRelative^2 + areaError^2 );  
  return ( errors );
};