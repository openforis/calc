package org.openforis.calc.metadata;

/**
 * A variable which may take on one or more distinct values of type {@link Category}.
 * 
 * @author G. Miceli
 * @author M. Togna
 */
public abstract class CategoricalVariable<T> extends Variable<T> {

	private static final long serialVersionUID = 1L;

	private CategoryLevel categoryLevel;
	
	public CategoricalVariable() {
	}

	protected CategoricalVariable(Scale scale) {
		super.setScale(scale);
	}

	public CategoryLevel getCategoryLevel() {
		return categoryLevel;
	}
	
	public void setCategoryLevel(CategoryLevel categoryLevel){
		this.categoryLevel = categoryLevel;
		setCategoryLevelId( categoryLevel == null || categoryLevel.getId() == null ? null: categoryLevel.getId().longValue());
	}
	
}
