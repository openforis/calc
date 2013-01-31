estimateTreeBiomass <- function(trees) {
  # C = cf(AGB + BGB + DWD)
#  trees$aboveground_biomass_per_ha <- with(trees,
#                               ifelse(taxon_code == 'PIN/PAT', est_volume_per_ha * 390,
#                                      ifelse(taxon_code == 'DAL/MEL', est_volume_per_ha * 1060,
#                                             # ifelse(primary_vegetation_type == '2', 0.06 * dbh^2.012 * est_height^0.71,
#                                             ifelse(vegetation_type == '101', est_volume_per_ha * 580,
#                                                    est_volume_per_ha * 500)
#                                             #  )                                             
#                                      )
#                               )
#  );
  
  #convert from kg to tonnes
 # trees$aboveground_biomass_per_ha <- trees$aboveground_biomass_per_ha * 0.001;
  
#  trees$belowground_biomass_per_ha <- trees$aboveground_biomass_per_ha * 0.28;
  trees$aboveground_biomass <- with(trees,
                                           ifelse(taxon_code == 'PIN/PAT', est_volume * 390,
                                                  ifelse(taxon_code == 'DAL/MEL', est_volume * 1060,
                                                         # ifelse(primary_vegetation_type == '2', 0.06 * dbh^2.012 * est_height^0.71,
                                                         ifelse(vegetation_type == '101', est_volume * 580,
                                                                est_volume * 500)
                                                         #  )                                             
                                                  )
                                           )
                                    );
  
  #convert from kg to tonnes
  trees$aboveground_biomass <- trees$aboveground_biomass * 0.001;
  
  trees$belowground_biomass <- trees$aboveground_biomass * 0.28;
  
  return (trees);
}

f <- c('specimen_id','taxon_code','est_volume','vegetation_type')
trees <- getTrees(fields=f);
trees[is.na(trees$vegetation_type),]$vegetation_type <- 0;

trees <- estimateTreeBiomass( trees=trees );

data <- trees[, c('specimen_id','aboveground_biomass','belowground_biomass')];
patchCsv( host, port, updateSpecimenValueUri, data );