#== 
#== function used by open foris calc to check if the execution of a task returned an error 
#== 
checkError <- function(e, connection=NULL){
  if( inherits(e, "try-error") || inherits(e, "simpleError") ){
    print("CALC-ERROR",quote=F);
    if( !is.null(connection) ){      
      dbDisconnect(connection);
    }
    stop(e);
  }
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
