package org.openforis.calc.persistence;

import org.hibernate.type.AbstractSingleColumnStandardBasicType;
import org.hibernate.type.descriptor.WrapperOptions;
import org.hibernate.type.descriptor.java.AbstractTypeDescriptor;
import org.hibernate.type.descriptor.sql.LongVarcharTypeDescriptor;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.openforis.calc.engine.ParameterMap;

/**
 * 
 * @author G. Miceli
 * @author M. Togna
 */
public class JsonParameterMapType extends AbstractSingleColumnStandardBasicType<ParameterMap>  {

	private static final long serialVersionUID = 1L;

	public JsonParameterMapType() {
		super( LongVarcharTypeDescriptor.INSTANCE, ParametersTypeDescriptor.INSTANCE );
		
	}

	@Override
	public String getName() {
		return "json_parameters";
	}
	
	private static class ParametersTypeDescriptor extends AbstractTypeDescriptor<ParameterMap> {
		private static final long serialVersionUID = 1L;
		private static final ParametersTypeDescriptor INSTANCE = new ParametersTypeDescriptor();
		
		public ParametersTypeDescriptor() {
			super(ParameterMap.class);
		}

		@Override
		public String toString(ParameterMap value) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public ParameterMap fromString(String string) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public <X> X unwrap(ParameterMap value, Class<X> type, WrapperOptions options) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public <X> ParameterMap wrap(X value, WrapperOptions options) {
			if ( value instanceof String ) {
				return parseJson((String) value);
			} else {
				throw new IllegalArgumentException("value");
			}
		}

		@SuppressWarnings("unchecked")
		private <X> ParameterMap parseJson(String str) {
			try {
				JSONParser parser = new JSONParser();
				Object obj = parser.parse(str);
				if ( obj instanceof JSONObject ) {
					return new ParameterHashMap((JSONObject) obj);
				} else {
					throw new IllegalArgumentException("Invalid JSON in database: "+str);
				}
			} catch (ParseException e) {
				throw new IllegalArgumentException("Invalid JSON in database: "+str, e);
			}
		}
	}
}
