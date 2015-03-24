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
		super( getName(workspace) , workspace);
	}

	public static String getName(Workspace workspace) {
		return workspace.getExtendedSchemaName();
	}
	
	@Override
	protected void initSchema() {
		initCategoryDimensionTables();
	}
	
}
