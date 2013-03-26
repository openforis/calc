
f <- c('specimen_id', 'taxon_code', 'vegetation_type', 'volume');

deadWoods <- getDeadWoods(fields=f);
deadWoods[is.na(deadWoods$vegetation_type),]$vegetation_type <- 0;
deadWoods <- subset( deadWoods , !is.na(volume) );
deadWoods <- estimateTreeBiomass( deadWoods );

data <- deadWoods[, c('specimen_id','aboveground_biomass','belowground_biomass')];
#nrow( data[is.na(data$aboveground_biomass),] )
patchCsv( host, port, updateDeadWoodValueUri, data );