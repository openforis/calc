package org.openforis.calc.schema;

/**
 * 
 * @author G. Miceli
 * @author S. Ricci
 *
 */
public class Member {
	
	protected RolapSchema rolapSchema;
	
	Member(RolapSchema rolapSchema) {
		this.rolapSchema = rolapSchema;
	}
	
	RolapSchema getRolapSchema() {
		return rolapSchema;
	}
	
}
