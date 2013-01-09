package org.openforis.calc.server.rest;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

import org.openforis.calc.io.csv.CsvWriter;
import org.openforis.calc.io.flat.FlatDataStream;

/**
 * @author G. Miceli
 */
@Provider
@Produces({"text/plain", "text/csv"})
public class FlatDataStreamProvider implements MessageBodyWriter<FlatDataStream> {
    public boolean isWriteable(Class<?> t, Type gt, Annotation[] as, MediaType mediaType) {
        return FlatDataStream.class.isAssignableFrom(t);
    }

    public long getSize(FlatDataStream result, Class<?> type, Type genericType, 
    		Annotation[] annotations, MediaType mediaType) {
        return -1;
    }

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
}
