/**
 * 
 */
package org.openforis.calc.metadata;

import org.apache.commons.lang3.StringUtils;
import org.openforis.calc.engine.Workspace;
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

	public void setHierarchy(CategoryHierarchy categoryHierarchy) {
		this.hierarchy = categoryHierarchy;
		setHierarchyId(categoryHierarchy.getId());
	}

	// overriden for backwards compatibility.
	// before schema name was not imported.
	@Override
	public String getSchemaName() {
		String schemaName = super.getSchemaName();
		if (StringUtils.isBlank(schemaName)) {
			Category category = hierarchy.getCategory();
			Workspace workspace = category.getWorkspace();
			schemaName = workspace.getInputSchema();
		}
		return schemaName;
	}
	
	// used for export / import
	public static class CategoryLevelValue {
		
		private Long id;
		private String code;
		private String caption;
		
		public CategoryLevelValue(){
			super();
		}

		public CategoryLevelValue( Long id , String code , String caption ){
			super();
			this.id = id;
			this.code = code;
			this.caption = caption;
		}

		public Long getId() {
			return id;
		}
		
		public String getCode() {
			return code;
		}
		
		public String getCaption() {
			return caption;
		}
	}
	
}
