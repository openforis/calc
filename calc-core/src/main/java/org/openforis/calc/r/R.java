package org.openforis.calc.r;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.rosuda.JRI.RMainLoopCallbacks;
import org.rosuda.JRI.Rengine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/*
 * To use:
	1. Install R and rJava using sudo calc/lib/install.sh
	2. Set env R_HOME=/usr/lib/R or equivalent
	3. Add to Java param -Djava.library.path=.:/usr/local/lib/R/site-library/rJava/jri or equivalent
 * 
 * @author G. Miceli
 *
 */
@Component
public class R {
	private static final String[] R_PARAMS = {"--vanilla", "--slave"};
	private Rengine engine;
	private Logger logger;

	public R() {
		this.logger = LoggerFactory.getLogger(getClass());
	}
	
	public void eval(String s) {
		engine.eval(s);
	}

	public double evalDouble(String s) {
		return engine.eval(s).asDouble();
	}
	
	@PostConstruct
	private void startup() {
		Rengine re = new Rengine(R_PARAMS, true, new RCallbacks());
        if (!re.waitForR()) {
        	throw new RuntimeException("Could not start R");
        }
        this.engine = re; 
	}
	
	@PreDestroy
	private void shutdown() {
		engine.end();
	}
	
	private class RCallbacks implements RMainLoopCallbacks {

		@Override
		public void rBusy(Rengine engine, int which) {
		}

		@Override
		public String rChooseFile(Rengine engine, int newFile) {
			return null;
		}

		@Override
		public void rFlushConsole(Rengine engine) {
		}

		@Override
		public void rLoadHistory(Rengine engine, String filename) {
		}

		@Override
		public String rReadConsole(Rengine engine, String prompt, int addToHistory) {
			return "";
		}

		@Override
		public void rSaveHistory(Rengine engine, String filename) {
		}

		@Override
		public void rShowMessage(Rengine engine, String message) {
		}

		@Override
		public void rWriteConsole(Rengine engine, String text, int arg2) {
			System.out.println(text);
			// TODO why doesn't this print?
			logger.info("R:", text);
		}
	}
}
