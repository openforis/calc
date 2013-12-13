package org.openforis.calc.r;

import org.rosuda.REngine.REngine;
import org.rosuda.REngine.REngineStdOutput;

/**
 * 
 * @author Mino Togna
 * 
 */
class RStdOutputListner extends REngineStdOutput {

	private final R r;

	// TODO create a set of loggers instead one single logger to allow more than
	// 1 logger to be registered
	private RLogger logger;

	RStdOutputListner(R r) {
		this.r = r;
	}

	@Override
	public void RWriteConsole(REngine engine, String text, int oType) {
		if (r.getLogger().isDebugEnabled()) {
			super.RWriteConsole(engine, text, oType);
		}

		// if a logger has been registered, append text to the logger
		if (logger != null) {
			this.logger.append(oType, text);
		}

	}

	void registerLogger(RLogger logger) {
		this.logger = logger;

	}

	void unregisterLogger(RLogger logger) {
		if (this.logger == logger) {
			this.logger = null;
		}

	}
}
