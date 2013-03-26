
f <- c('specimen_id','taxon_code','volume','vegetation_type','inclusion_area');

trees <- getTrees(fields=f);
trees[is.na(trees$vegetation_type),]$vegetation_type <- 0;

trees <- estimateTreeBiomass( trees=trees );

#data <- trees[, c('specimen_id','aboveground_biomass','belowground_biomass', 'avg_aboveground_biomass', 'avg_belowground_biomass')];
data <- trees[, c('specimen_id','aboveground_biomass','belowground_biomass')];
patchCsv( host, port, updateSpecimenValueUri, data );