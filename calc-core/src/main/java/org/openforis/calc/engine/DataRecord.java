/**
 * 
 */
package org.openforis.calc.engine;

import java.util.HashMap;
import java.util.Map;

/**
 * This class is a container for the data entity table
 * 
 * @author M. Togna
 * 
 */
public class DataRecord {

	private long id;
	private Map<String, Object> fields;

	public DataRecord() {
		fields = new HashMap<String, Object>();
	}

	public DataRecord(long id) {
		this();
		this.id = id;
	}

	public Map<String, Object> getFields() {
		return fields;
	}

	public Object getFieldValue(String field) {
		return fields.get(field);
	}

	public void addField(String field, Object value) {
		fields.put(field, value);
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

}
