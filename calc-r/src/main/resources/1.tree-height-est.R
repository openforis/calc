###############################################################################
# Estimate tree height
# 
# Author:  M. Togna
###############################################################################

#surveys/naforma1/units/tree/specimens?f=specimen_id,stratum_id,cluster_id,plot_section_id,dbh,total_height,health


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
