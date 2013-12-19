package org.openforis.calc.metadata;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Transient;

/**
 * A {@link CategoricalVariable} that is not a {@link BinaryVariable}
 * 
 * @author G. Miceli
 *
 */
@javax.persistence.Entity
@DiscriminatorValue("C")
public class MultiwayVariable extends CategoricalVariable<String> {

	@Column(name = "default_value")
	private String defaultValue;
	
	@Column(name = "multiple_response")
	private Boolean multipleResponse;

	@Transient // TODO persist
	private boolean pivotCategories;
	
	@Transient // TODO persist
	private ArrayList<CategoryHierarchy> hierarchies = new ArrayList<CategoryHierarchy>();
	
	@Column(name = "input_category_id_column")
	private String inputCategoryIdColumn;

	@Column(name = "output_category_id_column")
	private String outputCategoryIdColumn;

	public void setMultipleResponse(boolean multipleResponse) {
		this.multipleResponse = multipleResponse;
	}
	public boolean isMultipleResponse() {
		return multipleResponse == null || multipleResponse;
	}

	public void setPivotCategories(boolean pivotCategories) {
		this.pivotCategories = pivotCategories;
	}

	public boolean isPivotCategories() {
		return this.pivotCategories;
	}
	
	public List<CategoryHierarchy> getHierarchies() {
		return Collections.unmodifiableList(hierarchies);
	}
	
	public String getInputCategoryIdColumn() {
		return inputCategoryIdColumn;
	}

	public void setInputCategoryIdColumn(String inputCategoryIdColumn) {
		this.inputCategoryIdColumn = inputCategoryIdColumn;
	}

	public String getOutputCategoryIdColumn() {
		return outputCategoryIdColumn;
	}

	public void setOutputCategoryIdColumn(String outputCategoryIdColumn) {
		this.outputCategoryIdColumn = outputCategoryIdColumn;
	}

	@Override
	public Type getType() {
		return Type.CATEGORICAL;
	}
	
	@Override
	public boolean isInput() {
		return super.isInput() || inputCategoryIdColumn != null;
	}
	
	/**
	 * Indicates if the order of the categories has meaning (ORDINAL, 
	 * e.g. severity, size) or if they are unordered categorizations 
	 * (NOMINAL, e.g. land use, ownership).
	 * 
	 * @return
	 */
	public boolean isOrdered() {
		return getScale() == Scale.ORDINAL;
	}

	@Override
	public void setScale(Scale scale) {
		if ( scale != Scale.NOMINAL && scale != Scale.ORDINAL ) {
			throw new IllegalArgumentException("Illegal scale: " + scale);
		}
		super.setScale(scale);
	}
	
	@Override
	public String getDefaultValue() {
		return defaultValue;
	}
	
	@Override
	public void setDefaultValue(String defaultValue) {
		this.defaultValue = defaultValue;
	}
	
	
}
