estimateTreePlotArea <- function(trees) {
  # Set slope limit to 80 %  
  trees$slope  <- with( trees,
                        ifelse( slope > 80, 80, slope )
                      );
  
  trees$slope <- round( trees$slope / 5, 0 ) * 5;
  
  trees$slope_cf <- with( trees, cos(atan(slope/100)) / cos(.9 * slope * pi / 180) )
  #    cos(atan(slopePercent/100)) / 
  #		cos(.9 * slopePercent * pi / 180)
  
  #trees$time_study <- as.numeric( paste(trees$time_study_date_year, sprintf( "%02d", trees$time_study_date_month ), sprintf( "%02d", trees$time_study_date_day ), sep='') )
  
  #After 14 may 2011 when dbh < 5, plot radius is 1m
  trees$plot_section_survey_date <- as.Date(trees$plot_section_survey_date);
  trees$plot_radius <- with(trees,
                            ifelse(dbh < 5 & plot_section_survey_date <= '2011-05-13', 2,
                                   ifelse(dbh < 5, 1,        
                                          ifelse(dbh < 10, 5,
                                                 ifelse(dbh < 20, 10, 15)))));
  
  # Area in which trees were observed (m2)
  trees$plot_area <- with(trees,  pi * plot_radius^2 * slope_cf);
 
  return( trees );
}

estimateTreeVolume <- function(trees) {
  # Basic volume model
  #trees$est_volume <- with( trees, 0.5 *pi * (0.01 * dbh / 2)^2 * est_height );
  
  # EUC/GRA    V  =  0.000065 D^1.633 H^1.137  
  # PIN/PAT  	V = 0.00002117 D^1.8644 H^1.3246  
  # TCT/GRA		V=0.0001D^1.91*H^0.75  
  # DAL/MEL		V= 0.00023D^2.231
  # Woodlands (Malimbwi) V    = 0.0001 DBH^2.032*H^0.66
  # else 0.5 *pi * (0.01 * dbh / 2)^2 * est_height
  
  trees$est_volume <- with(trees,
                           ifelse(taxon_code == 'EUC/GRA', 0.000065 * dbh^1.633 * est_height^1.137 ,
                                  ifelse(taxon_code == 'PIN/PAT' , 0.00002117 * dbh^1.8644 * est_height^1.3246 ,
                                         ifelse(taxon_code == 'TCT/GRA', 0.0001 * dbh^1.91 * est_height^0.75 ,
                                                ifelse(taxon_code == 'DAL/MEL' , 0.00023 * dbh^2.231, 
                                                       ifelse(round(vegetation_type/100) == 2, 0.0001 * dbh^2.032 * est_height^0.66,
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
  
  trees <- estimateTreePlotArea( trees=trees );
  
  # Vol per ha per tree (m3/ha)
  trees$est_volume_per_ha <- with(trees, 10000 * est_volume / plot_area);
  
  
  return (trees);
}

f <- c('specimen_id','dbh','est_height','taxon_code','vegetation_type','slope','plot_section_survey_date')
trees <- getTrees( f );
trees <- subset( trees, !is.na(trees$dbh) );
trees[is.na(trees$vegetation_type),]$vegetation_type <- 0;

#Estimate volume
trees <- estimateTreeVolume( trees=trees );

#Upload results
data <- trees[ ,c('specimen_id','est_volume', 'est_volume_per_ha') ];
patchCsv( host, port, updateSpecimenValueUri, data );

#data <- trees[ ,c('specimen_id','est_volume_per_ha') ];
#patchCsv( host, port, updateSpecimenValueUri, data);
