package org.jooq.impl;

import org.jooq.DataType;
import org.jooq.Record;
import org.jooq.TableField;
import org.jooq.UniqueKey;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.openforis.calc.persistence.jooq.CalcSchema;
import org.openforis.calc.psql.Psql;
import org.openforis.calc.schema.KeyFactory;

/**
 * 
 * @author Mino Togna
 * 
 */
//@SuppressWarnings("rawtypes")
public class CsvFileTable extends DynamicTable<Record> {

	private static final long serialVersionUID = 1L;

	public final TableField<Record, Long> ID = createField("id", Psql.SERIAL, this);

	private UniqueKey<Record> primaryKey;

	public CsvFileTable(String name, JSONArray columnOptions) {
		this(name, columnOptions, CalcSchema.CALC.getName() );
	}

	@SuppressWarnings("unchecked")
	public CsvFileTable(String name, JSONArray columnOptions, String schema) {
		super(name, schema);
		createFields(columnOptions);

		primaryKey = KeyFactory.newUniqueKey(this, ID);
	}

	@Override
	public UniqueKey<Record> getPrimaryKey() {
		return primaryKey;
	}

	private void createFields(JSONArray columnOptions) {
		for (Object object : columnOptions) {
			JSONObject settings = (JSONObject) object;
			Boolean importCol = (Boolean) settings.get("import");
			if (importCol) {
				String column = (String) settings.get("column");
				String type = (String) settings.get("dataType");

				DataType<?> dataType = getDataType(type);
				
				addField(column, dataType);
			}
		}
	}

	private DataType<?> getDataType(String type) {
		if ("Integer".equalsIgnoreCase(type)) {
			return SQLDataType.INTEGER;
		} else if ("Real".equalsIgnoreCase(type)) {
			return Psql.DOUBLE_PRECISION;
		} else {
			return SQLDataType.VARCHAR;
		}
	}

}
