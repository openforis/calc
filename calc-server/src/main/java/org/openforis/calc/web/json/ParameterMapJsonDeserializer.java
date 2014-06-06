/**
 * 
 */
package org.openforis.calc.web.json;

import java.io.IOException;
import java.util.HashMap;

import org.openforis.calc.engine.ParameterHashMap;
import org.openforis.calc.engine.ParameterMap;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

/**
 * Custom JSON deserializer for instance variables of ParameterMap type
 * 
 * @author Mino Togna
 *
 */
public class ParameterMapJsonDeserializer extends JsonDeserializer<ParameterMap> {

	@SuppressWarnings( { "rawtypes" , "unchecked"} )
	@Override
	public ParameterMap deserialize( JsonParser jp , DeserializationContext ctxt ) throws IOException, JsonProcessingException {
		HashMap map = jp.readValueAs( HashMap.class );
		
		ParameterHashMap parameterMap = new ParameterHashMap( map );
		
		return parameterMap;
	}

}
