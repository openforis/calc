setwd( "install_directory\R-windows" );

.libPaths( paste(R.home() , 'library' , sep='/') );

install.packages( "DBI_0.2-7.zip" , repos = NULL );
install.packages( "proto_0.3-10.zip" , repos = NULL );
install.packages( "gsubfn_0.6-5.zip" , repos = NULL );
install.packages( "chron_2.3-45.zip" , repos = NULL );
install.packages( "RSQLite_0.11.4.zip" , repos = NULL );
install.packages( "RSQLite.extfuns_0.0.1.zip" , repos = NULL );
install.packages( "sqldf_0.4-7.1.zip" , repos = NULL );
install.packages( "RPostgreSQL_0.4.zip" , repos = NULL );
install.packages( "rJava_0.9-6.zip" , repos = NULL );
