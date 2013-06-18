package org.openforis.calc.r;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.rosuda.JRI.Rengine;
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

	private Rengine engine;

	public String eval(String s) {
		return engine.eval(s).asString();
	}

	public double evalDouble(String s) {
		return engine.eval(s).asDouble();
	}
	
	@PostConstruct
	private void startup() {
		Rengine re = new Rengine(new String [] {"--vanilla"}, false, null);
        if (!re.waitForR()) {
        	throw new RuntimeException("Could not start R");
        }
        this.engine = re; 
	}
	
	@PreDestroy
	private void shutdown() {
		engine.end();
	}
}
