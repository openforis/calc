/**
 * 
 */
package org.openforis.calc.schema;

import org.openforis.calc.metadata.AoiHierarchy;

/**
 * @author M. Togna
 *
 */
public class AoiDimension extends Dimension {

	private AoiHierarchy aoiHierarchy;
	
	
	AoiDimension(AoiHierarchy aoiHierarchy) {
		this.aoiHierarchy = aoiHierarchy;
	}
	
//	@Override
//	protected void createLevels() {
//		this.levels = new ArrayList<Hierarchy>();
//		
//	}
//	
	
}
