package org.openforis.calc.persistence;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.openforis.calc.engine.ParameterMap;

/**
 * 
 * @author G. Miceli
 * @author M. Togna
 */
public class ParameterHashMap implements ParameterMap {
	private Map<String, Object> map;
	
	public ParameterHashMap() {
		this.map = new HashMap<String, Object>();
	}

	/**
	 * WARNING: Does not defensively copy the map; 
	 * Changes to ParameterHashMap pass through to the 
	 * original map and vice-versa
	 * 
	 * @param map
	 */
	public ParameterHashMap(Map<String, Object> map) {
		this.map = map;
	}

	@Override
	public Set<String> names() {
		return Collections.unmodifiableSet( map.keySet() );
	}

	@Override
	public void removeValue(String name) {
		map.remove(name);
	}

	@SuppressWarnings("unchecked")
	@Override
	public ParameterMap getMap(String name) {
		Object value = map.get(name);
		if ( value == null ) {
			return null;
		} else if ( value instanceof Map ) {
			return new ParameterHashMap((Map<String, Object>) value);
		} else if ( value instanceof ParameterMap ){
			return (ParameterMap) value;
		} else {
			throw new IllegalStateException("Unknown value "+value.getClass());
		}
	}

	/**
	 * WARNING: Does not defensively copy the map; 
	 * Changes to this map value pass through to the 
	 * original map and vice-versa
	 * @param map
	 */
	@Override
	public void setMap(String name, ParameterMap value) {
		map.put(name, value);
	}

	@Override
	public Number getNumber(String name) {
		return (Number) map.get(name);
	}

	@Override
	public void setNumber(String name, Number value) {
		map.put(name, value);
	}

	@Override
	public String getString(String name) {
		return (String) map.get(name);
	}

	@Override
	public void setString(String name, String value) {
		map.put(name, value);
	}

	@Override
	public Boolean getBoolean(String name) {
		return (Boolean) map.get(name);
	}

	@Override
	public void setBoolean(String name, Boolean value) {
		map.put(name, value);
	}
	
	@Override
	public String toString() {
		return map.toString();
	}
}
