#function used by open foris calc to check if a task 
checkError <- function(e){
  if( inherits(e, "try-error") || inherits(e, "simpleError") ){
    print("CALC-ERROR",quote=F);
    stop(e);
  }
}