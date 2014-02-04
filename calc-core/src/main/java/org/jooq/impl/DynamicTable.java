/**
 * 
 */
package org.jooq.impl;

import java.util.ArrayList;
import java.util.List;

import org.jooq.DataType;
import org.jooq.Field;
import org.jooq.Record;

/**
 * @author Mino Togna
 * 
 */
public class DynamicTable<R extends Record> extends TableImpl<R> {

	private static final long serialVersionUID = 1L;

	private List<Field<?>> fields;

	public DynamicTable(String name, String schema) {
		super(name, new SchemaImpl(schema));
		this.fields = new ArrayList<Field<?>>();
	}

	public <T extends Object> Field<T> addField(String name, DataType<T> dataType) {
		Field<T> field = super.createField(name, dataType, this);
		this.fields.add(field);
		return field;
	}

	public List<Field<?>> getFields() {
		return fields;
	}

	@SuppressWarnings("unchecked")
	public Field<Integer> getIntegerField(String name) {
		Field<?> field = field(name);
		if( field == null ){
			field = addField(name, SQLDataType.INTEGER);
		}
		
		return (Field<Integer>) field;
	}

	@SuppressWarnings("unchecked")
	public Field<Long> getLongField(String name) {
		Field<?> field = field(name);
		if( field == null ){
			field = addField(name, SQLDataType.BIGINT);
		}
		
		return (Field<Long>) field;
	}
	
	@SuppressWarnings("unchecked")
	public Field<String> getVarcharField(String name) {
		Field<?> field = field(name);
		if( field == null ){
			field = addField(name, SQLDataType.VARCHAR);
		}
		
		return (Field<String>) field;
	}
	
	public Field<Long> getIdField(){
		return getLongField( "id" );
	}
}
