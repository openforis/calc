getTrees <- function(fileName) {
  trees <- read.csv(fileName);
  
  trees$cluster_plot_subplot[trees$cluster_plot_subplot == ''] <- 'A';
  
  trees$total_height <- trees$total_height_value;
  trees$dbh <- trees$dbh_value;
  trees$species_code <- toupper(trees$species_code);
  
  #Remove trees with no dbh
  trees <- subset( trees, !is.na(trees$dbh) );
  
  #Remove baobab from trees (for growing stock volume estimation)
  trees <- subset( trees, species_code != 'ADA/DIG' );
  
  return (trees);
}

#setwd('/home/gino/workspace/minotz/')
obsp = read.csv('data/plot.csv')
# collect can flatten and rename columns, exporting to this format
# column names specified in calc:column and calc:table
# each plot row includes key (rowno or id) of each ancestor

sp = with( obsp, 
            data.frame(
              cluster_code  = cluster_id, 
              visit_type    = cluster_measurement, 
              plot_no       = no, 
              plot_section  = subplot, 
              survey_date   = paste(time_study_date_year, time_study_date_month, time_study_date_day, sep="-"),
              percent_share = share_value,
              gps_reading_x = location_x,
              gps_reading_y = location_y,
              gps_reading_srs_id = location_srs_id,
              center_direction   = centre_dir_value,
              center_distance    = centre_dist,
              accessible    = (accessibility == 0),
              step          = 3,
              land_use, vegetation_type, ownership_type, accessibility,
              canopy_cover  = canopy_cover_value, 
              canopy_coverage_centre, 
              canopy_coverage_north,
              canopy_coverage_east, 
              canopy_coverage_south, 
              canopy_coverage_west
              ) 
           )

# Fix known problems
sp[sp$cluster_code=='152_63'  & sp$plot_no==3,]$plot_no[1] = 8

write.csv(sp, '~/tzdata/plots.csv')

trees  = getTrees('data/trees.csv')

tr = with(
      trees, 
        data.frame(
          cluster_code  = cluster_id, 
          visit_type    = cluster_measurement, 
          plot_no       = cluster_plot_no, 
          plot_section  = cluster_plot_subplot, 
          survey_date   = paste(time_study_date_year, time_study_date_month, time_study_date_day, sep="-"),
          specimen_code = paste(tree_no, stem_no, sep="/"),
          dbh           = dbh_value,
          health, origin,
          stump_diameter = stump_diameter_value,
          stump_height  = stump_height_value,
          total_height  = total_height_value,
          bole_height   = bole_height_value
        )
      )

write.csv(tr, '~/tzdata/trees.csv')
