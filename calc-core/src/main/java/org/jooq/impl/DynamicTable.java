/**
 * 
 */
package org.jooq.impl;

import java.util.ArrayList;
import java.util.List;

import org.jooq.DataType;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.Schema;

/**
 * @author Mino Togna
 * 
 */
public class DynamicTable<R extends Record> extends TableImpl<R> {

	private static final long serialVersionUID = 1L;

	private List<Field<?>> fields;

	public DynamicTable(String name, Schema schema) {
		super(name, schema);
		this.fields = new ArrayList<Field<?>>();
	}

	public void addField(String name, DataType<?> dataType) {
		Field<?> field = super.createField(name, dataType, this);
		this.fields.add(field);
	}

	public List<Field<?>> getFields() {
		return fields;
	}


}
