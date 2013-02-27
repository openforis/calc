package org.openforis.calc.persistence.jooq.rolap;

import org.openforis.calc.model.VariableMetadata;

/**
 * 
 * @author G. Miceli
 *
 */
public class CategoryDimensionTable extends DimensionTable<CategoryDimensionRecord> {

	private static final long serialVersionUID = 1L;

	private VariableMetadata variableMetadata;
	
	CategoryDimensionTable(String schema, VariableMetadata var) {
		super(schema, var.getDimensionTableName(), CategoryDimensionRecord.class);
		this.variableMetadata = var;
	}

	public VariableMetadata getVariableMetadata() {
		return variableMetadata;
	}
}
