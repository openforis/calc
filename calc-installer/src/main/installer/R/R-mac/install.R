setwd( "install_directory/R-linux" );

.libPaths( paste(R.home() , 'library' , sep='/') );

install.packages( "DBI_0.2-7.tar.gz" , repos = NULL );
install.packages( "proto_0.3-10.tar.gz" , repos = NULL );
install.packages( "gsubfn_0.6-5.tar.gz" , repos = NULL );
install.packages( "chron_2.3-45.tar.gz" , repos = NULL );
install.packages( "RSQLite_0.11.4.tar.gz" , repos = NULL );
install.packages( "RSQLite.extfuns_0.0.1.tar.gz" , repos = NULL );
install.packages( "sqldf_0.4-7.1.tar.gz" , repos = NULL );
install.packages( "RPostgreSQL_0.4.tar.gz" , repos = NULL );
install.packages( "rJava_0.9-6.tar.gz" , repos = NULL );
