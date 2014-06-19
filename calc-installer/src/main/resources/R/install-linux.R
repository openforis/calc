setwd( "./" );

.libPaths( paste(R.home() , 'library' , sep='/') );

install.packages( "packages/linux/DBI_0.2-7.tar.gz" , repos = NULL );
install.packages( "packages/linux/proto_0.3-10.tar.gz" , repos = NULL );
install.packages( "packages/linux/gsubfn_0.6-5.tar.gz" , repos = NULL );
install.packages( "packages/linux/chron_2.3-45.tar.gz" , repos = NULL );
install.packages( "packages/linux/RSQLite_0.11.4.tar.gz" , repos = NULL );
install.packages( "packages/linux/RSQLite.extfuns_0.0.1.tar.gz" , repos = NULL );
install.packages( "packages/linux/sqldf_0.4-7.1.tar.gz" , repos = NULL );
install.packages( "packages/linux/RPostgreSQL_0.4.tar.gz" , repos = NULL );
install.packages( "packages/linux/rJava_0.9-6.tar.gz" , repos = NULL );
