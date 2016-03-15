/**
 * 
 */
package org.openforis.calc.schema;

import org.openforis.calc.collect.SpeciesCodeView;
import org.openforis.calc.schema.Hierarchy.Level;
import org.openforis.calc.schema.Hierarchy.View;

/**
 * @author M. Togna
 * 
 */
public class SpeciesCategoryDimension extends Dimension {

	private static final String STRATUM = "Stratum";
	private SpeciesCodeView table;

	SpeciesCategoryDimension(RolapSchema rolapSchema, SpeciesCodeView specieCodeView) {
		super(rolapSchema);
		this.table = specieCodeView;

		setName( specieCodeView.getSpeciesListName() );
		setCaption( specieCodeView.getSpeciesListName() );
//		createHierarchy();
	}

//	private void createHierarchy() {
//		Hierarchy hierarchy = new Hierarchy( STRATUM );
//
//		View view = new View( STRATUM, this.table.getSelect().toString() );
//		hierarchy.setView( view );
//		
//		Level level = new Level( STRATUM, this.table.getStratumNo().getName(), this.table.getCaption().getName(), STRATUM );
//		hierarchy.addLevel( level );
//		
//		setHierarchy(hierarchy);
//	}

}
