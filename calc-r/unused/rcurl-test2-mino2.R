#data = trees
#data = data.frame(a=c(10,20,30), b=c(2,3,4))
chunkSize = 50


as.vector.csv <- function(data, includeHeaders) {
  txtConn = textConnection(NULL, "w", local = T)
  textConnectionValue(txtConn)
  write.table(data, txtConn, append = F, quote = T, sep = ",",
              eol = "\n", na = "NA", dec = ".", row.names = F,
              col.names = includeHeaders, qmethod = c("escape", "double"))
  #write.csv(data, txtConn, row.names = F, col.names = T)
  csv = textConnectionValue(txtConn) 
  close(txtConn)
  return(csv)
}
nbytes.csv <- function(lines) {
  data = sum(nchar(lines, type="bytes"))
  lfs = length(lines)
  return (data+lfs)
}
chunk.csv <- function(data, conn, chunkSize) {
  a = 1;
  nrows = nrow(data)
  while ( a <= nrows ) {
    b = min(a + chunkSize-1, nrows)
    lines = as.vector.csv(data[a:b,], a==1)
    len = nbytes.csv(lines)
    #print(len)
    writeChar( as.character(as.hexmode(len)), conn )
    writeChar("\r\n", conn)
    writeLines(lines, conn, sep='-')    
    writeChar("\r\n", conn)
    a = a + chunkSize
  }
  writeChar("0\r\n\r\n", conn)
}
closeAllConnections()
conn = socketConnection(
    host = "127.0.0.1",
    port = 8080,
    blocking = T,
    open = "a+",
    encoding = getOption("encoding"),
#    encoding = "utf-8",
    timeout = 5
)
header = c(
  "PATCH /calc/rest/surveys/naforma1/test HTTP/1.1",
  "Host: 127.0.0.1:8080",
  "User-Agent: ROpenForisCalc/1.0",
  "Accept: text/csv",
  "Content-Type: text/csv; charset=utf-8",
  "Transfer-Encoding: chunked",
  ""
)
writeLines(header, con = conn, sep = "\r\n")
chunk.csv(data, conn, chunkSize)
flush(conn)
lines = readLines(con=conn, n=-1)
print(lines)
close(conn)
