/**
 * 
 */
package org.openforis.calc.schema;

import org.openforis.calc.schema.Hierarchy.Level;
import org.openforis.calc.schema.Hierarchy.View;

/**
 * @author G. Miceli
 * @author M. Togna
 * 
 */
public class StratumDimension extends Dimension {

	private static final String STRATUM = "Stratum";
	private StratumDimensionTable table;

	StratumDimension(RolapSchema rolapSchema, StratumDimensionTable stratumDimensionTable) {
		super(rolapSchema);
		this.table = stratumDimensionTable;

		setName(STRATUM);
		setCaption(STRATUM);
		createHierarchy();
	}

	private void createHierarchy() {
		Hierarchy hierarchy = new Hierarchy( STRATUM );

		View view = new View( STRATUM, this.table.getSelect().toString() );
		hierarchy.setView( view );
		
		Level level = new Level( STRATUM, this.table.getStratumNo().getName(), this.table.getCaption().getName(), STRATUM );
		hierarchy.addLevel( level );
		
		setHierarchy(hierarchy);
	}

}
