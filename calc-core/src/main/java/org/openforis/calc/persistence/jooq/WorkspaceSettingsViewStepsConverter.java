/**
 * 
 */
package org.openforis.calc.persistence.jooq;

import org.jooq.impl.EnumConverter;
import org.openforis.calc.metadata.WorkspaceSettings.VIEW_STEPS;

/**
 * @author Mino Togna
 * 
 */
public class WorkspaceSettingsViewStepsConverter extends EnumConverter<String, VIEW_STEPS> {

	private static final long serialVersionUID = 1L;

	public WorkspaceSettingsViewStepsConverter() {
		super( String.class, VIEW_STEPS.class );
	}

}
