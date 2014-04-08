package org.openforis.calc.metadata;

import org.openforis.calc.common.UserObject;

/**
 * Collects a set of {@link Category}s together into a user-defined grouping.  Non-leaf groups contain other Groups, which leaves contain actual Categories.
 * 
 * @author G. Miceli
 * @author M. Togna
 */
public class CategoryGroup extends UserObject {
	private String code;
	private int index;
	private CategoryHierarchy hierarchy;
	private CategoryLevel level;

	public void setCode(String code) {
		this.code = code;
	}

	public String getCode() {
		return this.code;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public int getIndex() {
		return this.index;
	}
	
	public CategoryHierarchy getHierarchy() {
		return hierarchy;
	}
	
	public CategoryLevel getLevel() {
		return level;
	}
}