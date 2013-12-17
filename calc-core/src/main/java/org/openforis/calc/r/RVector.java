/**
 * 
 */
package org.openforis.calc.r;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

/**
 * @author Mino Togna
 *
 */
public class RVector extends RScript {

	private List<String> strings ;
	RVector(RScript previous, String... values) {
		super(previous);
		this.strings = new ArrayList<String>();
		for (String value : values) {
			this.strings.add(value);
		}
		
		reset();
	}

	protected void reset() {
		super.reset();
		
		append("c('");
		append( StringUtils.join(this.strings, "','") );
		append("')");
	}

	public RVector addValue(String value) {
		this.strings.add(value);
		reset();
		return this;
	}
}
