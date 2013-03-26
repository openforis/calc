
f <- c('specimen_id' , 'aboveground_biomass' , 'belowground_biomass', 'vegetation_type');

deadWoods <- getDeadWoods(fields=f);
deadWoods <- subset( deadWoods, !is.na(aboveground_biomass) & !is.na(belowground_biomass) );

#deadWoods[is.na(deadWoods$vegetation_type),]$vegetation_type <- 0;

deadWoods <- setCarbonRootShootRatio( deadWoods );
deadWoods$carbon <- with(deadWoods, carbonCf * (aboveground_biomass + belowground_biomass) );

data <- deadWoods[, c('specimen_id','carbon')];
data <- subset( data, !is.na(carbon) );
patchCsv( host, port, updateDeadWoodValueUri, data );


