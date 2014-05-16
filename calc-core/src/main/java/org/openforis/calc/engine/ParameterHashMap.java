package org.openforis.calc.engine;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jooq.tools.json.JSONArray;
import org.json.simple.JSONObject;

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
	 * Wraps the given Map
	 * 
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
	public void remove(String name) {
		map.remove(name);
	}
	
	@Override
	public ParameterMap getMap(String name) {
		Object value = map.get(name);
		return toParameterMap(value);
	}

	@SuppressWarnings("unchecked")
	private ParameterMap toParameterMap(Object value) {
		if ( value == null ) {
			return null;
		} else if ( value instanceof ParameterMap ) {
			return (ParameterMap) value;
		} else if ( value instanceof Map ) {
			return new ParameterHashMap((Map<String, Object>) value);
		} else {
			throw new IllegalStateException("Unknown value "+value.getClass());
		}
	}
	
	@Override
	public List<ParameterMap> getList(String name) {
		Object value = map.get(name);
		if( value == null ){
			return null;
		} else if( value instanceof List ){
			
			List<ParameterMap> list = new ArrayList<ParameterMap>();
			for (Object object : (List<?>)value) {
				ParameterMap parameterMap = toParameterMap(object);
				list.add(parameterMap);
			}
			return list;
			
		} else {
			throw new IllegalStateException("Unknown value "+value.getClass());			
		}
	}

	@Override
	public void setList(String name, List<ParameterMap> value) {
		map.put( name,  new JSONArray(value) );
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public Collection<? extends Object> getArray(String name) {
		Object value = map.get(name);
		
		if( value == null ) {
			return null;
		} else if( value instanceof List ) {
			return (Collection<? extends Object>) value;
		} else {
			throw new IllegalStateException( "Unknown value " + value.getClass() );			
		}
	}
	
	@Override
	public void setArray( String name, Collection<? extends  Object> values ) {
		map.put( name, new ArrayList<Object>(values) );
	}
	
	/**
	 * WARNING: Does not defensively copy the map; 
	 * Changes to this map value pass through to the 
	 * original map and vice-versa
	 * @param map
	 */
	@Override
	public void setMap(String name, ParameterMap value) {
		map.put(name, ((ParameterHashMap) value).map);
	}

	@Override
	public Number getNumber(String name) {
		Object value = map.get(name);
		if ( value == null ) {
			return null;
		} else if ( value instanceof Number ) {
			return (Number) value;
		} else if ( value instanceof String ) {
			double result = Double.parseDouble((String) value);
			return result;
		} else {
			throw new IllegalStateException("Exptected Number, found: " + value.getClass().getName());
		}
	}

	@Override
	public void setNumber(String name, Number value) {
		map.put(name, value);
	}

	@Override
	public Integer getInteger(String name) {
		Object value = map.get(name);
		if ( value == null ) {
			return null;
		} else if ( value instanceof Number ) {
			return ( (Number) value ).intValue();
		} else if ( value instanceof String ) {
			int result = Integer.parseInt( (String) value );
			return result;
		} else {
			throw new IllegalStateException("Exptected Integer, found: " + value.getClass().getName());
		}
	}
	
	@Override
	public void setInteger(String name, Integer value) {
		setNumber(name, value);
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
		Object value = map.get(name);
		if ( value == null ) {
			return null;
		} else if ( value instanceof Boolean ) {
			return (Boolean) value;
		} else if ( value instanceof String ) {
			boolean result = Boolean.parseBoolean((String) value);
			return result;
		} else {
			throw new IllegalStateException("Exptected Boolean, found: " + value.getClass().getName());
		}
	}

	@Override
	public void setBoolean(String name, Boolean value) {
		map.put(name, value);
	}
	
	@Override
	public String toString() {
//		return map.toString();
		return toJsonString();
	}

	@Override
	public String toJsonString() {
		return JSONObject.toJSONString(map);
	}
	
	@Override
	public Object get(String name) {
		return map.get(name);
	}
	
	/**
	 * Creates a deep copy of the map and all contained maps
	 */
	@Override
	public ParameterMap deepCopy() {
		Map<String, Object> newMap = new HashMap<String, Object>(map.size());
		for (Map.Entry<String, Object> e : map.entrySet()) {
			String name = e.getKey();
			Object value = e.getValue();
			if ( value instanceof ParameterHashMap ) {
				value = ((ParameterHashMap) value).deepCopy();
			}
			newMap.put(name, value);
		}
		return new ParameterHashMap(newMap);
	}
}
