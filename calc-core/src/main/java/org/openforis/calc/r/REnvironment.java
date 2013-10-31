package org.openforis.calc.r;

import org.rosuda.REngine.REXP;
import org.rosuda.REngine.REXPInteger;
import org.rosuda.REngine.REXPMismatchException;
import org.rosuda.REngine.REXPString;
import org.rosuda.REngine.RList;

/**
 * 
 * @author G. Miceli
 *
 */
public class REnvironment {
	private R r;
	private REXP env;
	
	REnvironment(R r, REXP env) throws RException {
		this.r = r;
		this.env = env;
	}

	public void assign(String symbol, RDataFrame frame) throws RException {
		try {
			String[] colNames = {"name", "no"};
			REXP df = REXP.createDataFrame(new RList(new REXP[] { new REXPString("TEST"), new REXPInteger(123) }, colNames));
			r.assign(symbol, df, env);
		} catch (REXPMismatchException e) {
			throw new RException(e);
		}
	}

	public void eval(String expr) throws RException {
		r.eval(expr, env, false);
	}

	public double evalDouble(String expr) throws RException {
		try {
			return r.eval(expr, env, true).asDouble();
		} catch (REXPMismatchException e) {
			throw new RException(e);
		}
	}

	public String[] evalStrings(String expr) throws RException {
		try {
			return r.eval(expr, env, true).asStrings();
		} catch (REXPMismatchException e) {
			throw new RException(e);
		}
	}
	

}
