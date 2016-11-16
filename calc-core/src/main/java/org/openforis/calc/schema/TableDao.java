/**
 * 
 */
package org.openforis.calc.schema;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.Result;
import org.jooq.SelectQuery;
import org.jooq.Table;
import org.jooq.impl.DynamicTable;
import org.jooq.util.postgres.information_schema.tables.Columns;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.openforis.calc.engine.DataRecord;
import org.openforis.calc.persistence.jooq.AbstractJooqDao;
import org.springframework.stereotype.Repository;

/**
 * @author Mino Togna
 * 
 */
@Repository
public class TableDao extends AbstractJooqDao {
	
	public DynamicTable<Record> createJooqTable( String schema , String table ){
		DynamicTable<Record> jooqTable = new DynamicTable<Record>( table , schema );
		JSONArray tableInfo = this.info( jooqTable );
		
		jooqTable.initFields(tableInfo);
		
		return jooqTable;
	}
	
	/**
	 * Check if the given table exists in the database
	 * @param schema
	 * @param table
	 * @return
	 */
	public boolean exists(String schema, String table){
		Table<Record> dbTable = new DynamicTable<Record>("tables", "information_schema").as("t");
		Long count = 
				psql()
				.selectCount()
				.from(dbTable)
				.where( "t.table_name = '" + table + "' and t.table_schema = '" + schema + "'" )
				.fetchOne(0, Long.class);
		return count > 0;
	}
	
	public boolean exists( Table<?> table ){
		return exists( table.getSchema().getName() , table.getName() );
	}
	
	public long count(String schema, String table){
		DynamicTable<?> dbTable = new DynamicTable<Record>(table, schema);
		Long count = psql().selectCount().from(dbTable).fetchOne(0, Long.class);
		return count;
	}
	
	public JSONArray info( Table<?> table ){
		return info( table.getSchema().getName() , table.getName() );
	}
	
	// to test
	public boolean hasColumn(  Table<?> table , Field<?> field ){
		JSONArray info = info( table );
		for ( Object object : info ){
			JSONObject o = (JSONObject) object;
			Object columnName = o.get( Columns.COLUMNS.COLUMN_NAME.getName() );
			if( StringUtils.equalsIgnoreCase( columnName.toString() , field.getName() ) ){
				return true;
			}
		}
		return false;
	}
	
	@SuppressWarnings("unchecked")
	public JSONArray info(String schema, String table){
		Columns T = Columns.COLUMNS;
		
		Result<Record> result = psql()
		.select()
			.from( T )
			.where( T.TABLE_SCHEMA.eq(schema)
			.and( T.TABLE_NAME.eq(table) ) )
			.orderBy( T.COLUMN_NAME )
			.fetch();
		
		JSONArray array = new  JSONArray();
		for (Record record : result) {

			JSONObject o = new JSONObject();
			for ( Field<?> field : T.fields() ) {
				String fieldName = field.getName();
				o.put(fieldName, record.getValue(fieldName));
			}
			
			array.add(o);
		}
		
		return array;
	}
	
	public List<DataRecord> selectAll( Table<?> table ) {
		Set<String> fields = new HashSet<String>();
		for ( Field<?> field : table.fields() ) {
			fields.add( field.getName() );
		}
		
		return selectAll( table, fields ); 
	}
	
	public List<DataRecord> selectAll( Table<?> table , Collection<String> fields ) {
		return query( table.getSchema().getName() , table.getName() , null , null  , null , fields.toArray(new String[]{}) );
	}
	
	public List<DataRecord> query(String schema, String table, Integer offset, Integer numberOfRows, Boolean excludeNull, String... fields) {
		if (fields == null || fields.length == 0) {
			throw new IllegalArgumentException("fields must be specifed");
		}

		List<DataRecord> records = new ArrayList<DataRecord>();

		// prepare query
		DynamicTable<Record> dbTable = new DynamicTable<Record>(table, schema);
		SelectQuery<Record> select = psql().selectQuery();
		select.addFrom(dbTable);
		if( fields != null && fields.length >0 ) {
			for (String field : fields) {
				Field<?> f = dbTable.getObjectField(field);
				select.addSelect(f);
				if( excludeNull != null && excludeNull ) {
					select.addConditions(f.isNotNull());
				}
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
