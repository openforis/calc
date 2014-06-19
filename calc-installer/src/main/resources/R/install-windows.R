setwd( "c:/dev/" );

.libPaths( paste(R.home() , 'library' , sep='/') );

install.packages( "packages/windows/DBI_0.2-7.zip" , repos = NULL );
install.packages( "packages/windows/proto_0.3-10.zip" , repos = NULL );
install.packages( "packages/windows/gsubfn_0.6-5.zip" , repos = NULL );
install.packages( "packages/windows/chron_2.3-45.zip" , repos = NULL );
install.packages( "packages/windows/RSQLite_0.11.4.zip" , repos = NULL );
install.packages( "packages/windows/RSQLite.extfuns_0.0.1.zip" , repos = NULL );
install.packages( "packages/windows/sqldf_0.4-7.1.zip" , repos = NULL );
install.packages( "packages/windows/RPostgreSQL_0.4.zip" , repos = NULL );
install.packages( "packages/windows/rJava_0.9-6.zip" , repos = NULL );
