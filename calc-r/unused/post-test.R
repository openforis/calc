data = data.frame(A=c(10,20,30,40), B=c(11,21,31,41))

# Write data frame in array of lines
closeAllConnections()
conn = textConnection(NULL, "w")
write.csv(data, conn, row.names=F, quote=F)
lines = textConnectionValue(conn)

# Concat array of lines together in one string (!!!)
body = paste(lines, collapse='\n')

#testUri <- 'http://localhost:8080/calc/rest/surveys/naforma1/area-results';
testUri <- 'http://localhost:8080/calc/rest/surveys/naforma1/units/tree/specimens';

# HTTP PATCH allows for partial update of a resource
curlPerform(url           = testUri,          
            httpheader    =  c(Accept="text/csv", 'Content-Type' = "text/csv; charset=utf-8"),
            customrequest = "PATCH",            
            postfields    = body
)

# Close the connection when done!
close(conn) 