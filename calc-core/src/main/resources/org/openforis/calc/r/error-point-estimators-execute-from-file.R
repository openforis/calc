rm( list=ls() );
source( "error-point-estimators.R" );

plots <- read.csv( 'calc-test-data/plots.csv' );
strata <- read.csv( 'calc-test-data/strata.csv' );
data <- read.csv( 'calc-test-data/trees.csv' );


areaArr <- calculateAreaError( plots=data, strata=strata );
qtyErr <- calculateQuantityError( data=data, plots=plots, strata=strata );

