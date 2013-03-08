
estimateTreeExpFactor <- function(trees) {
  # Set slope limit to 80 %  
  trees$slope  <- with( trees,
                        ifelse( slope > 80, 80, slope )
  );
  
  trees$slope <- round( trees$slope / 5, 0 ) * 5;
  
  trees$slope_cf <- with( trees, cos(atan(slope/100)) / cos(.9 * slope * pi / 180) )
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
  trees$exp_factor <- with(trees,  pi * plot_radius^2 * slope_cf);
  #convert tree exp factor from m2 to ha
  trees$exp_factor <- trees$exp_factor * 0.0001;
  return( trees );
}

f <- c('specimen_id','dbh','slope','plot_section_survey_date');
trees <- getTrees( f );
trees <- subset( trees, !is.na(trees$dbh) );

trees <- estimateTreeExpFactor( trees=trees );
data <- trees[,c('specimen_id', 'exp_factor')]

uri <- "/calc/rest/surveys/naforma1/units/tree/specimens/exp-factor";
patchCsv( host, port, uri, data );