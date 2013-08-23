package org.openforis.calc.schema;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.jooq.Table;
import org.jooq.impl.SchemaImpl;

/**
 * 
 * @author G. Miceli
 * @author M. Togna
 */
public abstract class RelationalSchema extends SchemaImpl {

	private static final long serialVersionUID = 1L;
	private List<Table<?>> tables;

	public RelationalSchema(String name) {
		super(name);
		this.tables = new ArrayList<Table<?>>();
	}

	@Override
	public List<Table<?>> getTables() {
		return Collections.unmodifiableList(tables);
	}

	public void addTable(Table<?> table) {
		tables.add(table);
	}
}
