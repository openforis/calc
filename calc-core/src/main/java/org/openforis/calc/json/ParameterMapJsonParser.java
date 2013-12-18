package org.openforis.calc.json;

import java.util.Set;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.openforis.calc.engine.ParameterHashMap;
import org.openforis.calc.engine.ParameterMap;

/**
 * 
 * @author S. Ricci
 *
 */
public class ParameterMapJsonParser {

	public ParameterMap parse(String json) {
		try {
			JSONParser jsonParser = new JSONParser();
			JSONObject jsonObj = (JSONObject) jsonParser.parse(json);
			return parse(jsonObj);
		} catch (ParseException e) {
			throw new RuntimeException("Error parsing json into ParameterMap", e);
		}
	}

	private ParameterMap parse(JSONObject jsonObj) throws ParseException {
		ParameterMap result = new ParameterHashMap();
		@SuppressWarnings("unchecked")
		Set<String> keySet = jsonObj.keySet();
		for (String propName : keySet) {
			Object propValue = jsonObj.get(propName);
			if ( propValue instanceof JSONObject ) {
				ParameterMap nestedMap = parse((JSONObject) propValue);
				result.setMap(propName, nestedMap);
			} else if ( propValue instanceof Number ) {
				result.setNumber(propName, (Number) propValue);
			} else if ( propValue instanceof String ) {
				result.setString(propName, (String) propValue);
			} else if ( propValue instanceof Boolean ) {
				result.setBoolean(propName, (Boolean) propValue);
			} else {
				//skip it
			}
		}
		return result;
	}
	
	
}
