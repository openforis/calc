# ============================ Tree inclusion area
f <- c('specimen_id','dbh','slope','plot_section_survey_date','plot_share');
trees <- getTrees( f );
trees <- subset( trees, !is.na(trees$dbh) );

trees <- estimateSpecimenInclusionArea( trees=trees );
data <-  trees[ , c('specimen_id', 'inclusion_area')];
  
patchCsv( host, port, updateSpecimenInclusionAreaUri, data );

# ============================ Dead wood inclusion area
f <- c('specimen_id','slope','plot_section_survey_date', 'plot_share');
deadWoods <- getDeadWoods( f );
deadWoods <- estimateDeadWoodInclusionArea( deadWoods );
data <-  deadWoods[ , c('specimen_id', 'inclusion_area')];

patchCsv( host, port, updateDeadWoodInclusionAreaUri, data );


#=========================== Plot Area
fp <- c('plot_section_id','slope','plot_share');
plotSections <- getPlotSections(fields=fp);

plotSections <- estimateSlopeCf( obj=plotSections );

plotSections$area <- with(plotSections, pi * 15^2 * slope_cf * plot_share / 100 * 0.0001);
plotSections <- plotSections[ , c('plot_section_id','area') ];
patchCsv( host, port, updatePlotSectionAreaUri, plotSections );