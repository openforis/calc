package org.openforis.calc.rolap;

import org.openforis.calc.metadata.CategoricalVariable;

/**
 * 
 * @author G. Miceli
 * @author S. Ricci
 *
 */
public class CategoricalVariableDimension extends Dimension {
	private CategoricalVariable variable;
	private CategoryDimensionTable table;
	
	public CategoricalVariableDimension(CategoricalVariable variable, CategoryDimensionTable table) {
		this.variable = variable;
		this.table = table;
	}

	public CategoricalVariable getVariable() {
		return variable;
	}
	
	public CategoryDimensionTable getTable() {
		return table;
	}
}
