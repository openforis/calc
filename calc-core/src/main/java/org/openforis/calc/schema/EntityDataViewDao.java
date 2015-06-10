/**
 * 
 */
package org.openforis.calc.schema;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.jooq.Condition;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.Result;
import org.jooq.Select;
import org.jooq.SelectQuery;
import org.jooq.impl.DSL;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.openforis.calc.engine.DataRecord;
import org.openforis.calc.engine.DataRecordVisitor;
import org.openforis.calc.engine.ParameterHashMap;
import org.openforis.calc.engine.ParameterMap;
import org.openforis.calc.engine.Workspace;
import org.openforis.calc.metadata.Entity;
import org.openforis.calc.metadata.MultiwayVariable;
import org.openforis.calc.metadata.QuantitativeVariable;
import org.openforis.calc.metadata.TextVariable;
import org.openforis.calc.metadata.Variable;
import org.openforis.calc.persistence.jooq.AbstractJooqDao;
import org.springframework.stereotype.Repository;

/**
 * @author Mino Togna
 * 
 */
@Repository
public class EntityDataViewDao extends AbstractJooqDao {
	
	public void createOrUpdateView( Entity entity, boolean joinWithResults ){
		Workspace ws = entity.getWorkspace();

		Schemas schemas = new Schemas(ws);
		DataSchema inputSchema = schemas.getDataSchema();
		EntityDataView view = inputSchema.getDataView(entity);
		
		// drop view
		drop(view);

		// create view
		create( view , joinWithResults );
	}
	
	public void createOrUpdateView( Entity entity ) {
		createOrUpdateView( entity , true );
	}

	public void create( EntityDataView view , boolean joinWithResults ) {
		Select<?> select = view.getSelect( joinWithResults );
		psql().createView(view).as(select).execute();
	}

	public void drop( EntityDataView view ) {
		psql().dropViewIfExists(view).execute();
	}

	public void drop( Entity entity ) {
		Workspace ws = entity.getWorkspace();

		Schemas schemas = new Schemas(ws);
		DataSchema inputSchema = schemas.getDataSchema();
		EntityDataView view = inputSchema.getDataView(entity);

		drop( view );
	}
	
	public long count( Entity entity , JSONArray filters ) {
		EntityDataView view = getDataView(entity.getWorkspace(), entity);
		
		SelectQuery<Record> select = psql().selectQuery();
		select.addSelect( DSL.count() );
		select.addFrom( view );

		// add filters to query
		if( filters != null ) { 
			addQueryFilters(select, view, filters);
		}
		
		Long count = select.fetchOne(0, Long.class);
		return count;
	}

	public List<DataRecord> query(Workspace workspace, Entity entity, JSONArray filters, String... fields) {
		return query(workspace, entity, false, filters, fields);
	}

	public List<DataRecord> query(Workspace workspace, Entity entity, boolean excludeNull, JSONArray filters, String... fields) {
		return query((DataRecordVisitor) null, workspace, 0, Integer.MAX_VALUE, entity, excludeNull, filters, fields);
	}

//	public List<DataRecord> query(Workspace workspace, Integer offset, Integer numberOfRows, Entity entity, String... fields) {
//		return query(null, workspace, offset, numberOfRows, entity, false, fields);
//	}

	public List<DataRecord> query(Workspace workspace, Integer offset, Integer numberOfRows, Entity entity, boolean excludeNull, JSONArray filters, String... fields) {
		return query(null, workspace, offset, numberOfRows, entity, excludeNull, filters, fields);
	}

//	public List<DataRecord> query(DataRecordVisitor visitor, Workspace workspace, Integer offset, Integer numberOfRows, Entity entity, String... fields) {
//		return query(visitor, workspace, offset, numberOfRows, entity, false, fields);
//	}

	public List<DataRecord> query(DataRecordVisitor visitor, Workspace workspace, Integer offset, Integer numberOfRows, Entity entity, boolean excludeNull, JSONArray filters, String... fields) {
		if (fields == null || fields.length == 0) {
			throw new IllegalArgumentException("fields must be specifed");
		}

		List<DataRecord> records = new ArrayList<DataRecord>();

		EntityDataView view = getDataView(workspace, entity);

		// prepare query
		SelectQuery<Record> select = psql().selectQuery();
		select.addFrom(view);
		select.addSelect(view.getIdField());
		for (String field : fields) {
			Field<?> f = view.field(field);
			select.addSelect(f);
			if (excludeNull) {
				select.addConditions(f.isNotNull());
			}
		}
		// add order by id -- important?!
		select.addOrderBy(view.getIdField());

		// add offset to query
		if (offset != null && numberOfRows != null) {
			select.addLimit(offset, numberOfRows);
		} else if (offset == null && numberOfRows != null) {
			select.addLimit(0, numberOfRows);
		} else if (offset != null && numberOfRows == null) {
			select.addLimit(offset);
		}

		// add filters to query
		if( filters != null ) { 
			addQueryFilters(select, view, filters);
		}
		
		// execute the query
		Result<Record> result = select.fetch();

		// process results
		for (Record record : result) {
			Long id = record.getValue(view.getIdField().getName(), Long.class);
			DataRecord dataRecord = new DataRecord(id);
			for (String field : fields) {
				Object value;
				try {
					value = record.getValue(field);
					dataRecord.add(field, value);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			if (visitor != null) {
				visitor.visit(dataRecord);
			}
			records.add(dataRecord);
		}
		return records;
	}

	@SuppressWarnings("unchecked")
	private void addQueryFilters(SelectQuery<Record> select, EntityDataView view, JSONArray filters) {
		
		for ( Object o : filters ) {
			if ( o instanceof JSONObject ) {
				ParameterHashMap conditionParams = new ParameterHashMap( (JSONObject) o );
				Variable<?> variable = view.getEntity().findVariableByName( conditionParams.getString("variable") );
				
				if( variable instanceof MultiwayVariable ) {
					Field<String> field = (Field<String>) view.getCategoryValueField( (MultiwayVariable) variable );
					addFieldConditions( select, field, conditionParams , String.class );
				} else if( variable instanceof QuantitativeVariable ) {
					Field<BigDecimal> field = view.getQuantityField( (QuantitativeVariable) variable );
					addFieldConditions( select, field, conditionParams , BigDecimal.class );
				} else if ( variable instanceof TextVariable ){
					Field<String> field = view.getTextField( (TextVariable) variable );
					addFieldConditions( select, field, conditionParams , String.class );
				}
			}
		}
		
	}

//	 "=" , "!=" , "<" , "<=" , ">" , ">=" , "LIKE" , "NOT LIKE" , "BETWEEN" , "NOT BETWEEN" , "IS NULL" , "IS NOT NULL" 	
	@SuppressWarnings("unchecked")
	private <T> void addFieldConditions( SelectQuery<Record> select, Field<T> field, ParameterHashMap conditionParams , Class<T> fieldType) {
		Condition condition = null;
		for (ParameterMap conditionParam : conditionParams.getList("conditions")) {
			
			T value1 = null; 
			T value2 = null;
			if( String.class.isAssignableFrom( fieldType ) ) {
				value1 = (T) conditionParam.getString("value1");
				value2 = (T) conditionParam.getString("value2");
			} else if( Number.class.isAssignableFrom( fieldType ) ) {
				value1 = (T) conditionParam.getNumber("value1");
				value2 = (T) conditionParam.getNumber("value2");
			} else {
				throw new IllegalArgumentException( "Data type " + fieldType.getName() + " not yet supported" );
			}
			
			String conditionString = conditionParam.getString("condition");
			Condition c = null;
			if ("=".equals(conditionString)) {
				c = field.eq( value1 );
			} else if ("!=".equals(conditionString)) {
				c = field.notEqual( value1 );
			} else if ( "<".equals(conditionString) ) {
				c = field.lessThan(value1);
			} else if ( "<=".equals(conditionString)) {
				c = field.lessOrEqual( value1 );
			} else if ( ">".equals(conditionString)) {
				c = field.greaterThan( value1 );
			} else if ( ">=".equals(conditionString)) {
				c = field.greaterOrEqual( value1 );
			} else if ( "LIKE".equals(conditionString)) {
				c = field.like( value1.toString() );
			} else if ( "NOT LIKE".equals(conditionString)) {
				c = field.notLike( value1.toString() );
			} else if ("BETWEEN".equals(conditionString)) {
				c = field.between( value1 , value2 );
			} else if ("NOT BETWEEN".equals(conditionString)) {
				c = field.notBetween( value1 , value2 );
			} else if ("IS NULL".equals(conditionString)) {
				c = field.isNull();
			} else if ("IS NOT NULL".equals(conditionString)) {
				c = field.isNotNull();
			} else if ("IN".equals(conditionString)) {
				// right now only string with IN clause is working
				JSONArray valuesList = (JSONArray) conditionParam.get("values");
				c = field.in( valuesList );
			}
			if( condition == null ) {
				condition = c;
			} else {
				condition.and( c );
			}
		}
		select.addConditions( condition );
	}

	private EntityDataView getDataView(Workspace workspace, Entity entity) {
		Schemas schemas = new Schemas(workspace);
		EntityDataView view = schemas.getDataSchema().getDataView(entity);
		return view;
	}

}
