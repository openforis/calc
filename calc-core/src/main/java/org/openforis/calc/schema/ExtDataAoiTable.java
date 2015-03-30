/**
 * 
 */
package org.openforis.calc.schema;


/**
 * A table derived from phase1 or primary su; this includes a column for each aoi level
 * 
 * @author M. Togna
 * 
 */
public class ExtDataAoiTable extends DataAoiTable {

	private static final long serialVersionUID = 1L;

	protected ExtDataAoiTable( String name, DataSchema schema ){
		super( name , schema);
	}

}
