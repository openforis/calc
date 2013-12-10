/**
 * 
 */
package org.openforis.calc.engine;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * This class is a container for the data entity table
 * 
 * @author M. Togna
 * 
 */
public class DataRecord implements Cloneable {

	private long id;
	private Map<String, Object> fields;

	public DataRecord() {
		fields = new LinkedHashMap<String, Object>();
	}

	public DataRecord(long id) {
		this();
		this.id = id;
	}

	@Override
	public DataRecord clone() throws CloneNotSupportedException {
		Object result = super.clone();
		DataRecord dataRecord = (DataRecord) result;
		dataRecord.fields = fields == null ? null: new HashMap<String, Object>(fields);
		return dataRecord;
	}
	
	public Map<String, Object> getFields() {
		return fields;
	}

	public Object getValue(String field) {
		return fields.get(field);
	}

	public void add(String field, Object value) {
		fields.put(field, value);
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

}
