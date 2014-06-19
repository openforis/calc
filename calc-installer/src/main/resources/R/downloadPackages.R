getPackages <- function(packs){
  packages <- unlist(
    tools::package_dependencies(packs, available.packages(),
                                which=c("Depends", "Imports"), 
                                recursive=TRUE)
  );
  packages <- union( packs, packages );
  packages
};

downloadPackages <- function( packages ){

  download.packages( packages, destdir="packages/linux" );
  
};

packages <- getPackages( c("RPostgreSQL" , "sqldf" , "rJava") );
downloadPackages(  packages = packages );
