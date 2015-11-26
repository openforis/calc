/**
 * 
 */
package org.openforis.calc.engine;

import java.util.ArrayList;
import java.util.List;

import org.jooq.impl.SchemaImpl;
import org.openforis.calc.psql.Psql;
import org.openforis.calc.r.DbConnect;
import org.openforis.calc.r.REnvironment;
import org.openforis.calc.r.RException;
import org.openforis.calc.r.RLogger;
import org.openforis.calc.r.RScript;
import org.openforis.calc.r.RVariable;

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

	private RVariable rConnection;

//	private boolean shutDown;
	
	protected CalcRTask( REnvironment rEnvironment, String name ){
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

		String script = toString();

		rEnvironment.eval(script, logger);

		// an R error has been detected by the logger
		if (logger.containsCalcErrorSignal()) {
			throw new RException("R error while evaluating " + this.name);
		}

//		if (isShutDown()) {
//			getJob().r.shutdown();
//		}
	}

	protected RLogger getJobLogger() {
		CalcJob job = (CalcJob) getJob();
		RLogger logger = job.getRLogger();
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

	@JsonIgnore
	protected RVariable getrConnection() {
		return rConnection;
	}

	protected void addCloseConnectionScript() {
		addScript(r().dbDisconnect(rConnection));
	}

	protected void addOpenConnectionScript() {
		// init libraries
		// initTask.addScript(r().library("lmfor"));
		addScript(r().library("RPostgreSQL"));
		
		// common functions //org/openforis/calc/r/functions.R
		addScript(RScript.getCalcCommonScript());
		// create driver
		RVariable driver = r().variable("driver");
		addScript(r().setValue(driver, r().dbDriver("PostgreSQL")));

		rConnection = r().variable("connection");
		DbConnect dbConnect = r().dbConnect(driver, getHost(), getDatabase(), getUser(), getPassword(), getPort());
		addScript(r().setValue(rConnection, dbConnect));

		// set search path to current and public schemas
		addScript(r().dbSendQuery(rConnection, new Psql().setDefaultSchemaSearchPath(getInputSchema(), new SchemaImpl("public"))));
	}

//	boolean isShutDown() {
//		return shutDown;
//	}
//
//	void setShutDown(boolean shutDown) {
//		this.shutDown = shutDown;
//	}
}
