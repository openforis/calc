package org.openforis.calc.r;

import org.rosuda.REngine.REXP;
import org.rosuda.REngine.REXPMismatchException;

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

	public void assign(String symbol, RDataFrame df) throws RException {
		//String[] colNames = {"name", "no"};
		//REXP df = REXP.createDataFrame(new RList(new REXP[] { new REXPString("TEST"), new REXPInteger(123) }, colNames));
		REXP rexpDF = df.toREXP();
		r.assign(symbol, rexpDF, env);
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
