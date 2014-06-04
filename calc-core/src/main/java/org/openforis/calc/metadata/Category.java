/**
 * 
 */
package org.openforis.calc.metadata;

import java.util.ArrayList;
import java.util.List;

import org.openforis.calc.engine.Workspace;
import org.openforis.calc.persistence.jooq.tables.pojos.CategoryBase;
import org.openforis.commons.collection.CollectionUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * @author Mino Togna
 *
 */
public class Category extends CategoryBase {

	private static final long serialVersionUID = 1L;
	
	@JsonIgnore
	private Workspace workspace;
	
	private List<CategoryHierarchy> hierarchies;
	
	public Workspace getWorkspace() {
		return workspace;
	}
	
	public void setWorkspace(Workspace workspace) {
		this.workspace = workspace;
		this.setWorkspaceId( workspace.getId().longValue() );
	}
	
	public List<CategoryHierarchy> getHierarchies() {
		return CollectionUtils.unmodifiableList( hierarchies );
	}
	
	public void setHierarchies(List<CategoryHierarchy> hierarchies) {
		this.hierarchies = hierarchies;
	}
	
	public void addHierarchy( CategoryHierarchy categoryHierarchy ){
		if( this.hierarchies == null ){
			this.hierarchies = new ArrayList<CategoryHierarchy>();
		}
		this.hierarchies.add( categoryHierarchy );
		categoryHierarchy.setCategory( this ); 
	}

	public boolean isInput() {
		return getOriginalId() != null;
	}

	public boolean isUserDefined() {
		return getOriginalId() == null;
	}
}
