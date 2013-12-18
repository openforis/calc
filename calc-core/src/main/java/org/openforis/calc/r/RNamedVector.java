/**
 * 
 */
package org.openforis.calc.r;

/**
 * @author S. Ricci
 *
 */
public class RNamedVector extends RVector {

	private String name;

	public RNamedVector(RScript previous, String name, Object... values) {
		super(previous, values);
		this.name = name;
	}

	public String getName() {
		return name;
	}
	
	@Override
	protected void createVector() {
		append(name);
		append(" = ");
		super.createVector();
	}
	
	
}
