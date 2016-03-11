package org.openforis.calc.schema;

import org.apache.commons.lang3.StringUtils;
import org.openforis.calc.metadata.Category;
import org.openforis.calc.metadata.CategoryHierarchy;
import org.openforis.calc.metadata.CategoryLevel;
import org.openforis.calc.metadata.MultiwayVariable;
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
		MultiwayVariable variable = table.getVariable();
		
		CategoryLevel categoryLevel = variable.getCategoryLevel();
		CategoryHierarchy categoryHierarchy = categoryLevel.getHierarchy();
		Category category = categoryHierarchy.getCategory();
		
		String caption = StringUtils.isNotBlank(variable.getCaption()) ? variable.getCaption() : category.getCaption();
		caption += " [" + variable.getName()+"]";
		setCaption( caption );
		
		String hName = categoryHierarchy.getName();
//		if( variable.isUserDefined() ){
//			hName += " " + variable.getName();
//		}
		Hierarchy hierarchy = new Hierarchy(hName);
		
		Table t = new Table(table.getSchema().getName(), table.getName());
		hierarchy.setTable(t);
		
//		String caption = variable.getCaption();
//		if ( StringUtils.isBlank(caption) ) {
//			caption = WordUtils.capitalize( variable.getName() );
//		}
		
		String idColumnName = ( variable.isSpecieCategory() ) ? table.getCodeField().getName() : table.getIdField().getName();
		Level level = new Level(variable.getName(), idColumnName, table.getCaptionField().getName() , categoryLevel.getCaption() );
		hierarchy.addLevel(level);
		
		setHierarchy(hierarchy);
	}

	public CategoryDimensionTable getTable() {
		return table;
	}
	
}
