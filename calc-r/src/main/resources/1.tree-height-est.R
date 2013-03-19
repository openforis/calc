###############################################################################
# Estimate tree height
# 
# Author:  M. Togna
###############################################################################

#surveys/naforma1/units/tree/specimens?f=specimen_id,stratum_id,cluster_id,plot_section_id,dbh,total_height,health

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

f <- c('specimen_id','dbh','total_height','taxon_code','vegetation_type')
trees <- getTrees( f );
# trees$species_code <- toupper(trees$species_code);  

#Remove trees with no dbh
trees <- subset( trees, !is.na(trees$dbh) );
  
#Remove baobab from trees (for growing stock volume estimation)
#trees <- subset( trees, species_code != 'ADA/DIG' );

trees <- estimateTreeHeight( trees );
trees <- limitTreeHeight( trees );

#names(trees)
#data <- subset(trees, is.na(trees$total_height) );
data <- trees[, c('specimen_id','est_height')];
names(data) <- c('specimen_id','total_height');

patchCsv( host, port, updateSpecimenValueUri, data );
