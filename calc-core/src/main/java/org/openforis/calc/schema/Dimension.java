package org.openforis.calc.schema;


/**
 * 
 * @author G. Miceli
 * @author S. Ricci
 * @author M. Togna
 */
public class Dimension extends Member {

	protected String name;
	protected Hierarchy hierarchy;

	Dimension() {

	}

	public Hierarchy getHierarchy() {
		return hierarchy;
	}
}
