
estimateTreeVolume <- function(trees) {
  # Basic volume model
  #trees$est_volume <- with( trees, 0.5 *pi * (0.01 * dbh / 2)^2 * est_height );
  
  # EUC/GRA    V  =  0.000065 D^1.633 H^1.137  
  # PIN/PAT  	V = 0.00002117 D^1.8644 H^1.3246  
  # TCT/GRA		V=0.0001D^1.91*H^0.75  
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
  return (trees);
}

f <- c('specimen_id','dbh','total_height','taxon_code','vegetation_type');
trees <- getTrees( f );
trees <- subset( trees, !is.na(trees$dbh) );
trees[is.na(trees$vegetation_type),]$vegetation_type <- 0;

#Estimate volume
trees <- estimateTreeVolume( trees=trees );

#Upload results
#data <- trees[ ,c('specimen_id','est_volume', 'est_volume_per_ha') ];
data <- trees[ ,c('specimen_id','volume') ];
patch( updateSpecimenValueUri, data);

#patchCsv( host, port, updateSpecimenValueUri, data );
#data <- trees[ ,c('specimen_id','est_volume_per_ha') ];
#patchCsv( host, port, updateSpecimenValueUri, data);
