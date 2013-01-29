CRLF = "\r\n"
LF = "\n"
#data = trees
chunkSize = 1000

as.vector.csv <- function(data, includeHeaders) {
  txtConn = textConnection(NULL, "w", local = T)
  write.table(data, txtConn, append = F, quote = F, sep = ",",
              eol = "\n", na = "NA", dec = ".", row.names = F,
              col.names = includeHeaders, qmethod = c("escape", "double"))
  csv = textConnectionValue(txtConn) 
  close(txtConn)
  
  return(csv)
}

nbytes.csv <- function(lines) {
  data = sum(nchar(lines, type="bytes"))
  lfs = length(lines)
  return (data+lfs)
}

writeHttp <- function(data, conn) {
  writeLines(data, conn, sep = CRLF)
}

writeChunkHeader <- function(size, conn) {
  writeHttp(as.character(as.hexmode(size)), conn)
}

writeChunk<- function(size, lines, conn) {
  writeChunkHeader(size, conn)
  writeLines(lines, conn, sep = LF)
  writeHttp("", conn)
}

writeChunkTrailer <- function(conn) {
  writeHttp(c("0",""), conn)
}

chunk.csv <- function(data, conn, chunkSize) {
  a = 1;
  nrows = nrow(data)
  while ( a <= nrows ) {
    b = min(a + chunkSize-1, nrows)
    lines = as.vector.csv(data[a:b,], a==1)
    len = nbytes.csv(lines)
    writeChunk(len, lines, conn)
    a = a + chunkSize
  }
  writeChunkTrailer(conn)
}

tic <- function(gcFirst = TRUE, type=c("elapsed", "user.self", "sys.self")) {
  type <- match.arg(type)
  assign(".type", type, envir=baseenv())
  if(gcFirst) gc(FALSE)
  tic <- proc.time()[type]         
  assign(".tic", tic, envir=baseenv())
  invisible(tic)
}

toc <- function() {
  type <- get(".type", envir=baseenv())
  toc <- proc.time()[type]
  tic <- get(".tic", envir=baseenv())
  print(toc - tic)
  invisible(toc)
}



patchCsv <- function(host, port, uri, data){
# closeAllConnections()
  
  conn = socketConnection(
      host = host,
      port = port,
      blocking = T,
      open = "a+",
  #    encoding = getOption("encoding"),
      encoding = "utf-8",
      timeout = 30
  )
  
  httpRequest <- sprintf( "PATCH %s HTTP/1.1",uri )
  hostHeader <- sprintf( "Host: %s:%s",host,port )
  #print(httpRequest)
  #print(hostHeader)
  writeHttp(c(
    httpRequest,
    hostHeader,
    "User-Agent: ROpenForisCalc/1.0",
    "Accept: text/csv",
    "Content-Type: text/csv; charset=utf-8",
    "Transfer-Encoding: chunked",""), conn)
  
  tic()
  chunk.csv(data, conn, chunkSize)
  flush(conn)
  toc()
  lines = readLines(con=conn, n=10)
  #print(lines)
  close(conn) 
}

# patchCsv( '127.0.0.1','8080','/calc/rest/surveys/naforma1/test', data)