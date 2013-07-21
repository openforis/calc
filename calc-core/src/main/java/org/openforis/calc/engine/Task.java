package org.openforis.calc.engine;

import org.openforis.calc.nls.Captionable;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * A unit of work in the system.
 * 
 * Tasks are not reusable.
 * 
 * @author G. Miceli
 * @author M. Togna
 */
public abstract class Task extends Worker implements Captionable {
	@JsonIgnore
	private Job job;
	
	public JobContext getContext() {
		return getJob().getContext();
	}

	public Workspace getWorkspace() {
		return getContext().getWorkspace();
	}
	
	public Job getJob() {
		return job;
	}
	
	void setJob(Job job) {
		this.job = job;
	}
}
