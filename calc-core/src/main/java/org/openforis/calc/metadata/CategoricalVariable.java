package org.openforis.calc.metadata;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.persistence.DiscriminatorValue;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * A variable which may take on one or more distinct values of type {@link Category}.
 * 
 * @author G. Miceli
 * @author M. Togna
 */
@javax.persistence.Entity
@DiscriminatorValue("C")
public class CategoricalVariable extends Variable {
	private boolean ordered;
	private boolean multipleResponse;
	private boolean pivotCategories;
	private ArrayList<Hierarchy> hierarchies = new ArrayList<Hierarchy>();
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "default_category_id")
	@JsonIgnore
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