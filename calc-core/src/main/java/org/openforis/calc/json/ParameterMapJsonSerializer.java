/**
 * 
 */
package org.openforis.calc.json;

import java.io.IOException;

import org.openforis.calc.engine.ParameterMap;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

/**
 * Custom JSON serializer for {@link ParameterMap} class
 * 
 * @author M. Togna
 * 
 */
public class ParameterMapJsonSerializer extends JsonSerializer<ParameterMap> {

	@Override
	public void serialize(ParameterMap value, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonProcessingException {
		jgen.writeStartObject();

		for ( String paramName : value.names() ) {
			Object paramValue = value.get(paramName);

			jgen.writeObjectField(paramName, paramValue);
		}

		jgen.writeEndObject();
	}

}
