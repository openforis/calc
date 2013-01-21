calcRestUri <- 'http://localhost:8080/calc/rest';
saveAreaResultsUri <- paste(calcRestUri, 'surveys/naforma1/area-results', sep='/');
library('RCurl');

upload <- function(uri, data){
  # closeAllConnections();
  
  #conn = textConnection(NULL, "w");
  #write.csv(data, conn, row.names=F);
  #body = textConnectionValue(conn);
    
  #postForm(uri, style="POST",
  #        "fileData" = body,
  #         .opts = list(verbose = TRUE, header = TRUE));
  
  #close(conn);
  
  closeAllConnections();
  conn = textConnection(NULL, "w");
  write.csv(data, conn, row.names=F, quote=F);
  lines = textConnectionValue(conn);
  
  # Concat array of lines together in one string (!!!)
  body = paste(lines, collapse='\n');
  
  # HTTP PATCH allows for partial update of a resource
  curlPerform(url           = uri,          
              httpheader    =  c(Accept="text/csv", 'Content-Type' = "text/csv; charset=utf-8"),
              customrequest = "PATCH",            
              postfields    = body
  );
  
  # Close the connection when done!
  close(conn);
}