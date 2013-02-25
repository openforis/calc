getTrees <- function(fileName) {
  trees <- read.csv(fileName);
  
  trees$cluster_plot_subplot[trees$cluster_plot_subplot == ''] <- 'A';
  
  trees$total_height <- trees$total_height_value;
  trees$dbh <- trees$dbh_value;
  trees$species_code <- toupper(trees$species_code);
  
  #Remove trees with no dbh
  trees <- subset( trees, !is.na(trees$dbh) );
  
  #Remove baobab from trees (for growing stock volume estimation)
  #TODO in calc
  # trees <- subset( trees, species_code != 'ADA/DIG' );
  
  return (trees);
}

#setwd('/home/gino/workspace/minotz/')
obsp = read.csv('~/tzdata/src/plot.csv');
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
              land_use, 
              vegetation_type, 
              ownership_type, 
              accessibility,
              canopy_cover  = canopy_cover_value, 
              canopy_coverage_centre, 
              canopy_coverage_north,
              canopy_coverage_east, 
              canopy_coverage_south, 
              canopy_coverage_west,
              slope       = slope_value
              )
           )

# Fix known problems
sp[sp$cluster_code=='152_63'  & sp$plot_no==3,]$plot_no[1] = 8
sp[is.na(sp$percent_share),]$percent_share <- 100

#Default slope is 0
sp[is.na(sp$slope), ]$slope <- 0
# Set slope limit to 80 %  
sp$slope  <- with( sp,
                      ifelse( slope > 80, 80, slope )
                  );

#default plot section = A
sp[is.na(sp$plot_section) | sp$plot_section=='',]$plot_section <- 'A'
stopifnot( length(unique(sp$plot_section)) == 2)
write.csv(sp, '~/tzdata/plots.csv')

trees  = getTrees('~/tzdata/src/trees.csv')

tr = with(
      trees, 
        data.frame(
          cluster_code  = cluster_id, 
          visit_type    = cluster_measurement, 
          plot_no       = cluster_plot_no, 
          plot_section  = cluster_plot_subplot,
#          specimen_no   = tree_no * 100 + ifelse(is.na(stem_no), 1, stem_no),
#          survey_date   = paste(time_study_date_year, time_study_date_month, time_study_date_day, sep="-"),
          specimen_code = paste(tree_no, stem_no, sep="/"),
          dbh           = dbh_value,
          health, origin,
          stump_diameter = stump_diameter_value,
          stump_height  = stump_height_value,
          total_height  = total_height_value,
          bole_height   = bole_height_value,
          taxon_code    = species_code
        )
      );

tr$specimen_no = 1:nrow(tr)
write.csv(tr, '~/tzdata/trees.csv', row.names = F)

hh = read.csv('~/tzdata/src/household.csv')

hh = hh[!duplicated(hh[,c('cluster_id','id')]),]

hh$interview_date =  with(hh, paste(task_fieldInterview_date_year, task_fieldInterview_date_month, task_fieldInterview_date_day, sep="-"))

write.csv(hh, '~/tzdata/household.csv', row.names = F)
