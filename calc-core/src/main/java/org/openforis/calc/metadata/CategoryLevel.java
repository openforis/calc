/**
 * 
 */
package org.openforis.calc.metadata;

import org.openforis.calc.persistence.jooq.tables.pojos.CategoryLevelBase;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * @author Mino Togna
 *
 */
public class CategoryLevel extends CategoryLevelBase {

	private static final long serialVersionUID = 1L;
	
	@JsonIgnore
	private CategoryHierarchy hierarchy;
	
	public CategoryHierarchy getHierarchy() {
		return hierarchy;
	}
	
	public void setHierarchy( CategoryHierarchy categoryHierarchy ){
		this.hierarchy = categoryHierarchy;
		setHierarchyId( categoryHierarchy.getId() );
	}

}
