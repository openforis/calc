package org.openforis.calc.engine;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openforis.calc.chain.CalculationStep;
import org.openforis.calc.r.RScript;
import org.openforis.calc.r.RVariable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Base class for asynchronous  
 * 
 * @author G. Miceli
 *
 */
public abstract class Worker {

	private Status status;	
	private UUID id;
	private long startTime;
	private long endTime;
	private long itemsProcessed;
	private long itemsSkipped;
	private long totalItems;
	private Throwable lastException;

	// TODO can use 'transient' Java keyword instead?
	@JsonIgnore
	private Logger logger;

	public enum Status {
		PENDING, RUNNING, COMPLETED, FAILED, ABORTED;
	}

	public Worker() {
		this.status = Status.PENDING;
		this.startTime = -1;
		this.endTime = -1;
		this.itemsProcessed = 0;
		this.itemsSkipped = 0;
		this.lastException = null;
		this.logger = LoggerFactory.getLogger(getClass());
		this.id = UUID.randomUUID();
	}
	
	synchronized 
	public void init() {
		this.totalItems = countTotalItems();
	}

	// TODO
//	protected abstract long countTotalItems();
	protected long countTotalItems() { return -1; };

	protected abstract void execute() throws Throwable;

	public String getName() {
		return getClass().getSimpleName();
	}
	
	public synchronized void run() {
		if ( !isPending() ) { 
			throw new IllegalStateException("Already run");
		}
		try {
			this.status = Status.RUNNING;
			this.startTime = System.currentTimeMillis();
			execute();
			this.status = Status.COMPLETED;
		} catch ( Throwable t ) {
			this.status = Status.FAILED;
			this.lastException = t;
			logger.warn("Task failed");
			t.printStackTrace();
		} finally {
			this.endTime = System.currentTimeMillis();
			notifyAll();
		}
	}

	public final long getDuration() {
		switch ( status ) {
		case PENDING:
			return -1;
		case RUNNING:
			return System.currentTimeMillis() - startTime;
		default:
			return endTime - startTime;
		}
	}

	public final boolean isPending() {
		return status == Status.PENDING;
	}

	public final boolean isRunning() {
		return status == Status.RUNNING;
	}

	public final boolean isFailed() {
		return status == Status.FAILED;
	}

	public final boolean isAborted() {
		return status == Status.ABORTED;
	}

	public final boolean isCompleted() {
		return status == Status.COMPLETED;
	}

	/**
	 * If task was run and finished, aborted or failed
	 * 
	 * @return
	 */
	public final boolean isEnded() {
		return status != Status.PENDING && status != Status.RUNNING;
	}

	public final Task.Status getStatus() {
		return this.status;
	}

	public final long getStartTime() {
		return this.startTime;
	}

	public final long getEndTime() {
		return this.endTime;
	}

	public long getItemsProcessed() {
		return this.itemsProcessed;
	}

	public long getItemsSkipped() {
		return this.itemsSkipped;
	}

	public final long getTotalItems() {
		return this.totalItems;
	}

	public final Throwable getLastException() {
		return this.lastException;
	}
	
	public void setItemsProcessed(long itemsProcessed) {
		this.itemsProcessed = itemsProcessed;
	}
	
	public long incrementItemsProcessed() {
		return ++this.itemsProcessed;
	}

	public long incrementItemsSkipped() {
		return ++this.itemsSkipped;
	}
	
	public long getItemsRemaining() {
		return getTotalItems() - (getItemsProcessed() + getItemsSkipped()); 
	}

	public UUID getId() {
		return id;
	}

	protected final Logger log() {
		return this.logger;
	}

	public synchronized boolean waitFor(int timeoutMillis) {
		long start = System.currentTimeMillis();
		while (!isEnded() && System.currentTimeMillis() - start < timeoutMillis) {
			try {
				wait(timeoutMillis);
			} catch (InterruptedException e) {
			}
		}
		return isCompleted();
	}
	
	// returns a new r script. left here now since this is the common super class between calcjob and calcrscript
	protected RScript r() {
		return new RScript();
	}

	protected String replaceVariables(RVariable dataFrame, String script) {
		String newScript = script;
		
		Pattern pattern = Pattern.compile(CalculationStep.VARIABLE_PATTERN);
		Matcher m = pattern.matcher(script);
		while (m.find()) {
			String variable = m.group(1);
			RVariable rVariable = new RScript().variable(dataFrame, variable);
			newScript = newScript.replaceFirst("\\$"+variable+"\\$", rVariable.toString().replaceAll("\\$","\\\\\\$") );
		}
		
		return newScript;
	}
	
	protected Set<String> extractVariables(String script) {
		Set<String> variables = new HashSet<String>();
		Pattern p = Pattern.compile(CalculationStep.VARIABLE_PATTERN);
		Matcher m = p.matcher(script);
		while (m.find()) {
			String variable = m.group(1);
			variables.add(variable);
		}
		return variables;
	}
}