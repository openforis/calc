package org.openforis.calc.chain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.openforis.calc.engine.ParameterHashMap;
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
	
	public void addCalculationStep( CalculationStep step ) {
		step.setProcessingChain( this );
		steps.add( step );
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
	
	public void shiftStep(CalculationStep step, int newIndex) {
		steps.set(newIndex, step);
		//set step no based on position in calculation step
		int newStepNo = 1;
		for ( CalculationStep calculationStep : steps ) {
			calculationStep.setStepNo(newStepNo ++);
		}
	}

}