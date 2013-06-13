package org.openforis.calc.engine;

import java.util.Set;

/**
 * 
 * @author G. Miceli
 * @author M. Togna
 */
public interface ParameterMap {

	/** Unmodifiable */
	Set<String> names();

	Number getNumber(String name);
	void setNumber(String name, Number value);
	
	String getString(String name);
	void setString(String name, String value);

	Boolean getBoolean(String name);
	void setBoolean(String name, Boolean value);

	ParameterMap getMap(String name);
	void setMap(String name, ParameterMap value);

	void removeValue(String name);
}