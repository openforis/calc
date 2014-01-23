/**
 * 
 */
package org.openforis.calc.schema;

import java.util.ArrayList;
import java.util.List;

import org.jooq.Field;
import org.jooq.Record;
import org.jooq.Result;
import org.jooq.SelectQuery;
import org.jooq.impl.DynamicTable;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.openforis.calc.engine.DataRecord;
import org.openforis.calc.engine.DataRecordVisitor;
import org.openforis.calc.engine.Workspace;
import org.openforis.calc.metadata.Entity;
import org.openforis.calc.persistence.jooq.AbstractJooqDao;
import org.openforis.calc.psql.InformationSchemaColumnsTable;
import org.springframework.stereotype.Repository;

/**
 * @author Mino Togna
 * 
 */
@Repository
public class TableDataDao extends AbstractJooqDao {

	
	public long count(String schema, String table){
		DynamicTable<?> dbTable = new DynamicTable<Record>(table, schema);
		Long count = psql().selectCount().from(dbTable).fetchOne(0, Long.class);
		return count;
	}
	
	@SuppressWarnings("unchecked")
	public JSONArray info(String schema, String table){
		InformationSchemaColumnsTable infoTable = new InformationSchemaColumnsTable();
		
		Result<Record> result = psql()
		.select()
		.from(infoTable)
		.where(
				infoTable.TABLE_SCHEMA.eq(schema)
				.and(infoTable.TABLE_NAME.eq(table))
		).fetch();
		
		JSONArray array = new  JSONArray();
		for (Record record : result) {

			JSONObject o = new JSONObject();
			for (Field<?> field : infoTable.fields()) {
				String fieldName = field.getName();
				o.put(fieldName, record.getValue(fieldName));
			}
			
			array.add(o);
		}
		
		return array;
	}
	
	public List<DataRecord> query(String schema, String table, Integer offset, Integer numberOfRows, boolean excludeNull, String... fields) {
		if (fields == null || fields.length == 0) {
			throw new IllegalArgumentException("fields must be specifed");
		}

		List<DataRecord> records = new ArrayList<DataRecord>();

		// prepare query
		DynamicTable<Record> dbTable = new DynamicTable<Record>(table, schema);
		SelectQuery<Record> select = psql().selectQuery();
		select.addFrom(dbTable);
		for (String field : fields) {
			Field<?> f = dbTable.field(field);
			select.addSelect(f);
			if(excludeNull) {
				select.addConditions(f.isNotNull());
			}
		}
		// add order by id -- important?!
		
		// add offset to query
		if (offset != null && numberOfRows != null) {
			select.addLimit(offset, numberOfRows);
		} else if (offset == null && numberOfRows != null) {
			select.addLimit(0, numberOfRows);
		} else if (offset != null && numberOfRows == null) {
			select.addLimit(offset);
		}
		
		// execute the query
		Result<Record> result = select.fetch();

		// process results
		for (Record record : result) {
//			Long id = record.getValue(table.getIdField().getName(), Long.class);
			DataRecord dataRecord = new DataRecord();
			for (String field : fields) {
				Object value;
				try {
					value = record.getValue(field);
					dataRecord.add(field, value);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			records.add(dataRecord);
		}
		return records;
	}

}
