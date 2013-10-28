package org.openforis.calc.model;

/**
 * 
 * @author G. Miceli
 *
 * @param <T>
 */
class AttributeImpl<T> implements Attribute<T> {

	private String name;

	AttributeImpl(String name) {
		this.name = name;
	}

	@Override
	public String getName() {
		return name;
	}
}
