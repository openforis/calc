package org.openforis.calc.common;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * 
 * @author G. Miceli
 * @author M. Togna
 */
public abstract class Reflection {
	
	@SuppressWarnings("unchecked")
	public static <T> T extractGenericType(Class<?> c) {
	   Type t = c.getGenericSuperclass();
       ParameterizedType pt = (ParameterizedType) t;
       return (T) pt.getActualTypeArguments()[0];
	}
}
