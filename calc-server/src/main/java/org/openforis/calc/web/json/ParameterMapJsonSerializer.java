package org.openforis.calc.web.json;

import java.io.IOException;

import org.openforis.calc.engine.ParameterMap;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

/**
 * Custom Json Serializer for ParameterHashMap class
 * 
 * @author Mino Togna
 * 
 */
public class ParameterMapJsonSerializer extends JsonSerializer<ParameterMap> {

	@Override
	public void serialize(ParameterMap map, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonProcessingException {
		jgen.writeStartObject();
		for (String paramName : map.names()) {
			Object paramValue = map.get(paramName);

			jgen.writeObjectField(paramName, paramValue);
		}
		jgen.writeEndObject();
	}
}