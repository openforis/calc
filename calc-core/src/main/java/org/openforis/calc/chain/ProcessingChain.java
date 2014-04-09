package org.openforis.calc.chain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.openforis.calc.engine.ParameterHashMap;
import org.openforis.calc.engine.ParameterMap;
import org.openforis.calc.engine.Workspace;
import org.openforis.calc.persistence.jooq.tables.pojos.ProcessingChainBase;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * User-defined set of steps to be run after pre-processing and before post-processing tasks.
 * 
 * @author G. Miceli
 * @author M. Togna
 */
public class ProcessingChain extends ProcessingChainBase {
	
	private static final long serialVersionUID = 1L;

	@JsonIgnore	
	private Workspace workspace;
	
	private List<CalculationStep> steps;
	
	public ProcessingChain() {
		setParameters( new ParameterHashMap() );
		this.steps = new ArrayList<CalculationStep>();
	}
	
	public List<CalculationStep> getCalculationSteps() {
		return Collections.unmodifiableList(steps);
	}
	
	public Workspace getWorkspace() {
		return workspace;
	}
	
	public void setWorkspace(Workspace workspace) {
		this.workspace = workspace;
		setWorkspaceId( workspace.getId() );
	}
	
	public CalculationStep getCalculationStep( int stepId ){
		CalculationStep step = null;
		
		for ( CalculationStep tmpStep : steps ) {
			int id = Integer.valueOf(stepId);
			if( tmpStep.getId().equals(id)){
				step = tmpStep;
				break;
			}
		}
		return step;
	}
	
	public void addCalculationStep( CalculationStep step ) {
		step.setProcessingChain( this );
		steps.add( step );
	}
	
	/**
	 * Removes the specified calculation step and updates the other steps stepNo
	 * 
	 * @param step
	 */
	public void removeCalculationStep(CalculationStep step) {
		steps.remove(step);
		updateStepNumbers();
	}
	
	/**
	 * Moves a step into the specified stepNo and updates the other steps stepNo so that it will be consistent.
	 * 
	 * @param step
	 * @param stepNo
	 */
	public void shiftStep(CalculationStep step, int stepNo) {
		int oldIndex = step.getStepNo() - 1;
		steps.remove(oldIndex);
		int newIndex = stepNo - 1;
		if ( newIndex == steps.size() ) {
			// move step at the end
			steps.add(step);
		} else {
			// move step in the middle
			steps.add(newIndex, step);
		}
		updateStepNumbers();
	}

	protected void updateStepNumbers() {
		//set step number based on the position in this chain
		int newStepNo = 1;
		for ( CalculationStep calculationStep : steps ) {
			calculationStep.setStepNo(newStepNo ++);
		}
	}
	
	@Override
	@JsonIgnore
	public ParameterMap getParameters() {
		return super.getParameters();
	}
	
}