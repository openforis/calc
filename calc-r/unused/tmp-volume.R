# Calculate total and mean volumes per land use per vegetation type in AOI
# 
# Author: G. Miceli, M. Togna
###############################################################################


getTrees <- function(fileName) {
  trees <- read.csv(fileName);
  
  trees$cluster_plot_subplot[trees$cluster_plot_subplot == ''] <- 'A';
  
  trees$total_height <- trees$total_height_value;
  trees$dbh <- trees$dbh_value;
  trees$species_code <- toupper(trees$species_code);
  
  #Remove trees with no dbh
  trees <- subset( trees, !is.na(trees$dbh) );
  
  #Remove baobab from trees (for growing stock volume estimation)
  trees <- subset( trees, species_code != 'ADA/DIG' );
  
  return (trees);
}

estimateTreeHeight <- function(trees) {
  sample_trees <- subset(trees, total_height > 0);
  
  # Naive model for tree height
  height_model <- with( sample_trees, lm(total_height ~ dbh  + I(dbh ^2) + I(dbh ^3)) );
  
  #summary(height_model)
  
  trees$est_height <- predict( height_model, newdata = trees[,c('dbh','total_height')] );
  
  trees$est_height <- with( trees, ifelse(is.na(total_height), est_height, total_height) );
  
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
                                                ifelse(est_height > 50 & substr(species_code,1,3) == 'EUC', 50, 
                                                       ifelse(est_height > 25, 25 , est_height)
                                                )
                                         )
                                  )
                           )
  )
  return (trees);
  
}

estimateTreeVolume <- function(trees) {
  # Basic volume model
  #trees$est_volume <- with( trees, 0.5 *pi * (0.01 * dbh / 2)^2 * est_height );
  
  # EUC/GRA    V  =  0.000065 D^1.633 H^1.137  
  # PIN/PAT		V = 0.00002117 D^1.8644 H^1.3246  
  # TCT/GRA		V=0.0001D^1.91*H^0.75  
  # DAL/MEL		V= 0.00023D^2.231
  # Woodlands (Malimbwi) V    = 0.0001 DBH^2.032*H^0.66
  # else 0.5 *pi * (0.01 * dbh / 2)^2 * est_height
  
  trees$est_volume <- with(trees,
                           ifelse(species_code == 'EUC/GRA', 0.000065 * dbh^1.633 * est_height^1.137 ,
                                  ifelse(species_code == 'PIN/PAT' , 0.00002117 * dbh^1.8644 * est_height^1.3246 ,
                                         ifelse(species_code == 'TCT/GRA', 0.0001 * dbh^1.91 * est_height^0.75 ,
                                                ifelse(species_code == 'DAL/MEL' , 0.00023 * dbh^2.231, 
                                                       ifelse(primary_vegetation_type == 2, 0.0001 * dbh^2.032 * est_height^0.66,
                                                              0.5 * pi * (0.01 * dbh / 2)^2 * est_height)
                                                )
                                         )
                                  )
                           )
  );
  trees$est_volume_function <- with(trees,
                                    ifelse(species_code == 'EUC/GRA', 1 ,
                                           ifelse(species_code == 'PIN/PAT' , 2 ,
                                                  ifelse(species_code == 'TCT/GRA', 3 ,
                                                         ifelse(species_code == 'DAL/MEL' , 4, 
                                                                ifelse(primary_vegetation_type == 2, 5,
                                                                       6)
                                                         )
                                                  )
                                           )
                                    )
  );
  
  #trees$slope_cf <- 1
  
  trees$slope_value <- round(trees$slope_value/5, 0) * 5
  trees$slope_cf <- with( trees, cos(atan(slope_value/100)) / cos(.9 * slope_value * pi / 180) )
  #  	cos(atan(slopePercent/100)) / 
  #		cos(.9 * slopePercent * pi / 180)
  
  trees$time_study <- as.numeric( paste(trees$time_study_date_year, sprintf( "%02d", trees$time_study_date_month ), sprintf( "%02d", trees$time_study_date_day ), sep='') )
  
  #After 14 may 2011 when dbh < 5, plot radius is 1m
  trees$plot_radius <- with(trees,
                            ifelse(dbh < 5 & time_study <= 20110513, 2,
                                   ifelse(dbh < 5, 1,        
                                          ifelse(dbh < 10, 5,
                                                 ifelse(dbh < 20, 10, 15)))))
  
  # Area in which trees were observed (m2)
  trees$plot_area <- with(trees,  pi * plot_radius^2 * slope_cf)
  
  # Vol per ha per tree (m3/ha)
  trees$mean_volume <- with(trees, 10000 * est_volume / plot_area) 
  
  
  return (trees);
}

estimatePlotVolume <- function(trees) {
  plot_volumes <- aggregate(mean_volume ~ cluster_id + cluster_measurement + cluster_plot_no + cluster_plot_subplot, trees, sum);
  
  #Plot total mean volumes
  # TODO other plot level means
  #trees$mean_dbh3 <- with(trees, dbh^3 / plot_area)
  #trees$mean_dbh2 <- with(trees, dbh^2 / plot_area) 
  #volplot<- with(trees, tapply(total_volume, list(cluster_id, plot_no), sum))
  #notreep<- tapply(notree, list(treevol$clusp), sum)
  #basarep<- tapply(basare, list(treevol$clusp), sum)
  #meand1p<-tapply(meand1, list(treevol$clusp), sum) # for calcualtion of mean diameter
  #meand2p<-tapply(meand2, list(treevol$clusp), sum) # for calcualtion of mean diameter
  #meandp<- meand1p/meand2p
  
  #ppvol<-data.frame(levels(treevol$clusp),volplot)
  
  #a<-merge(a, ppvol, all=T, by.x=c("clusp2"), by.y=c("levels.treevol.clusp."))
  plot_results <- merge(plot_volumes, plots2aoi, all.y = TRUE, by.x=c('cluster_id','cluster_measurement','cluster_plot_no','cluster_plot_subplot'), by.y=c('cluster_id','cluster_measurement','no','subplot'));
  
  # Assume volume is 0 in plots with no observations
  # TODO FIX THIS e.g. if plots are inaccessible, do not include! 
  plot_results$mean_volume[is.na(plot_results$mean_volume)] <- 0;
  
  # Plot expansion factor (from stratum)
  #plot_results$expf <- with(plot_results, strata$texpf[stratum] * share / 100)
  plot_results$share[is.na(plot_results$share)]<-100;
  plot_results$expf <- with(plot_results, strata$aexpf[stratum] * share / 100);
  
  plot_results$total_volume <- with(plot_results, expf * mean_volume);
  
  return (plot_results);
}


trees <- getTrees("data/trees.csv");

trees <- estimateTreeHeight(trees);

#merge trees with plots
trees <- merge(trees, plots2aoi, by.x=c('cluster_id','cluster_measurement','cluster_plot_no','cluster_plot_subplot'), by.y=c('cluster_id','cluster_measurement','no','subplot'))

#limit tree height
trees <- limitTreeHeight(trees);

# estimate tree volume
trees <- estimateTreeVolume(trees);

# Number of trees per hectare for each tree
trees$density <- 10000 / trees$plot_area

# Basal area of a tree
trees$basal_area <- with(trees, 10000 * pi * (0.01*dbh/2)^2 / plot_area)

#trees$plot_code <- with(trees, paste(cluster_id, plot_no, sep='_'))

plot_results <- estimatePlotVolume(trees);

# Total volume per class per stratum
total_volume_per_class_per_stratum <- with(plot_results, tapply(total_volume, list(stratum, land_use, primary_vegetation_type), sum))
total_volume_per_class_per_stratum[which(is.na(total_volume_per_class_per_stratum))] <- 0

total_volumes <- apply(total_volume_per_class_per_stratum, c(2,3), sum)

# Replace unobserved volumes with 0
total_volumes[which(is.na(total_volumes))] <- 0

# print("Total volumes by land use and vegetation type") 
# print(round(addmargins(total_volumes/1000)))
v <- formatData(round(addmargins(total_volumes/1000)))
writeCsv(v,'country_total_gs_volume')

# Wrong calc with old Erkk's script
# wrong_expf <- with(strata, area / nplot2o)
# wrong_area_per_class_per_stratum <- counts * wrong_expf
# wrong_areas <- apply(wrong_area_per_class_per_stratum, c('land_use', 'primary_vegetation_type'), sum)

#mean_volumes <- addmargins(total_volumes) / addmargins(wrong_areas)
mean_volume_per_class_per_stratum <- total_volume_per_class_per_stratum / area_per_class_per_stratum 
mean_volume_per_class_per_stratum[which(is.na(mean_volume_per_class_per_stratum))] <- 0

mean_volumes <- addmargins(total_volumes) / addmargins(areas)
mean_volumes[which(is.nan(mean_volumes))] <- 0

#print("Mean volumes by land use and vegetation type")
#print(round(mean_volumes, digits=2))
m <- round(mean_volumes, digits=2)
m <- formatData(m)
writeCsv(m, 'country_mean_gs_volume')
