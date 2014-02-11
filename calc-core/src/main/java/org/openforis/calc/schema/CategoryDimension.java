package org.openforis.calc.schema;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.WordUtils;
import org.openforis.calc.schema.Hierarchy.Level;
import org.openforis.calc.schema.Hierarchy.Table;

/**
 * 
 * @author G. Miceli
 * @author S. Ricci
 * @author M. Togna
 */
public class CategoryDimension extends Dimension {

	private CategoryDimensionTable table;

	public CategoryDimension(RolapSchema rolapSchema, CategoryDimensionTable table) {
		super(rolapSchema);
		this.table = table;
		
		setName(table.getVariable().getOutputValueColumn());
		createHierarchy();
	}

	private void createHierarchy() {
		Hierarchy hierarchy = new Hierarchy(table.getVariable().getName());
		
		Table t = new Table(table.getSchema().getName(), table.getName());
		hierarchy.setTable(t);
		
		String caption = table.getVariable().getCaption();
		if ( StringUtils.isBlank(caption) ) {
			caption = WordUtils.capitalize( table.getVariable().getName() );
		}
		
		Level level = new Level(table.getVariable().getName(), table.getIdField().getName(), table.getCaptionField().getName() , caption );
		hierarchy.addLevel(level);
		
		setHierarchy(hierarchy);
	}

	public CategoryDimensionTable getTable() {
		return table;
	}
	
}
