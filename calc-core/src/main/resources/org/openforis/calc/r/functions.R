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