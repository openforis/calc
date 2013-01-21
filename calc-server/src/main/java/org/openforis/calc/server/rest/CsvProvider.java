package org.openforis.calc.server.rest;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;

import org.openforis.calc.io.csv.CsvReader;
import org.openforis.calc.io.csv.CsvWriter;
import org.openforis.calc.io.flat.FlatDataStream;

import com.sun.jersey.core.provider.AbstractMessageReaderWriterProvider;

/**
 * @author G. Miceli
 */
@Provider
@Produces("text/csv")
@Consumes("text/csv")
public class CsvProvider extends AbstractMessageReaderWriterProvider<FlatDataStream> {
	@Override
    public boolean isWriteable(Class<?> t, Type gt, Annotation[] as, MediaType mediaType) {
        return FlatDataStream.class.isAssignableFrom(t);
    }

	@Override
    public long getSize(FlatDataStream result, Class<?> type, Type genericType, 
    		Annotation[] annotations, MediaType mediaType) {
        return -1;
    }

	@Override
    public void writeTo(FlatDataStream in, Class<?> t, Type gt, Annotation[] as,
            MediaType mediaType, MultivaluedMap<String, Object> httpHeaders, 
            OutputStream out) throws IOException {
    	
    	CsvWriter csvWriter = new CsvWriter(out);
    	
		csvWriter.writeAll(in);
    	
		if ( csvWriter.getLinesWritten() == 0 ) {
			throw new WebApplicationException(Response.Status.NOT_FOUND);
		}
		
		csvWriter.close();
    }

	@Override
	public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
		return FlatDataStream.class.isAssignableFrom(type);
	}

	@Override
	public FlatDataStream readFrom(Class<FlatDataStream> type, Type genericType, Annotation[] annotations, 
			MediaType mediaType, MultivaluedMap<String, String> httpHeaders, InputStream in)
			throws IOException, WebApplicationException {
		CsvReader csvReader = new CsvReader(new InputStreamReader(in));
		csvReader.readHeaders();
		return csvReader;
	}
}
