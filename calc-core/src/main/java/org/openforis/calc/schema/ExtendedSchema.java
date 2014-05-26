/**
 * 
 */
package org.openforis.calc.schema;

import org.openforis.calc.engine.Workspace;

/**
 * @author Mino Togna
 *
 */
public class ExtendedSchema extends DataSchema {

	private static final long serialVersionUID = 1L;

	public ExtendedSchema(Workspace workspace) {
		super( workspace.getInputSchema() + "_ext" , workspace);
	}
	
	@Override
	protected void initSchema() {
		initCategoryDimensionTables();
	}
	
}
