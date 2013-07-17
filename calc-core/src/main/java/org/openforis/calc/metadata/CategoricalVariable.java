package org.openforis.calc.metadata;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A variable which may take on one or more distinct values of type {@link Category}.
 * 
 * @author G. Miceli
 * @author M. Togna
 */
public final class CategoricalVariable extends Variable {
	private boolean ordered;
	private boolean multipleResponse;
	private boolean pivotCategories;
	private ArrayList<Hierarchy> hierarchies = new ArrayList<Hierarchy>();
	private Category defaultCategory;

	public Category getDefaultCategory() {
		return this.defaultCategory;
	}

	public void setOrdered(boolean ordered) {
		this.ordered = ordered;
	}

	public boolean isOrdered() {
		return this.ordered;
	}

	public void setMultipleResponse(boolean multipleResponse) {
		this.multipleResponse = multipleResponse;
	}

	public boolean isMultipleResponse() {
		return this.multipleResponse;
	}

	public void setPivotCategories(boolean pivotCategories) {
		this.pivotCategories = pivotCategories;
	}

	public boolean isPivotCategories() {
		return this.pivotCategories;
	}
	
	public List<Hierarchy> getHierarchies() {
		return Collections.unmodifiableList(hierarchies);
	}
}