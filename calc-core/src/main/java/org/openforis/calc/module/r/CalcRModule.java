package org.openforis.calc.module.r;

import org.openforis.calc.module.Module;

/**
 * Provides support for R-based operations.
 * 
 * @author G. Miceli
 * @author M. Togna
 */
public class CalcRModule extends Module {

	public CalcRModule() {
		super("calc-r", "1.0");
		registerOperation(new CustomROperation(this));
	}
}