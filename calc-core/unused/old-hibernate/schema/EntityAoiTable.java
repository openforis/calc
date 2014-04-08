/**
 * 
 */
package org.openforis.calc.schema;

import org.openforis.calc.metadata.Entity;


/**
 * A table derived from phase1; this includes a column for each aoi level
 * 
 * @author M. Togna
 * 
 */
public class EntityAoiTable extends DataAoiTable {

	private static final long serialVersionUID = 1L;

	protected EntityAoiTable(Entity entity, InputSchema inputSchema) {

		super( getName(entity) , inputSchema);
	}
	
	private static String getName(Entity entity) {
		return String.format( "_%s_aoi", entity.getName() );
	}

}
