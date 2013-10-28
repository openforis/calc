package org.openforis.calc.metadata;

import org.openforis.calc.common.NamedUserObject;

/**
 * Defines a single level of a hierarchy of (@link Category}s.
 */
public class CategoryLevel extends NamedUserObject {
	private Integer id;
	private String description;
	private int rank;
	private CategoryHierarchy hierarchy;

	public void setId(Integer id) {
		this.id = id;
	}

	public Integer getId() {
		return this.id;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getDescription() {
		return this.description;
	}

	public int getRank() {
		return this.rank;
	}
	
	public CategoryHierarchy getHierarchy() {
		return hierarchy;
	}
}