#== 
#== function used by open foris calc to check if the execution of a task returned an error 
#== 
checkError <- function(e, connection){
  if( inherits(e, "try-error") || inherits(e, "simpleError") ){
    print("CALC-ERROR",quote=F);
    dbDisconnect(connection);
    stop(e);
  }
};