package org.openforis.calc.persistence;

import org.hibernate.type.AbstractSingleColumnStandardBasicType;
import org.hibernate.type.descriptor.WrapperOptions;
import org.hibernate.type.descriptor.java.AbstractTypeDescriptor;
import org.hibernate.type.descriptor.sql.LongVarcharTypeDescriptor;
import org.openforis.calc.engine.ParameterMap;

public class JsonParameterMapType extends AbstractSingleColumnStandardBasicType<ParameterMap>  {

	private static final long serialVersionUID = 1L;

	public JsonParameterMapType() {
//		super( ClobTypeDescriptor.DEFAULT, StringTypeDescriptor.INSTANCE );
		super( LongVarcharTypeDescriptor.INSTANCE, ParametersTypeDescriptor.INSTANCE );
		
	}

	@Override
	public String getName() {
		return "json_parameters";
	}
	
	private static class ParametersTypeDescriptor extends AbstractTypeDescriptor<ParameterMap> {
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
			// TODO Auto-generated method stub
			return null;
		}
	}
}
