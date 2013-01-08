package org.openforis.calc.server.rest.jooq;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.List;

import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

import org.openforis.calc.io.FlatDataStream;
import org.openforis.calc.io.flat.Record;

import au.com.bytecode.opencsv.CSVWriter;

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
    	Record r = in.nextRecord();
    	if ( r== null ) {
    		throw new WebApplicationException(404);
    	}    		

    	List<String> fieldNames = in.getFieldNames();
    	Writer wr = new BufferedWriter(new OutputStreamWriter(out));
    	CSVWriter csvOut = new CSVWriter(wr);
    	String[] headers = fieldNames.toArray(new String[fieldNames.size()]);
		csvOut.writeNext(headers);
		
		while ( r != null ) {
			String[] line = r.toStringArray();
			csvOut.writeNext(line);
			r = in.nextRecord();
		}
		csvOut.close();
    }
}
