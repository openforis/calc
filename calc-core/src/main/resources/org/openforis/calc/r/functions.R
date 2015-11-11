#= remove all objects in session
rm( list = ls() ) ;
#== 
#== function used by open foris calc to check if the execution of a task returned an error 
#== 
checkError <- function( e, connection=NULL ){
  if( inherits(e, "try-error") || inherits(e, "simpleError") ){
    print("CALC-ERROR",quote=F);
    if( !is.null(connection) ){
      dbDisconnect(connection);
    }
    stop(e);
  }
};

#===
#=== Extracts the content of a file and returns it as an SQL quoted string  
#===
calc.getQuotedFileContent <- function( filename ){
  filePath <- paste(scriptDir , filename , sep = .Platform$file.sep);
  print( filePath )
  fileContent <- readChar( filePath , file.info( filePath )$"size");
  print( fileContent )
  
  fileContentQuoted <- dbQuoteString( conn = connection , x = fileContent );
  print( fileContentQuoted )
  
  return ( fileContentQuoted );
};

library("RPostgreSQL");
library("sqldf");
# sqldf options. 
# driver is SQLLite in order to read from dataframe , otherwise it uses PostgreSQL which is the default driver used by Calc
# https://code.google.com/p/sqldf/#Troubleshooting
options (
  #gsubfn.engine = "R" , 
  sqldf.driver = "SQLite"
);
