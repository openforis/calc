/**
 * 
 */
package org.openforis.calc.schema;


/**
 * A table derived from phase1; this includes a column for each aoi level
 * 
 * @author M. Togna
 * 
 */
public class Phase1AoiTable extends DataAoiTable {

	private static final long serialVersionUID = 1L;

	protected Phase1AoiTable(InputSchema schema) {
		super("_phase1_aoi", schema);
	}

}
