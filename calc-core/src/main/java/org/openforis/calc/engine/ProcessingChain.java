package org.openforis.calc.engine;

import java.util.Collections;
import java.util.List;

import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.openforis.calc.common.UserObject;
import org.openforis.calc.workspace.Workspace;

/**
 * User-defined set of steps to be run after pre-processing and before post-processing tasks.
 * 
 * @author G. Miceli
 */
@javax.persistence.Entity
@Table(name = "processing_chain")
public final class ProcessingChain extends UserObject {
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "workspace_id")
	private Workspace workspace;
	
	// TODO is EAGER necessary, or is it default?
	@OneToMany(mappedBy = "chain", fetch = FetchType.EAGER)
	private List<CalculationStep> steps;
	
	@Transient
	private Parameters chainParameters;

	public ProcessingChainJob createJob(Task.Context context) {
		throw new UnsupportedOperationException();
	}
	
	public List<CalculationStep> getSteps() {
		return Collections.unmodifiableList(steps);
	}
}