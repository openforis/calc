
f <- c( 'specimen_id', 'volume' );
trees <- getTrees(fields=f);

trees <- estimateTreeBoleVolume( trees );


#data <- trees[, c('specimen_id','aboveground_biomass','belowground_biomass', 'avg_aboveground_biomass', 'avg_belowground_biomass')];
data <- trees[ , c('specimen_id','bole_volume') ];
patchCsv( host, port, updateSpecimenValueUri, data );