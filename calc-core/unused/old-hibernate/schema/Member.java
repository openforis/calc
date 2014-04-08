package org.openforis.calc.schema;

import org.apache.commons.lang3.text.WordUtils;

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
	
	protected String capitalize(String string) {
		return WordUtils.capitalize(string);
	}
	
}
