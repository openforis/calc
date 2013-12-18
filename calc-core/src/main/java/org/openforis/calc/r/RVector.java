/**
 * 
 */
package org.openforis.calc.r;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * @author Mino Togna
 * @author S. Ricci
 *
 */
public class RVector extends RScript {

	private List<Object> values ;
	private boolean changed;
	
	RVector(RScript previous, Object... values) {
		super(previous);
		
		this.values = new ArrayList<Object>(Arrays.asList(values));
		
		this.changed = true;
	}

	//TODO now only strings and numbers
	protected void createVector() {
		append("c(");
		Iterator<Object> iterator = this.values.iterator();
		while(iterator.hasNext()) {
			Object value = iterator.next();
			if ( value instanceof Number ) {
				append(value);
			} else {
				append("'");
				append(value);
				append("'");
			}
			if ( iterator.hasNext() ) {
				append(",");
			}
		}
		append(")");
	}

	public RVector addValue(Object value) {
		this.values.add(value);
		this.changed = true;
		return this;
	}
	
	public Object getValue(int index) {
		return values.get(index);
	}
	
	public int size() {
		return values.size();
	}
	
	@Override
	protected String toScript() {
		if ( changed ) {
			reset();
			createVector();
		}
		return super.toScript();
	}
}
