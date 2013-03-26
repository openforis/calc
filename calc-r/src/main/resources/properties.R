library('RCurl');
# patchCsv( '127.0.0.1','8080','/calc/rest/surveys/naforma1/test', data)

host <- '127.0.0.1';
port <- '8080';
# 'http://localhost:8080/calc/rest'
calcRestUri <- sprintf('http://%s:%s/calc/rest', host, port);
saveAreaResultsUri <- paste(calcRestUri, '/surveys/naforma1/area-results', sep='/');
updateSpecimenValueUri <- '/calc/rest/surveys/naforma1/units/tree/specimens';
updateDeadWoodValueUri <- '/calc/rest/surveys/naforma1/units/dead_wood/specimens';
updateSpecimenInclusionAreaUri <- '/calc/rest/surveys/naforma1/units/tree/specimens/inclusion-area';
updateDeadWoodInclusionAreaUri <- "/calc/rest/surveys/naforma1/units/dead_wood/specimens/inclusion-area";
updatePlotSectionAreaUri <- '/calc/rest/surveys/naforma1/units/plot/observations/area';
#updateSpecimenValueUri <-  sprintf('%s%s', calcRestUri, updateSpecimenValueUri );           

source('src/main/resources/sockets.R');
source('src/main/resources/functions.R');

#patch <- function(uri, data) {
  
  #closeAllConnections();
  #conn = textConnection(NULL, "w");
  #write.csv(data, conn, row.names=F, quote=F);
  #lines = textConnectionValue(conn);
  
  # Concat array of lines together in one string (!!!)
  #body = paste(lines, collapse='\n');
  
  # HTTP PATCH allows for partial update of a resource
  #curlPerform(url           = uri,          
   #           httpheader    =  c(Accept="text/csv", 'Content-Type' = "text/csv; charset=utf-8"),
    #          customrequest = "PATCH",            
     #         postfields    = body
  #);
  
  # Close the connection when done!
  #close(conn);
#}