

f <- c('specimen_id','dbh','total_height','taxon_code','vegetation_type');
trees <- getTrees( f );
trees <- subset( trees, !is.na(trees$dbh) );
trees[is.na(trees$vegetation_type),]$vegetation_type <- 0;

#Estimate volume
trees <- estimateTreeVolume( trees=trees );

#Upload results

data <- trees[ , c('specimen_id','volume') ];

patchCsv( host, port, updateSpecimenValueUri, data );
