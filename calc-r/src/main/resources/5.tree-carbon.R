
f <- c('specimen_id' , 'aboveground_biomass' , 'belowground_biomass' , 'vegetation_type' );

trees <- getTrees(fields=f);
trees <- setCarbonRootShootRatio( trees );
trees$carbon <- with(trees, carbonCf * (aboveground_biomass + belowground_biomass) );

data <- trees[, c('specimen_id','carbon')];
data <- subset( data, !is.na(carbon) );
patchCsv( host, port, updateSpecimenValueUri, data );

