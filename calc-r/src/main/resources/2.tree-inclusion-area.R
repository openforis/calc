estimateSlopeCf <- function( obj ){
  
  obj$slope  <- with( obj,
                        ifelse( slope > 80, 80, slope )
  );
  
  obj$slope <- round( obj$slope / 5, 0 ) * 5;
  
  obj$slope_cf <- with( obj, cos(atan(slope/100)) / cos(.9 * slope * pi / 180) );
  
  return( obj );
}


estimateTreeInclusionArea <- function(trees) {
  # Set slope limit to 80 %  
  
  trees <- estimateSlopeCf( obj=trees );
  #    cos(atan(slopePercent/100)) / 
  #  	cos(.9 * slopePercent * pi / 180)
  
  #trees$time_study <- as.numeric( paste(trees$time_study_date_year, sprintf( "%02d", trees$time_study_date_month ), sprintf( "%02d", trees$time_study_date_day ), sep='') )
  
  #After 14 may 2011 when dbh < 5, plot radius is 1m
  trees$plot_section_survey_date <- as.Date(trees$plot_section_survey_date);
  trees$plot_radius <- with(trees,
                            ifelse(dbh < 5 & plot_section_survey_date <= '2011-05-13', 2,
                                   ifelse(dbh < 5, 1,        
                                          ifelse(dbh < 10, 5,
                                                 ifelse(dbh < 20, 10, 15)
                                                 )
                                          )
                                   )
                            );
  
  # Area in which trees were observed (m2)
  #trees$plot_area <- with(trees,  pi * plot_radius^2 * slope_cf);
  trees$inclusion_area <- with(trees,  pi * plot_radius^2 * slope_cf);
  #convert tree exp factor from m2 to ha
  trees$inclusion_area <- trees$inclusion_area * 0.0001;
  
  trees$exp_factor <- with(trees,  1 / (inclusion_area * plot_share / 100) );
    
  return( trees );
}

f <- c('specimen_id','dbh','slope','plot_section_survey_date','plot_share');
trees <- getTrees( f );
trees <- subset( trees, !is.na(trees$dbh) );

trees <- estimateTreeInclusionArea( trees=trees );
#data <- trees[ , c('specimen_id', 'inclusion_area', 'plot_area')];
data <-  trees[ , c('specimen_id', 'exp_factor')];
  
#patchCsv( host, port, updateSpecimenValueUri, data );
patchCsv( host, port, updateSpecimenExpFactorUri, data );



fp <- c('plot_section_id','slope','plot_share');
plotSections <- getPlotSections(fields=fp);
plotSections <- estimateSlopeCf( obj=plotSections );
plotSections$area <- with(plotSections, pi * 15^2 * slope_cf * 0.0001);
patchCsv( host, port, updatePlotSectionAreaUri, data );