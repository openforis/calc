calcRestUri <- 'http://localhost:8080/calc/rest';
saveAreaResultsUri <- paste(calcRestUri, 'surveys/naforma1/area-results', sep='/');
library('RCurl');

upload <- function(uri, data){
  closeAllConnections();
  
  conn = textConnection(NULL, "w");
  write.csv(data, conn, row.names=F);
  body = textConnectionValue(conn);
    
  postForm(uri, style="POST",
           "fileData" = body,
           .opts = list(verbose = TRUE, header = TRUE));
  
  close(conn);
}