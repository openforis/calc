source('src/main/resources/properties.R');

#==================================
#Read Data
#==================================
aoisUri <- paste(calcRestUri,'surveys/naforma1/aoi-hierarchies/admin-unit/aois',sep='/');
aois <- read.csv( aoisUri );

# Only Country level for now
aoi <- subset( aois, aoi_id == 1);

# plot1 count
plot1CntUri <- paste( calcRestUri, 'surveys/naforma1/units/plot/counts', sep ='/' );
strata <- read.csv( plot1CntUri );

# plot2 count 
plot2CntUri <- paste( calcRestUri, 'surveys/naforma1/units/plot/counts?observed=true', sep ='/' );
plot2Cnt <- read.csv( plot2CntUri );
plot2Cnt <-plot2Cnt[, c('stratum_no','aoi_id','plot2_cnt')];

strata <- merge( strata, plot2Cnt, by=c('stratum_no','aoi_id') );

#Plot category distribution
plotCatDistrUri <- paste( calcRestUri, 'surveys/naforma1/units/plot/category-distribution', sep='/' );
plotCatDistr <- read.csv( plotCatDistrUri );

#===============================
# Area estimation
#===============================
#Strata area
strata$proportion <- strata$plot1_cnt / sum( strata$plot1_cnt );
stopifnot( sum(strata$proportion) == 1 );

strata$area <- strata$proportion * aoi$aoi_area;
stopifnot( sum(strata$area) == aoi$aoi_area );

#Expansion factor
strata$expf <- strata$area / strata$plot2_cnt;

expf <- strata[, c('stratum_id','expf')]
plotCatDistr <- merge( plotCatDistr, expf, by='stratum_id' );
plotCatDistr$est_area <- with( plotCatDistr, plot_distribution * expf );

#sum(plotCatDistr$est_area)
#aoi$aoi_area
stopifnot( sum(plotCatDistr$est_area) == aoi$aoi_area );

upload(uri=saveAreaResultsUri, data=plotCatDistr)
# == TO  REMOVE
#f <- plotCatDistr[1:100,]
#write.csv( f, file='~/tmp.csv', row.names=F)
#upFile <- '~/tmp.csv'
#file <- fileUpload(contents=f, contentType='text/csv')
#fileData <- fileUpload("fileId", paste(readLines(upFile), collapse = "\n"), "text/csv")
#postForm(saveAreaResultsUri, style="POST",
#        "fileData" = fileData,
#       .opts = list(verbose = TRUE, header = TRUE))

# Gino
#closeAllConnections();
#conn = textConnection(NULL, "w");
#write.csv(plotCatDistr, conn, row.names=F);
#body = textConnectionValue(conn);


#postForm(saveAreaResultsUri, style="POST",
#         "fileData" = body,
#         .opts = list(verbose = TRUE, header = TRUE));

#close(conn);