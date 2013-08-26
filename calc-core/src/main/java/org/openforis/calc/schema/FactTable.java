/**
 * 
 */
package org.openforis.calc.schema;

import org.jooq.Record;
import org.jooq.TableField;
import org.jooq.UniqueKey;
import org.openforis.calc.metadata.Entity;


/**
 * @author G. Miceli
 * 
 */
public class FactTable extends AbstractTable {

	private static final long serialVersionUID = 1L;
	private OutputDataTable dataTable;
	private Entity entity;
	private TableField<Record, Integer> idField;
	private UniqueKey<Record> primaryKey;	
	
	@SuppressWarnings("unchecked")
	public FactTable(Entity entity, OutputDataTable dataTable) {
		super(getFactTableName(dataTable), dataTable.getSchema());
		this.entity = entity;
		this.dataTable = dataTable;
		this.idField = copyField(dataTable.getIdField());
		this.primaryKey = KeyFactory.newUniqueKey(this, idField);
		createCategoryDimensionFields();
	}

	private void createCategoryDimensionFields() {
//		Field<?>[] fields = dataTable.fields();
//		for (Field<?> field : fields) {
//			
//		}
//		// TODO Auto-generated method stub
		
	}

	private static String getFactTableName(DataTable dataTable) {
		return "_"+dataTable.getName()+"_fact";
	}
	
	public OutputDataTable getDataTable() {
		return dataTable;
	}
	
	public Entity getEntity() {
		return entity;
	}
	
	@Override
	public UniqueKey<Record> getPrimaryKey() {
		return primaryKey;
	}
}
