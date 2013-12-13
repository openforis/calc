package org.openforis.calc.r;

import org.rosuda.REngine.REXP;
import org.rosuda.REngine.REXPInteger;
import org.rosuda.REngine.REXPMismatchException;
import org.rosuda.REngine.REXPString;
import org.rosuda.REngine.RList;

/**
 * 
 * @author G. Miceli
 * @author Mino Togna
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
			String[] colNames = { "name", "no" };
			REXP df = REXP.createDataFrame(new RList(new REXP[] { new REXPString("TEST"), new REXPInteger(123) }, colNames));
			r.assign(symbol, df, env);
		} catch (REXPMismatchException e) {
			throw new RException(e);
		}
	}

	public void eval(String expr) throws RException {
		this.eval(expr, null);
	}

	public double evalDouble(String expr) throws RException {
		return this.evalDouble(expr, null);
	}

	public String[] evalStrings(String expr) throws RException {
		return this.evalStrings(expr, null);
	}

	public void eval(String expr, RLogger logger) throws RException {
		r.eval(expr, env, false, logger);
	}

	public double evalDouble(String expr, RLogger logger) throws RException {
		try {
			return r.eval(expr, env, true, logger).asDouble();
		} catch (REXPMismatchException e) {
			throw new RException(e);
		}
	}

	public String[] evalStrings(String expr, RLogger logger) throws RException {
		try {
			return r.eval(expr, env, true, logger).asStrings();
		} catch (REXPMismatchException e) {
			throw new RException(e);
		}
	}

}
