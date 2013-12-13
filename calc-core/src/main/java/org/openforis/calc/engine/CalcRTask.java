/**
 * 
 */
package org.openforis.calc.engine;

import java.util.ArrayList;
import java.util.List;

import org.openforis.calc.r.REnvironment;
import org.openforis.calc.r.RException;
import org.openforis.calc.r.RLogger;
import org.openforis.calc.r.RScript;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * @author Mino Togna
 * 
 */
public class CalcRTask extends Task {

	@JsonIgnore
	private REnvironment rEnvironment;
	@JsonIgnore
	private List<RScript> scripts;

	private String name;

	protected CalcRTask(REnvironment rEnvironment, String name) {
		this.rEnvironment = rEnvironment;
		this.scripts = new ArrayList<RScript>();
		this.name = name;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openforis.calc.engine.Worker#execute()
	 */
	@Override
	protected void execute() throws Throwable {
		RLogger logger = getJobLogger();
		rEnvironment.eval( toString(), logger );

		// an R error has been logged
		if (logger.containsCalcErrorSignal()) {
			throw new RException("R error while evaluating " + this.name);
		}
	}

	private RLogger getJobLogger() {
		CalcJob job = (CalcJob) getJob();
		RLogger logger = job.getLogger();
		return logger;
	}

	protected void addScript(RScript rScript) {
		this.scripts.add(rScript);
	}

	@Override
	protected long countTotalItems() {
		return -1;
	}

	@Override
	public String getName() {
		return name;
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (RScript script : this.scripts) {
			String scriptString = script.toString();
			sb.append(scriptString);
		}
		return sb.toString();
	}

}
