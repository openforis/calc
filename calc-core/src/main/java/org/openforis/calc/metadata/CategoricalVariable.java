package org.openforis.calc.metadata;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;

/**
 * A variable which may take on one or more distinct values of type {@link Category}.
 * 
 * @author G. Miceli
 * @author M. Togna
 */
@MappedSuperclass
public abstract class CategoricalVariable<T> extends Variable<T> {

	@Column(name = "disaggregate")
	private boolean disaggregate;

	// TODO remove term "dimension" from name (Calc model uses statistical terminology while "dimension" is OLAP term)
	@Column(name = "degenerate_dimension")
	private boolean degenerateDimension;

	public CategoricalVariable() {
	}
	
	protected CategoricalVariable(Scale scale) {
		super.setScale(scale);
	}

	public boolean isDisaggregate() {
		return disaggregate;
	}

	public void setDisaggregate(boolean disaggregate) {
		this.disaggregate = disaggregate;
	}

	public Boolean isDegenerateDimension() {
		return degenerateDimension;
	}

	public void setDegenerateDimension(boolean degenerateDimension) {
		this.degenerateDimension = degenerateDimension;
	}
}
