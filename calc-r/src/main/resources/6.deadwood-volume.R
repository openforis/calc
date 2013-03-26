estimateDeadWoodVolume <- function( data ){
  
  data$volume <-
    with( data, 
          ( (pi*(diameter1/200)^2 + pi*(diameter2/200)^2 ) / 2 ) * length      
    );
  
  return (data);
}

f <- c('specimen_id','diameter1','diameter2','length');
deadWoods <- getDeadWoods( f );
deadWoods <- estimateDeadWoodVolume( deadWoods );

data <- deadWoods[, c('specimen_id','volume')];
data <- subset( data, !is.na(volume) );
patchCsv( host, port, updateDeadWoodValueUri, data );

