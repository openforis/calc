package org.openforis.calc.server.rest.jooq;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

import org.jooq.Record;
import org.jooq.Result;

/**
 * @author G. Miceli
 */
// TODO Wrap Result and Records
@Provider
@Produces({"text/plain", "text/csv"})
public class JooqResultCsvProvider implements MessageBodyWriter<Result<? extends Record>> {
    public boolean isWriteable(Class<?> t, Type gt, Annotation[] as, MediaType mediaType) {
        return Result.class.isAssignableFrom(t);
    }

    public long getSize(Result<? extends Record> result, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return -1;
    }

    public void writeTo(Result<? extends Record> result, Class<?> t, Type gt, Annotation[] as,
            MediaType mediaType, MultivaluedMap<String, Object> httpHeaders, 
            OutputStream out) throws IOException {
    	// TODO implement using own CSV writer, map to correct column names, etc.
    	if ( result.isEmpty() ) {
    		throw new WebApplicationException(404);
    	} else {
    		out.write(result.formatCSV().getBytes());
    	}
    }
}
