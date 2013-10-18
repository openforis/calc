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
	private List<Table<?>> views;

	public RelationalSchema(String name) {
		super(name);
		this.tables = new ArrayList<Table<?>>();
	}

	@Override
	public List<Table<?>> getTables() {
		return Collections.unmodifiableList(tables);
	}

	protected void addTable(Table<?> table) {
		tables.add(table);
	}
	
	public List<Table<?>> getViews() {
		return Collections.unmodifiableList(views);
	}

	protected void addView(Table<?> view) {
		tables.add(view);
	}
}
