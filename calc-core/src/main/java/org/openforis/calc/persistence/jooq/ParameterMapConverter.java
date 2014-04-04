/**
 * 
 */
package org.openforis.calc.persistence.jooq;

import org.jooq.Converter;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.openforis.calc.engine.ParameterHashMap;
import org.openforis.calc.engine.ParameterMap;

/**
 * @author Mino Togna
 * @author S. Ricci
 */
public class ParameterMapConverter implements Converter<String, ParameterMap> {

	private static final long serialVersionUID = 1L;

	public ParameterMapConverter() {
	}

	@Override
	public ParameterMap from(String databaseObject) {
		return parseJson(databaseObject);
	}

	@Override
	public String to(ParameterMap userObject) {
		return userObject.toJsonString();
	}

	@Override
	public Class<String> fromType() {
		return String.class;
	}

	@Override
	public Class<ParameterMap> toType() {
		return ParameterMap.class;
	}

	@SuppressWarnings("unchecked")
	private ParameterMap parseJson(String str) {
		try {
			JSONParser parser = new JSONParser();
			Object obj = parser.parse(str);
			if (obj instanceof JSONObject) {
				return new ParameterHashMap((JSONObject) obj);
			} else {
				throw new IllegalArgumentException("Invalid JSON in database: " + str);
			}
		} catch (ParseException e) {
			throw new IllegalArgumentException("Invalid JSON in database: " + str, e);
		}
	}

}
