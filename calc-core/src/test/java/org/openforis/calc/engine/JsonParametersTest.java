package org.openforis.calc.engine;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.junit.Assert;
import org.junit.Test;
import org.openforis.calc.persistence.ParameterHashMap;

public class JsonParametersTest {

	@Test
	public void testIntegerParameter() throws ParseException {
		JSONParser parser = new JSONParser();
		JSONObject jsonObject = (JSONObject) parser.parse("{\"value\": 123}");
		ParameterMap params = new ParameterHashMap(jsonObject);		
		Assert.assertEquals(123, params.getNumber("value").intValue());
	}
	
	@Test
	public void testObjectFloatParameter() throws ParseException {
		JSONParser parser = new JSONParser();
		JSONObject jsonObject = (JSONObject) parser.parse("{\"obj\":{\"x\":3.1415}}");
		ParameterMap params = new ParameterHashMap(jsonObject);
		ParameterMap value = params.getMap("obj");
		Assert.assertEquals(3.1415, value.getNumber("x").doubleValue(), 0);
	}

}
