package org.openforis.calc.rolap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.jooq.Table;
import org.jooq.impl.SchemaImpl;

/**
 * 
 * @author G. Miceli
 *
 */
public class RelationalSchema extends SchemaImpl {

	private static final long serialVersionUID = 1L;
	private List<Table<?>> tables;
	private List<CategoryDimensionTable> categoryDimensionTables;
	
	RelationalSchema(String name) {
		super(name);
		this.tables = new ArrayList<Table<?>>();
	}

	
	@Override
	public List<Table<?>> getTables() {
		return Collections.unmodifiableList(tables);
	}
	
	void addTable(Table<?> table) {
		tables.add(table);
		if ( table instanceof CategoryDimensionTable ) {
			categoryDimensionTables.add((CategoryDimensionTable) table);
		}
	}


	public List<CategoryDimensionTable> getCategoryDimensionTables() {
		return Collections.unmodifiableList(categoryDimensionTables);
	}
}
