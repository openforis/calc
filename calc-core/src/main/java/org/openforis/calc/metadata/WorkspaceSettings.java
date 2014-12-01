/**
 * 
 */
package org.openforis.calc.metadata;

import org.openforis.calc.engine.Workspace;
import org.openforis.calc.persistence.jooq.tables.pojos.WorkspaceSettingsBase;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * @author Mino Togna
 *
 */
public class WorkspaceSettings extends WorkspaceSettingsBase {

	private static final long serialVersionUID = 1L;

	@JsonIgnore
	private Workspace workspace;
	
	public enum VIEW_STEPS {
		AS_LIST("as_list") , BY_ENTITY("by_entity");
		
		private String value;

		private VIEW_STEPS(String value) {
			this.value = value;
		}
		
		@Override
		public String toString() {
			return value.toLowerCase();
		}
		
	}
	
	public Workspace getWorkspace() {
		return workspace;
	}
	
	public void setWorkspace( Workspace workspace ){
		if( workspace != null ){
			this.workspace = workspace;
			Integer wsId = workspace.getId();
			if( wsId != null ){
				this.setWorkspaceId( wsId.longValue() );
			}
		}
	}
	
}
