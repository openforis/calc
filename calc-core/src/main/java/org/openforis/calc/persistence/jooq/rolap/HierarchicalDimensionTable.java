package org.openforis.calc.persistence.jooq.rolap;

import static org.jooq.impl.SQLDataType.*;

import org.jooq.Record;
import org.jooq.SQLDialect;
import org.jooq.SelectQuery;
import org.jooq.impl.Factory;
/**
 * 
 * @author G. Miceli
 *
 */
public abstract class HierarchicalDimensionTable extends DimensionTable {
	private static final long serialVersionUID = 1L;
	
	public final org.jooq.TableField<Record, Integer> PARENT_ID = createField("parent_id", INTEGER, this);

	protected HierarchicalDimensionTable parentTable;

	HierarchicalDimensionTable(String schema, String name, HierarchicalDimensionTable parentTable) {
		super(schema, name);
		this.parentTable = parentTable;
	}

	public String getDenormalizedSelectSql() {
		Factory create = new Factory(SQLDialect.POSTGRES);
		SelectQuery select = create.selectQuery();
		select.addFrom(this);
		buildDenormalizedSelect(select, this);
		return select.getSQL();
	}

 	private void buildDenormalizedSelect(SelectQuery select, HierarchicalDimensionTable table) {
 		HierarchicalDimensionTable parentTable = table.getParentTable();
 		if ( parentTable != null ) {
 			select.addJoin(parentTable, table.PARENT_ID.eq(parentTable.ID));
			buildDenormalizedSelect(select, parentTable);
 		}
 		select.addSelect(table.ID.as(table.getDenormalizedIdColumn()));
 		select.addSelect(table.LABEL.as(table.getDenormalizedLabelColumn()));
	}

	public String getDenormalizedIdColumn() {
		return getName()+"_id";
	}

	public String getDenormalizedLabelColumn() {
		return getName()+"_label";
	}

	public HierarchicalDimensionTable getParentTable() {
		return parentTable;
	}

}
