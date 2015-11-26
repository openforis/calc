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
  
  fileContent <- readChar( filePath , file.info( filePath )$"size");
  
  fileContentQuoted <- dbQuoteString( conn = connection , x = fileContent );
    
  return ( fileContentQuoted );
};

calc.persistUserScript <- function( filename , table , column , id ){
	fileContent <- calc.getQuotedFileContent( filename );
	
	query <- 'UPDATE calc.';
	query <- paste(query , table , sep = '');
	query <- paste(query , 'SET' , sep = ' ');
	query <- paste(query , column , sep = ' ');
	query <- paste(query , '=' , sep = ' ');
	query <- paste(query , fileContent , sep = ' ');
	query <- paste(query , 'WHERE id =' , sep = ' ');
	query <- paste(query , id , sep = ' ');
	
	#print( query );
	
	dbSendQuery(conn=connection, statement=query);
};

calc.persistCommonScript <- function( filename , id ){
	calc.persistUserScript( filename , 'processing_chain' , 'common_script' , id );
};

calc.persistBaseUnitWeightScript <- function( filename , id ){
	calc.persistUserScript( filename , 'sampling_design' , 'sampling_unit_weight_script' , id );
};

calc.persistEntityPlotAreaScript <- function( filename , id ){
	calc.persistUserScript( filename , 'entity' , 'plot_area_script' , id );
};

calc.persistCalculationStepScript <- function( filename , id ){
	calc.persistUserScript( filename , 'calculation_step' , 'script' , id );
};

calc.persistErrorScript <- function( filename , id ){
	calc.persistUserScript( filename , 'error_settings' , 'script' , id );
};

library("RPostgreSQL");
library("sqldf");
# sqldf options. 
# driver is set to SQLLite in order to read from dataframe , otherwise it uses PostgreSQL which is the default driver loaded by Calc
# https://code.google.com/p/sqldf/#Troubleshooting
options (
  #gsubfn.engine = "R" , 
  sqldf.driver = "SQLite"
);
