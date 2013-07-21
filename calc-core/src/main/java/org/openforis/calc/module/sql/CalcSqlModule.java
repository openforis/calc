package org.openforis.calc.module.sql;

import org.openforis.calc.module.Module;

/**
 Provides support for SQL-based operations.
 * 
 * @author G. Miceli
 * @author M. Togna
 */
public class CalcSqlModule extends Module {
	
	public CalcSqlModule() {
		super("calc-sql", "1.0");
		registerOperation(new CustomSqlOperation(this));
	}

}