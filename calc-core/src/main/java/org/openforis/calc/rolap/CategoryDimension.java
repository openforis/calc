package org.openforis.calc.rolap;

import org.openforis.calc.metadata.CategoricalVariable;
import org.openforis.calc.rdb.CategoryDimensionTable;

/**
 * 
 * @author G. Miceli
 * @author S. Ricci
 *
 */
public class CategoryDimension extends Dimension {
	private CategoricalVariable variable;
	private CategoryDimensionTable table;
	
	public CategoryDimension(CategoricalVariable variable, CategoryDimensionTable table) {
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
