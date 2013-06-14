package org.openforis.calc.engine;

import java.util.Collections;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.hibernate.annotations.Type;
import org.openforis.calc.common.UserObject;
import org.openforis.calc.persistence.ParameterHashMap;

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
	
	@OneToMany(mappedBy = "chain")
	private List<CalculationStep> steps;
	
	@Type(type="org.openforis.calc.persistence.JsonParameterMapType")
	@Column(name = "parameters")
	private ParameterMap parameters;

	public ProcessingChain() {
		this.parameters = new ParameterHashMap();
	}
	
	public ProcessingChainJob createJob(Context context) {
		throw new UnsupportedOperationException();
	}
	
	public List<CalculationStep> getSteps() {
		return Collections.unmodifiableList(steps);
	}
	
	public ParameterMap parameters() {
		return parameters;
	}
	
	public Workspace getWorkspace() {
		return workspace;
	}
	
	void setWorkspace(Workspace workspace) {
		this.workspace = workspace;
	}
}