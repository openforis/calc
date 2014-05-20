/**
 * 
 */
package org.openforis.calc.metadata;

import java.util.ArrayList;
import java.util.List;

import org.openforis.calc.persistence.jooq.tables.pojos.CategoryHierarchyBase;
import org.openforis.commons.collection.CollectionUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * @author Mino Togna
 * 
 */
public class CategoryHierarchy extends CategoryHierarchyBase {

	private static final long serialVersionUID = 1L;

	private List<CategoryLevel> levels;

	@JsonIgnore
	private Category category;

	public Category getCategory() {
		return category;
	}

	public void setCategory(Category category) {
		this.category = category;
		setCategoryId( category == null || category.getId() == null ? null: category.getId().longValue());
	}

	public List<CategoryLevel> getLevels() {
		return CollectionUtils.unmodifiableList( levels );
	}

	public void setLevels(List<CategoryLevel> levels) {
		this.levels = levels;
	}

	public void addLevel(CategoryLevel categoryLevel) {
		if (this.levels == null) {
			this.levels = new ArrayList<CategoryLevel>();
		}
		this.levels.add(categoryLevel);
		categoryLevel.setHierarchy(this);
	}

}
