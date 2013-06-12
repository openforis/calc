package org.openforis.calc.metadata;

import org.openforis.calc.common.Identifiable;

/**
 * Describes a column in a table in the output schema.
 * 
 * @author G. Miceli
 * @author M. Togna
 */
public class Column implements Identifiable {
	private Integer id;
	private String name;
	private String type;
	private Table table;

	public Table getTable() {
		return this.table;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Integer getId() {
		return this.id;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return this.name;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getType() {
		return this.type;
	}
}