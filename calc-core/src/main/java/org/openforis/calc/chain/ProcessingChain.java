package org.openforis.calc.chain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.Type;
import org.openforis.calc.common.UserObject;
import org.openforis.calc.engine.ParameterHashMap;
import org.openforis.calc.engine.ParameterMap;
import org.openforis.calc.engine.Workspace;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * User-defined set of steps to be run after pre-processing and before post-processing tasks.
 * 
 * @author G. Miceli
 * @author M. Togna
 */
@javax.persistence.Entity
@Table(name = "processing_chain")
public class ProcessingChain extends UserObject {
	
	@JsonIgnore	
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "workspace_id")
	private Workspace workspace;
	
	@OneToMany(mappedBy = "processingChain", fetch = FetchType.EAGER)
	@Fetch(value = FetchMode.SELECT)
	@OrderBy("stepNo")
	private List<CalculationStep> steps;
	
	@Type(type="org.openforis.calc.persistence.hibernate.JsonParameterMapType")
	@Column(name = "parameters")
	private ParameterMap parameters;

	public ProcessingChain() {
		this.parameters = new ParameterHashMap();
		this.steps = new ArrayList<CalculationStep>();
	}
	
	public synchronized List<CalculationStep> getCalculationSteps() {
		return Collections.unmodifiableList(steps);
	}
	
	public ParameterMap parameters() {
		return parameters;
	}
	
	public Workspace getWorkspace() {
		return workspace;
	}
	
	public void setWorkspace(Workspace workspace) {
		this.workspace = workspace;
	}
	
	public synchronized void addCalculationStep(CalculationStep step) {
		step.setProcessingChain(this);
		steps.add(step);
	}
	
	public synchronized CalculationStep getCalculationStep( int stepId ) {
		for ( CalculationStep s : steps ) {
			if ( s.getId().equals(stepId) ){
				return s;
			}
		}
		return null;
	}
	
	public synchronized void removeStepById(int stepId) {
		Iterator<CalculationStep> it = steps.iterator();
		while ( it.hasNext() ) {
			CalculationStep s = it.next();
			if ( s.getId().equals(stepId) ) {
				it.remove();
			}
		}
	}

	public int getNextStepNo() {
		int result = 0;
		for (CalculationStep calculationStep : getCalculationSteps()) {
			int stepNo = calculationStep.getStepNo();
			result = Math.max(result, stepNo);
		}
		return result + 1;
	}
	
	public void shiftStep(CalculationStep step, int stepNo) {
		int oldStepNo = step.getStepNo();

		for ( CalculationStep s : steps ) {
			if ( s.getStepNo() > oldStepNo ) {
				s.setStepNo(s.getStepNo() - 1);
			}
		}
		for ( CalculationStep s : steps ) {
			if ( s.getStepNo() >= stepNo ) {
				s.setStepNo(s.getStepNo() + 1);
			}
		}
		step.setStepNo(stepNo);
	}

}