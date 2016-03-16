/**
 * 
 */
package org.openforis.calc.schema;

import org.apache.commons.lang3.StringUtils;
import org.openforis.calc.metadata.Category;
import org.openforis.calc.metadata.CategoryHierarchy;
import org.openforis.calc.metadata.CategoryLevel;
import org.openforis.calc.metadata.MultiwayVariable;
import org.openforis.calc.schema.Hierarchy.Level;
import org.openforis.calc.schema.Hierarchy.Table;

/**
 * @author M. Togna
 * 
 */
public class SpeciesCategoryDimension extends Dimension {

	private SpeciesCategoryDimensionTable table;

	SpeciesCategoryDimension( RolapSchema rolapSchema, SpeciesCategoryDimensionTable table ){
		super(rolapSchema);
		
		this.table = table;
		String entityName = this.table.getVariable().getEntity().getName();
		setName( entityName + "_"+ table.getName() );
		createHierarchy();
	}

	private void createHierarchy() {
		
		MultiwayVariable variable 		= table.getVariable();
		CategoryLevel cLevel 			= variable.getCategoryLevel();
		CategoryHierarchy cHierarchy 	= cLevel.getHierarchy();
		Category category 				= cHierarchy.getCategory();
		
		String variableName 			= variable.getName();
		
		String caption 					= StringUtils.isNotBlank(variable.getCaption()) ? variable.getCaption() : category.getCaption();
		caption += " [" + variableName+"]";
		setCaption( caption );
		
		Hierarchy hierarchy = new Hierarchy( cHierarchy.getName() );
		setHierarchy( hierarchy );
		Table t 			= new Table(table.getSchema().getName(), table.getName());
		hierarchy.setTable(t);
		
		
		String genusIdColumnName = table.getGenusIdField().getName();
		Level genusLevel 		= new Level( "genus_"+ variableName, genusIdColumnName , table.getGenusCaptionField().getName() , "Genus "+ cLevel.getCaption() );
		hierarchy.addLevel( genusLevel );		
		
		String idColumnName 	= table.getIdField().getName();
		Level speciesLevel 		= new Level( variableName, idColumnName, table.getCaptionField().getName() , cLevel.getCaption() );
		hierarchy.addLevel( speciesLevel );
		
	}

	public SpeciesCategoryDimensionTable getTable() {
		return table;
	}
	
}
