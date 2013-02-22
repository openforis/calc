#data = data.frame(a=c(1,2,3), b=c(2,3,4))
closeAllConnections()
conn = socketConnection(
    host = "127.0.0.1",
    port = 8080,
    blocking = T,
    open = "a+",
    encoding = "utf-8",
    timeout = 5
)
header = c(
  "PATCH /calc/rest/surveys/naforma1/test HTTP/1.1",
  "Host: 127.0.0.1:8080",
  "User-Agent: ROpenForisCalc/1.0",
  "Accept: text/csv",
  "Content-Type: text/csv; charset=utf-8",
  "Content-Length: 999999999999",
  ""
)
writeLines(header, con = conn)
write.csv(data, file = conn, row.names = F)
#write.csv(data[1:2,], file = conn, row.names = F)
#print("\U001A")

writeChar("\n", con=conn)
#writeChar("A\n", con=conn )

#writeBin(65,con=conn)
#writeBin(10,con=conn)
#writeLines("A", con = conn)


#write.csv("\\U001A", file = conn, row.names = F)
flush(conn)
lines = readLines(con=conn, n=-1)
print(lines)
close(conn)