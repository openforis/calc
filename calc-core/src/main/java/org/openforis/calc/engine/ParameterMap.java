package org.openforis.calc.engine;

import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * 
 * 
 * @author G. Miceli
 * @author M. Togna
 */
public interface ParameterMap {

	/** Unmodifiable */
	Set<String> names();

	Number getNumber(String name);
	void setNumber(String name, Number value);
	
	Integer getInteger(String name);
	void setInteger(String name, Integer value);
	
	String getString(String name);
	void setString(String name, String value);

	Boolean getBoolean(String name);
	void setBoolean(String name, Boolean value);

	ParameterMap getMap(String name);
	void setMap(String name, ParameterMap value);

	List<ParameterMap> getList(String name);
	void setList(String name, List<ParameterMap> value);
	
	Collection<? extends Object> getArray( String name );
	void setArray( String name , Collection<? extends Object> values );
	
	void remove(String name);
	
	String toJsonString();
	
	ParameterMap deepCopy();
	
	Object get(String name);

	Set<String> keys();
}