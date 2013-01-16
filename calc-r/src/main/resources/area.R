source('src/main/resources/properties.R');


aoisUri <- paste(calcRestUri,'surveys/naforma1/aoi-hierarchies/admin-unit/aois',sep='/');
aois <- read.csv( aoisUri );

# Only Country level for now
aoi <- subset( aois, aoi_id == 1);

plot1CntUri <- paste( calcRestUri, 'surveys/naforma1/units/plot/counts', sep ='/' );
strata <- read.csv( plot1CntUri );