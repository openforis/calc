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
	
	private List<CalculationStep> calculationSteps;
	
	public ProcessingChain() {
		setParameters( new ParameterHashMap() );
		this.calculationSteps = new ArrayList<CalculationStep>();
	}
	
	public List<CalculationStep> getCalculationSteps() {
		return Collections.unmodifiableList(calculationSteps);
	}
	
	public void clearCalculationSteps() {
		this.calculationSteps = new ArrayList<CalculationStep>();
	}
	
	public Workspace getWorkspace() {
		return workspace;
	}
	
	public void setWorkspace(Workspace workspace) {
		this.workspace = workspace;
		setWorkspaceId( workspace.getId() );
	}
	
	public CalculationStep getCalculationStepById( int stepId ){
		CalculationStep step = null;
		
		for ( CalculationStep tmpStep : calculationSteps ) {
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
		calculationSteps.add( step );
	}
	
	/**
	 * Removes the specified calculation step and updates the other steps stepNo
	 * 
	 * @param step
	 */
	public void removeCalculationStep(CalculationStep step) {
		calculationSteps.remove(step);
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
		calculationSteps.remove(oldIndex);
		int newIndex = stepNo - 1;
		if ( newIndex == calculationSteps.size() ) {
			// move step at the end
			calculationSteps.add(step);
		} else {
			// move step in the middle
			calculationSteps.add(newIndex, step);
		}
		updateStepNumbers();
	}

	protected void updateStepNumbers() {
		//set step number based on the position in this chain
		int newStepNo = 1;
		for ( CalculationStep calculationStep : calculationSteps ) {
			calculationStep.setStepNo(newStepNo ++);
		}
	}
	
	@Override
	@JsonIgnore
	public ParameterMap getParameters() {
		return super.getParameters();
	}
	
}