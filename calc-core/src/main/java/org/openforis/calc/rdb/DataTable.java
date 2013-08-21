/**
 * 
 */
package org.openforis.calc.rdb;

import static org.jooq.impl.SQLDataType.INTEGER;

import org.jooq.Record;
import org.jooq.TableField;
import org.jooq.UniqueKey;
import org.openforis.calc.metadata.Entity;


/**
 * @author G. Miceli
 * @author M. Togna
 * 
 */
public abstract class DataTable extends AbstractTable {

	private static final long serialVersionUID = 1L;

	private final TableField<Record, Integer> idColumn;
	
	private Entity entity;

	private UniqueKey<Record> primaryKey;
	
	@SuppressWarnings("unchecked")
	public DataTable(Entity entity, RelationalSchema schema) {
		super(entity.getDataTable(), schema);
		this.entity = entity;
		String idColumnName = entity.getIdColumn();
		this.idColumn = createField(idColumnName, INTEGER, this);
		this.primaryKey = KeyFactory.newUniqueKey(this, idColumn);
	}
	
	public TableField<Record, Integer> getIdColumn() {
		return idColumn;
	}
	
	public Entity getEntity() {
		return entity;
	}
	
	@Override
	public UniqueKey<Record> getPrimaryKey() {
		return primaryKey;
	}
}
