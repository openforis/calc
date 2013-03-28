
getTrees <- function( fields ) {
  params <- paste( fields, collapse=',' );
  
  uri <- paste( calcRestUri, 'surveys/naforma1/units/tree/specimens?f=',sep='/' );
  uri <- paste( uri, params, sep='' );
  #  print(uri)
  trees <- read.csv( uri );
  return ( trees );
}

getPlotSections <- function( fields ) {
  
  params <- paste( fields, collapse=',' );
  uri <- sprintf('%s/%s?f=%s', calcRestUri, 'surveys/naforma1/units/plot/observations' , params );
  #uri <- paste( calcRestUri, 'surveys/naforma1/units/plot/observations?f=',sep='/' );
  #uri <- paste( uri, params, sep='' );
  #  print(uri)
  plots <- read.csv( uri );
  return ( plots );
}


getDeadWoods <- function( fields ) {
  params <- paste( fields, collapse=',' );
  
  uri <- paste( calcRestUri, 'surveys/naforma1/units/dead_wood/specimens?f=',sep='/' );
  uri <- paste( uri, params, sep='' );
  #  print(uri)
  trees <- read.csv( uri );
  return ( trees );
}

setCarbonRootShootRatio <- function( data ){
  
  data$carbonCf <- 
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
                                    ifelse(taxon_code == 'PIN/PAT', volume * 390,
                                           ifelse(taxon_code == 'DAL/MEL', volume * 1060,
                                                  # ifelse(primary_vegetation_type == '2', 0.06 * dbh^2.012 * est_height^0.71,
                                                  ifelse(vegetation_type == '101', volume * 580,
                                                         volume * 500)
                                                  #  )                                             
                                           )
                                    )
  );
  
  #convert from kg to tonnes
  trees$aboveground_biomass <- trees$aboveground_biomass * 0.001;
  
  trees$belowground_biomass <- trees$aboveground_biomass * 0.28;
  
  #trees$avg_aboveground_biomass  <- trees$aboveground_biomass / trees$inclusion_area;
  #trees$avg_belowground_biomass  <- trees$belowground_biomass / trees$inclusion_area;
  
  return (trees);
}

estimateTreeVolume <- function(trees) {
  # Basic volume model
  #trees$est_volume <- with( trees, 0.5 *pi * (0.01 * dbh / 2)^2 * est_height );
  
  # EUC/GRA    V  =  0.000065 D^1.633 H^1.137  
  # PIN/PAT    V = 0.00002117 D^1.8644 H^1.3246  
  # TCT/GRA  	V=0.0001D^1.91*H^0.75  
  # DAL/MEL		V= 0.00023D^2.231
  # Woodlands (Malimbwi) V    = 0.0001 DBH^2.032*H^0.66
  # else 0.5 *pi * (0.01 * dbh / 2)^2 * est_height
  
  trees$volume <- with(trees,
                       ifelse(taxon_code == 'EUC/GRA', 0.000065 * dbh^1.633 * total_height^1.137 ,
                              ifelse(taxon_code == 'PIN/PAT' , 0.00002117 * dbh^1.8644 * total_height^1.3246 ,
                                     ifelse(taxon_code == 'TCT/GRA', 0.0001 * dbh^1.91 * total_height^0.75 ,
                                            ifelse(taxon_code == 'DAL/MEL' , 0.00023 * dbh^2.231, 
                                                   ifelse(round(vegetation_type/100) == 2, 0.0001 * dbh^2.032 * total_height^0.66,
                                                          0.5 * pi * (0.01 * dbh / 2)^2 * total_height)
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

limitTreeHeight <- function(trees) {
  # veg type 101: Montane forest, 50m
  # veg type 102: Lowlandforest, 40m
  # Woodlands  and other tree forms, 25m
  
  trees$est_height <- with(trees, 
                           ifelse(est_height > 50 & vegetation_type == 101, 50,
                                  ifelse(est_height > 40 & vegetation_type == 102, 40, 
                                         ifelse(est_height > 30 & vegetation_type == 104, 30, 
                                                ifelse(est_height > 50 & substr(taxon_code,1,3) == 'EUC', 50, 
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
  
  #trees$time_study <- as.numeric( paste(trees$time_study_date_year, sprintf( "%02d", trees$time_study_date_month ), sprintf( "%02d", trees$time_study_date_day ), sep='') )
  
  #After 14 may 2011 when dbh < 5, plot radius is 1m
  trees$plot_section_survey_date <- as.Date(trees$plot_section_survey_date);
  trees$plot_radius <- with(trees,
                            ifelse(dbh < 5 & plot_section_survey_date <= '2011-05-13', 2,
                                   ifelse(dbh < 5, 1,        
                                          ifelse(dbh < 10, 5,
                                                 ifelse(dbh < 20, 10, 15)
                                          )
                                   )
                            )
  );
  
  # Area in which trees were observed (m2)
  #trees$plot_area <- with(trees,  pi * plot_radius^2 * slope_cf);
  trees$inclusion_area <- with(trees,  pi * plot_radius^2 * slope_cf * plot_share / 100);
  #convert tree exp factor from m2 to ha
  trees$inclusion_area <- trees$inclusion_area * 0.0001;
  
  # trees$exp_factor <- with(trees,  1 / (inclusion_area) );
  
  return( trees );
}

estimateDeadWoodInclusionArea <- function( data ) {
  data <- estimateSlopeCf( data );
  data$inclusion_area <- with(data,  pi * 15^2 * slope_cf * plot_share / 100);
  
  #convert  from m2 to ha
  data$inclusion_area <- data$inclusion_area * 0.0001;
  
  return (data);
}

estimateTreeBoleVolume <- function( data ) {
  
  data$bole_volume <- data$volume *  0.68;
  
  return (data);
  
}
