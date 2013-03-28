# ===========================================================================================================
# Plots canopy cover
f <- c('plot_section_id','canopy_cover','canopy_coverage_centre','canopy_coverage_east','canopy_coverage_north','canopy_coverage_south','canopy_coverage_west')

plots <- getPlotSections( f );

plots$canopy_coverage_mean <- rowMeans(plots[,c('canopy_coverage_centre','canopy_coverage_north','canopy_coverage_east','canopy_coverage_south','canopy_coverage_west')])
plots$canopy_cover_value <- with(plots,
                                ifelse(is.na(canopy_cover),                                       
                                       canopy_coverage_mean * 4.17 ,
                                       canopy_cover
                                )                                
                            );

plots$canopy_cover_class <- with(plots,
                                ifelse(canopy_cover_value < 5, 1,
                                       ifelse(canopy_cover_value < 10, 2,
                                              ifelse(canopy_cover_value < 40, 3, 
                                                     ifelse(canopy_cover_value < 70, 4, 
                                                            5)
                                                     )
                                              )
                                       )
                                 );

data <- plots[ , c('plot_section_id','canopy_cover_class') ];
data <- subset( data, !is.na(canopy_cover_class) );

patchCsv( host, port, updatePlotSectionValues , data );




# ========================================================================================
# Dbh
f <- c('specimen_id' , 'dbh');
trees <- getTrees( f );

trees$dbh_class <- with(trees,
                        ifelse(dbh < 5, 1,        
                               ifelse(dbh < 10, 2,
                                      ifelse(dbh < 20, 3, 
                                             4)
                               )
                        )
                  );

data <- trees[ , c('specimen_id','dbh_class') ];

patchCsv( host, port, updateSpecimenValueUri , data );

