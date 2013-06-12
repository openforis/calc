package org.openforis.calc.metadata;

import java.util.ArrayList;

import org.openforis.calc.common.Identifiable;
import org.openforis.calc.metadata.Column;

/**
 * Base class for describing the structure of an output table.
 * 
 * @author G. Miceli
 * @author M. Togna
 */
public abstract class Table implements Identifiable {
	private Integer id;
	private String name;
	private String type;
	private ArrayList<Column> columns = new ArrayList<Column>();

	public boolean isDataTable() {
		throw new UnsupportedOperationException();
	}

	public boolean isCodeListTable() {
		throw new UnsupportedOperationException();
	}

	public Column getColumnByName(String name) {
		throw new UnsupportedOperationException();
	}

	public Column getColumnByType(String type) {
		throw new UnsupportedOperationException();
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
	public enum Type {
		DATA_TABLE, CODE_LIST;
	}
}