package org.openforis.calc.rolap;

import org.openforis.calc.metadata.CategoricalVariable;

/**
 * 
 * @author G. Miceli
 * @author S. Ricci
 *
 */
public class CategoryDimensionTable extends DimensionTable {

	private static final long serialVersionUID = 1L;
	private CategoricalVariable variable;
	
	CategoryDimensionTable(RelationalSchema schema, CategoricalVariable variable) {
		super(variable.getDimensionTable(), schema);
		this.variable = variable;
	}

	public CategoricalVariable getVariable() {
		return variable;
	}
}
