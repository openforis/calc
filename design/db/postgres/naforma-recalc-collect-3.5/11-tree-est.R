#######
#sudo apt-get install libpq-dev
#install.packages("RPostgreSQL")
#R CMD INSTALL --build lmfor

##### 
print( " ====== Process started " );
print( date() );


library('lmfor');
library('RPostgreSQL');

# ================================================ Functions

openConnection <- function() {  
  driver <- dbDriver('PostgreSQL');
  
  con <- dbConnect(driver, dbname="naforma-revisited", host="localhost", port=5432, user="calc", password="calc123")
  
  dbSendQuery(conn=con, statement= "SET search_path TO naforma1");
  
  return (con);
}

closeConnection <- function( con ){
  
  dbDisconnect(con);
  
}

getTrees <- function( ) {
  trees <- 
    dbGetQuery( conn=con , statement=                  
                  "select 
                    p.cluster_id_,
                    p.plot_id_,
                    t.tree_id_, 
                    t.total_height, 
                    t.dbh, 
                    t.species, 
                    t.health,                     
                    p.vegetation_type, 
                    p.slope, 
                    p.plot_time_study_date,
                    p.share
                  from 
                    tree t 
                  join 
                    _plot p 
                  on 
                    t.plot_id_ = p.plot_id_"
  );
  return (trees);
}

getDeadwoods <- function( ) {
  trees <- 
    dbGetQuery( conn=con , statement=
                  "
                  select
                    d.dead_wood_id_,
                    d.diameter1,
                    d.diameter2,
                    d.length,
                    d.dead_wood_species_code as species,
                    p.vegetation_type,
                    p.slope, 
                    p.plot_time_study_date,
                    p.share
                  from
                    naforma1.dead_wood d
                  join
                    naforma1._plot p
                    on d.plot_id_ = p.plot_id_
                "
    );
  return (trees);
}

estimateTreeHeight <- function( trees ) {
  sample_trees <- subset( trees, total_height > 0 );
  
  # Naive model for tree height
  height_model <- with( sample_trees, lm(total_height ~ dbh  + I(dbh ^2) + I(dbh ^3)) );
  
  #summary(height_model)  
  trees$est_height <- predict( height_model, newdata = trees[,c('dbh','total_height')] );  
  trees$est_height <- with( trees, ifelse(
    is.na(total_height), 
    est_height, 
    total_height) 
  );
  
  # Cap predicted height at 40m  
  #trees$est_height <- with( trees, ifelse(est_height > 40, 40, est_height) );
  
  return (trees);
}

estimateTreeHeightLM <- function( trees ) {
  # copy trees
  data <- trees;
  
  data$D <- data$dbh;
  data$H <- data$total_height;
  data$clustID <- data$"cluster_id_";
  
  #data$clustID <- 1000 * data$clusx + data$clusy
  #data$clustID <- data$id
  
  data$D[data$D <= 0] <- NA;
  # SELECT ONLY LIVING TREES FOR ANALYSIS and trees where total h >= 1.35
  # data$H[data$health.id == 7] <- -1
  data$H[data$H < 1.35] <- NA;
  
  # use models calibrated for plots
  im1 <- ImputeHeights(data$D,data$H,data$"plot_id_",makeplot=FALSE,varf=FALSE);
  # use models calibrated for clusters
  im2 <- ImputeHeights(data$D,data$H,data$clustID,makeplot=FALSE,varf=FALSE);
  # use fixed part of the model only
  im3 <- ImputeHeights(data$D,data$H,data$"plot_id_",makeplot=FALSE,varf=FALSE,level=0);
  
  # By default, use predictions from a model having plot-level random effects
  # If plot-effects are not known (no trees per plot, i.e. predType==2), 
  # use predictions from a model having cluster-level random effects
  # If there are no trees per cluster (should happen very seldom)
  # then use the fixed part predictions of a model having plot-level random effects.
  
  hpred <-im1$h                                   
  hpred[im1$predType==2]<-im2$h[im1$predType==2] 
  hpred[im2$predType==2]<-im3$h[im2$predType==2] 
  
  
  # Add imputed heights into the data (hpred) and a column indicating teh type of prediction (hpredType):
  # 0: tree has been measured, hpred includes the measured height
  # 1: tree height has been predicted either using a plot-level or cluster lever random effect
  # 2: tree height has been predicted using the fixed part of the model
  trees <- cbind(data, est_height = hpred, est_height_prediction_type = im2$predType);
  
  return (trees);
}

limitTreeHeight <- function(trees) {
  # veg type 101: Montane forest, 50m
  # veg type 102: Lowlandforest, 40m
  # Woodlands  and other tree forms, 25m
  
  trees$est_height <- with(trees, 
                           ifelse(est_height > 50 & vegetation_type == 101, 50,
                                  ifelse(est_height > 40 & vegetation_type == 102, 40, 
                                         ifelse(est_height > 30 & vegetation_type == 104, 30, 
                                                ifelse(est_height > 50 & substr(species,1,3) == 'EUC', 50, 
                                                       ifelse(est_height > 25, 25 , est_height)
                                                )
                                         )
                                  )
                           )
  );
  return (trees);  
}

estimateSlopeCf <- function( obj ){
  
  obj$slope  <- with( obj,
                      ifelse( slope > 80, 80, slope )
  );
  
  obj$slope <- round( obj$slope / 5, 0 ) * 5;
  
  obj$slope_cf <- with( obj, cos(atan(slope/100)) / cos(.9 * slope * pi / 180) );
  
  return( obj );
}

estimateSpecimenInclusionArea <- function(trees) {
  # Set slope limit to 80 %  
  
  trees <- estimateSlopeCf( obj=trees );
  #    cos(atan(slopePercent/100)) / 
  #    cos(.9 * slopePercent * pi / 180)
  
  #trees$time_study <- as.numeric( paste(trees$plot_time_study_date_year, sprintf( "%02d", trees$plot_time_study_date_month ), sprintf( "%02d", trees$plot_time_study_date_day ), sep='') )
  
  #After 14 may 2011 when dbh < 5, plot radius is 1m
  trees$plot_time_study_date <- as.Date(trees$plot_time_study_date);
  trees$plot_radius <- with(trees,
                            ifelse(dbh < 5 & plot_time_study_date <= '2011-05-13', 2,
                                   ifelse(dbh < 5, 1,        
                                          ifelse(dbh < 10, 5,
                                                 ifelse(dbh < 20, 10, 15)
                                          )
                                   )
                            )
  );
  
  # Area in which trees were observed (m2)
  #trees$plot_area <- with(trees,  pi * plot_radius^2 * slope_cf);
  trees$inclusion_area <- with(trees,  pi * plot_radius^2 * slope_cf * share / 100);
  #convert tree exp factor from m2 to ha
  trees$inclusion_area <- trees$inclusion_area * 0.0001;
  
  # trees$exp_factor <- with(trees,  1 / (inclusion_area) );
  
  return( trees );
}

estimateTreeVolume <- function(trees) {
  # Basic volume model
  #trees$est_volume <- with( trees, 0.5 *pi * (0.01 * dbh / 2)^2 * est_height );
  
  # EUC/GRA    V  =  0.000065 D^1.633 H^1.137  
  # PIN/PAT    V = 0.00002117 D^1.8644 H^1.3246  
  # TCT/GRA    V=0.0001D^1.91*H^0.75  
  # DAL/MEL    V= 0.00023D^2.231
  # Woodlands (Malimbwi) V    = 0.0001 DBH^2.032*H^0.66
  # else 0.5 *pi * (0.01 * dbh / 2)^2 * est_height
  
  trees$volume <- with(trees,
                       ifelse(species == 'EUC/GRA', 0.000065 * dbh^1.633 * est_height^1.137 ,
                              ifelse(species == 'PIN/PAT' , 0.00002117 * dbh^1.8644 * est_height^1.3246 ,
                                     ifelse(species == 'TCT/GRA', 0.0001 * dbh^1.91 * est_height^0.75 ,
                                            ifelse(species == 'DAL/MEL' , 0.00023 * dbh^2.231, 
                                                   ifelse(round(as.numeric(vegetation_type)/100) == 2, 0.0001 * dbh^2.032 * est_height^0.66,
                                                          0.5 * pi * (0.01 * dbh / 2)^2 * est_height)
                                            )
                                     )
                              )
                       )
  );
  
  #  trees$est_volume_function <- with(trees,
  #                                    ifelse(taxon_code == 'EUC/GRA', 1 ,
  #                                           ifelse(taxon_code == 'PIN/PAT' , 2 ,
  #                                                  ifelse(taxon_code == 'TCT/GRA', 3 ,
  #                                                         ifelse(taxon_code == 'DAL/MEL' , 4, 
  #                                                                ifelse(primary_vegetation_type == 2, 5,
  #                                                                       6)
  #                                                         )
  #                                                  )
  #                                           )
  #                                    )
  #  );
  
  #trees <- estimateTreePlotArea( trees=trees );
  
  # Vol per ha per tree (m3/ha)
  #trees$est_volume_per_ha <- with(trees, 10000 * est_volume / plot_area);    
  
  # trees$avg_volume <- trees$volume / trees$inclusion_area;
  return (trees);
}

# *********************************************************
#  this function adds a column agbTobgb 
#  that represents the ratio of below-ground biomass to above-ground biomass
# (IPCC 2006 guidelines http://www.ipcc-nggip.iges.or.jp/public/gpglulucf/gpglulucf_contents.html ) 
# *********************************************************
setAboveGroungBiomassToBelowGroundBiomassConversionFactor <- function( data ){
  
  data$agbTobgb <- 
    with(
      data,
      ifelse( vegetation_type == '102' , 0.37,
              ifelse( vegetation_type == '101' , 0.27,
                      ifelse( vegetation_type %in% c('301','302','303','304','305','306') , 0.40,
                              ifelse( vegetation_type %in% c('202','203','401','402','403','404','503','504','505','506','601','602','603','701','702','703','800') , 0.37,        
                                      ifelse( vegetation_type %in% c('103','201','501','502') , 0.28, 
                                              ifelse( vegetation_type == '104' , 0.20,
                                                      NA
                                              )
                                      )                    
                              )            
                      )        
              )
      )
      
    );
  
  return (data);
}


estimateTreeBiomass <- function(trees) {
  # C = cf(AGB + BGB + DWD)
  #  trees$aboveground_biomass_per_ha <- with(trees,
  #                               ifelse(taxon_code == 'PIN/PAT', volume_per_ha * 390,
  #                                      ifelse(taxon_code == 'DAL/MEL', volume_per_ha * 1060,
  #                                             # ifelse(primary_vegetation_type == '2', 0.06 * dbh^2.012 * est_height^0.71,
  #                                             ifelse(vegetation_type == '101', volume_per_ha * 580,
  #                                                    volume_per_ha * 500)
  #                                             #  )                                             
  #                                      )
  #                               )
  #  );
  
  #convert from kg to tonnes
  # trees$aboveground_biomass_per_ha <- trees$aboveground_biomass_per_ha * 0.001;
  
  #  trees$belowground_biomass_per_ha <- trees$aboveground_biomass_per_ha * 0.28;
  trees$aboveground_biomass <- with(trees,
                                    ifelse(species == 'PIN/PAT', volume * 390,
                                           ifelse(species == 'DAL/MEL', volume * 1060,
                                                  # ifelse(primary_vegetation_type == '2', 0.06 * dbh^2.012 * est_height^0.71,
                                                  ifelse(vegetation_type == '101', volume * 580,
                                                         volume * 500)
                                                  #  )                                             
                                           )
                                    )
  );
  
  #convert from kg to tonnes
  trees$aboveground_biomass <- trees$aboveground_biomass * 0.001;
  
  trees <- setAboveGroungBiomassToBelowGroundBiomassConversionFactor( trees );  
  trees$belowground_biomass <- trees$aboveground_biomass * trees$agbTobgb;
  
  #trees$avg_aboveground_biomass  <- trees$aboveground_biomass / trees$inclusion_area;
  #trees$avg_belowground_biomass  <- trees$belowground_biomass / trees$inclusion_area;
  
  return (trees);
}

estimateDeadWoodVolume <- function( data ){
  
  data$volume <-
    with( data, 
          ( (pi*(diameter1/200)^2 + pi*(diameter2/200)^2 ) / 2 ) * length      
    );
  
  return (data);
}

estimateDeadWoodInclusionArea <- function( data ) {
  data <- estimateSlopeCf( data );
  data$inclusion_area <- with(data,  pi * 15^2 * slope_cf * share / 100);
  
  #convert  from m2 to ha
  data$inclusion_area <- data$inclusion_area * 0.0001;
  
  return (data);
}

estimateTreeBoleVolume <- function( data ) {
  
  data$bole_volume <- data$volume *  0.68;
  
  return (data);
  
}

estimateTreeBasalArea <- function( data ){
  #10000 *
  data$basal_area <- with(data,  pi * (0.01*dbh/2)^2 / inclusion_area);
  
  return (data);
}

#============================================================================= 
# Estimates
con <- openConnection();
#==================================
# Get Trees
trees <- getTrees();
trees <- subset( trees, !is.na(trees$dbh) );
#trees[is.na(trees$vegetation_type),]$vegetation_type <- 0;


#================================== 
# 1. Tree height est
trees <- estimateTreeHeightLM( trees );
trees <- limitTreeHeight( trees );

#==================================
# 2.  tree inclusion area
trees <- estimateSpecimenInclusionArea( trees=trees );

#==================================
# 3.  volume
trees <- estimateTreeVolume( trees=trees );

#==================================
# 4. biomass
trees <- estimateTreeBiomass( trees=trees );

#==================================
# 5. carbon
#trees <- setCarbonRootShootRatio( trees );
trees$carbon <- with(trees, 0.47 * (aboveground_biomass + belowground_biomass) );

#==================================
# 5.1 basal_area
trees <- estimateTreeBasalArea( trees );


# Get deadwoods
deadWoods <- getDeadwoods( );
#deadWoods[is.na(deadWoods$vegetation_type),]$vegetation_type <- 0;

#==================================
# 6. deadwood volume

deadWoods <- estimateDeadWoodVolume( deadWoods );

deadWoods <- subset( deadWoods , !is.na(volume) );

#==================================
# 7. deadwood biomass
deadWoods <- estimateTreeBiomass( deadWoods );
deadWoods <- subset( deadWoods, !is.na(aboveground_biomass) & !is.na(belowground_biomass) );

#==================================
# 8. deadwood carbon
#deadWoods <- setCarbonRootShootRatio( deadWoods );
deadWoods$carbon <- with(deadWoods, 0.47 * (aboveground_biomass + belowground_biomass) );

#==================================
# 8. deadwood inclusion area
deadWoods <- estimateDeadWoodInclusionArea( deadWoods );

#==================================
# Convert IDs to character strings results
# numeric value in R itself is just a double precision
# floating point number having 53 significant bits, which is shorter than 64 bits.

#names(trees)
trees$"cluster_id_" <- as.character(trees$"cluster_id_");
trees$"plot_id_" <- as.character(trees$"plot_id_");
trees$"tree_id_" <- as.character(trees$"tree_id_");


#==================================
# save results
treeRes <-  trees[ , c('tree_id_', 'est_height','est_height_prediction_type', 'inclusion_area', 'volume','aboveground_biomass', 'belowground_biomass', 'carbon','basal_area') ];
deadwoodRes <- deadWoods[, c('dead_wood_id_','volume','aboveground_biomass','belowground_biomass', 'carbon','inclusion_area')];

dbRemoveTable(con, "_tree_results");
dbRemoveTable(con, "_dead_wood_results");

dbWriteTable(con, "_tree_results", treeRes, row.names=F);
dbWriteTable(con, "_dead_wood_results", deadwoodRes, row.names=F);


dbRemoveTable(con, "_tree");
dbSendQuery(
  conn=con,
  statement=
    "create table _tree as
      select
        t.*,
        p.stratum,
        p.country_id,
        p.zone_id,
        p.region_id,
        p.district_id,
        p.vegetation_type,
        p.land_use,
        p.ownership_type,
        p.undergrowth_type,
        p.soil_structure,
        p.soil_texture,    
        p.erosion,
        p.grazing,
        p.catchment,
        p.shrubs_coverage,
        p.canopy_cover_class,
        r.est_height,
        r.est_height_prediction_type,
        r.inclusion_area,
        r.volume,
        r.aboveground_biomass,
        r.belowground_biomass,
        r.carbon,
        r.basal_area,
        1 as est_cnt
        from
          tree t
        join
          _plot p 
        on 
          p.plot_id_ = t.plot_id_  
        join
          _tree_results r 
        on 
          t.tree_id_ = r.tree_id_::bigint
      
    " 
);

dbRemoveTable(con, "_tree_results");


dbRemoveTable(con, "_dead_wood");
dbSendQuery(
  conn=con,
  statement=
    "create table _dead_wood as
      select
        t.*,        
        r.inclusion_area,
        r.volume,
        r.aboveground_biomass,
        r.belowground_biomass,
        r.carbon
      from
        dead_wood t
      join
        _dead_wood_results r 
      on t.dead_wood_id_ = r.dead_wood_id_::bigint" 
);
dbRemoveTable(con, "_dead_wood_results");


closeConnection(con);


print( date() );
print( " ====== Process ended " );
