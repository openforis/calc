package org.openforis.calc.r;

//import org.rosuda.JRI.RMainLoopCallbacks;
//import org.rosuda.JRI.Rengine;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Scanner;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.openforis.calc.system.SystemUtils;
import org.rosuda.REngine.REXP;
import org.rosuda.REngine.REXPMismatchException;
import org.rosuda.REngine.REngine;
import org.rosuda.REngine.REngineException;
import org.rosuda.REngine.JRI.JRIEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Synchronized access to R native R engine
 * 
 * To use: 1. Install R and rJava using sudo calc/lib/install-R.sh 2. Set
 * environment variable R_HOME=/usr/lib/R
 * 
 * @author G. Miceli
 * 
 */
@Component
public class R {
	private static final String[] R_PARAMS = { "--vanilla", "--slave" };
	private REngine engine;
	private Logger logger;
	private RStdOutputListner rStdOutputListner;

	public R() {
		this.logger = LoggerFactory.getLogger(getClass());
	}

	@PostConstruct
	synchronized public void startup() {
		try {
			String jriPath = getJriPath();
			if (jriPath != null) {
				SystemUtils.addLibraryPath(jriPath);
				logger.info("JRI path added to library path: " + jriPath);
			}
			this.rStdOutputListner = new RStdOutputListner(this);
			this.engine = REngine.engineForClass(JRIEngine.class.getName(), R_PARAMS, rStdOutputListner, true);
		} catch (InvocationTargetException e) {
			throw new RuntimeException(e);
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		} catch (NoSuchMethodException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Returns JRI absolute path from R.
	 * JRI is included by rJava, the usual path should be /usr/lib/R/library/rJava/jri
	 */
	private String getJriPath() {
		Scanner s = null;
		try {
			ProcessBuilder pb = new ProcessBuilder("R", "--slave", "-e", "system.file('jri',package='rJava')");
			Process p = pb.start();
			p.waitFor();
			InputStream inputStream = p.getInputStream();
			s = new Scanner(inputStream);
			s.next("\\[1\\]");
			String path = s.next(".*");
			// Remove quotes
			path = path.substring(1, path.length() - 1);
			return path;
		} catch (Exception e) {
			logger.warn("Error getting JRI library path from R");
			// throw new
			// RuntimeException("Error getting JRI library path from R");
			return null;
		} finally {
			if ( s != null ) {
				s.close();
			}
		}
	}

	@PreDestroy
	synchronized public void shutdown() {
		engine.close();
	}

	public REnvironment newEnvironment() throws RException {
		try {
			REXP env = engine.newEnvironment(null, false);
			return new REnvironment(this, env);
		} catch (REXPMismatchException e) {
			throw new RException(e);
		} catch (REngineException e) {
			throw new RException(e);
		}
	}

	synchronized REXP eval(String expr, REXP env, boolean resolve, RLogger logger) throws RException {
		try {
			// before execution register logger
			this.rStdOutputListner.registerLogger(logger);

			REXP rexp = engine.parseAndEval(expr, env, resolve);
			
			return rexp;
		} catch (REXPMismatchException e) {
			throw new RException(e);
		} catch (REngineException e) {
			throw new RException(e);
		} finally {
			// after execution unregister logger
			this.rStdOutputListner.unregisterLogger(logger);
		}
	}

	synchronized void assign(String symbol, REXP value, REXP env) throws RException {
		try {
			engine.assign(symbol, value, env);
		} catch (REXPMismatchException e) {
			throw new RException(e);
		} catch (REngineException e) {
			throw new RException(e);
		}
	}

	Logger getLogger() {
		return logger;
	}

}
