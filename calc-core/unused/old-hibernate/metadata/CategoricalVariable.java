package org.openforis.calc.metadata;

/**
 * A variable which may take on one or more distinct values of type {@link Category}.
 * 
 * @author G. Miceli
 * @author M. Togna
 */
public abstract class CategoricalVariable<T> extends Variable<T> {

	private static final long serialVersionUID = 1L;

	public CategoricalVariable() {
	}

	protected CategoricalVariable(Scale scale) {
		super.setScale(scale);
	}

}
