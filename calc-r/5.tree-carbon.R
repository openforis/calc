setCarbonRootShootRatio <- function( data ){
  
  data$carbonCf <- 
    with(
      data,
      ifelse( vegetation_type == '102' , 0.37,
        ifelse( vegetation_type == '101' , 0.27,
          ifelse( vegetation_type %in% c('301','302','303','304','305','306') , 0.40,
            ifelse( vegetation_type %in% c('202','203','401','402','403','404','503','504','505','506','601','602','603','701','702','703','800') , 0.37,        
              ifelse( vegetation_type %in% c('103','201','501','502') , 0.28, 
                ifelse( vegetation_type == '104' , 0.20,
                  NA
                )
              )                    
            )            
          )        
        )
      )
        
    );

  return (data);
}

f <- c('specimen_id' , 'aboveground_biomass' , 'belowground_biomass' , 'vegetation_type' );

trees <- getTrees(fields=f);
trees <- setCarbonRootShootRatio( trees );
trees$carbon <- with(trees, carbonCf * (aboveground_biomass + belowground_biomass) );

data <- trees[, c('specimen_id','carbon')];
data <- subset( data, !is.na(carbon) );
patchCsv( host, port, updateSpecimenValueUri, data );

