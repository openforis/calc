package org.openforis.calc.persistence.jooq.rolap;

/**
 * 
 * @author G. Miceli
 *
 */
public abstract class HierarchicalDimensionTable<R extends HierarchicalDimensionRecord<R>> extends DimensionTable<R> {
	private static final long serialVersionUID = 1L;
	
	public final org.jooq.TableField<R, Integer> PARENT_ID = createField("parent_id", org.jooq.impl.SQLDataType.INTEGER, this);

	HierarchicalDimensionTable(String schema, String name, Class<R> recordType) {
		super(schema, name, recordType);
	}

}
