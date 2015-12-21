/**
 * 
 */
package org.openforis.calc.chain.export;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openforis.calc.chain.CalculationStep;
import org.openforis.calc.chain.ProcessingChain;
import org.openforis.calc.engine.Workspace;
import org.openforis.calc.metadata.CategoricalVariable;
import org.openforis.calc.metadata.Entity;
import org.openforis.calc.metadata.QuantitativeVariable;
import org.openforis.calc.metadata.Variable;
import org.openforis.calc.schema.ResultTable;
import org.openforis.commons.collection.CollectionUtils;

/**
 * Calculation step tasks are executed grouped by entity. This class groups
 * them.
 * 
 * @author Mino Togna
 * 
 */
public class CalculationStepsGroup {

	private Map<Integer, List<CalculationStep>> calculationSteps;
	private Map<Integer, Set<String>> resultVariables;
	// list of all calculation steps, including the disabled ones
	private List<CalculationStep> allCalculationSteps;

	private Workspace workspace;

	
	public CalculationStepsGroup( Workspace workspace ){
		this.workspace 			= workspace;
		this.initCalculationSteps( Boolean.TRUE );
	}
	
	private void initCalculationSteps( boolean onlyActive ){

		ProcessingChain processingChain = this.workspace.getDefaultProcessingChain();
		this.calculationSteps 			= new LinkedHashMap<Integer, List<CalculationStep>>();
		this.resultVariables			= new LinkedHashMap<Integer, Set<String>>();
		this.allCalculationSteps		= new ArrayList<CalculationStep>();
		
		List<CalculationStep> steps = processingChain.getCalculationSteps();
		for (CalculationStep calculationStep : steps) {
			
			this.allCalculationSteps.add( calculationStep );
			
			if( !onlyActive || calculationStep.getActive() ){
				this.addCalculationStep(calculationStep);
			}
		}
		
	}
	
	private void addCalculationStep(CalculationStep step) {
		Integer entityId 			= step.getOutputVariable().getEntity().getId();
		List<CalculationStep> steps = this.calculationSteps.get(entityId);
		if (steps == null) {
			steps = new ArrayList<CalculationStep>();
			this.calculationSteps.put(entityId, steps);
		}
		steps.add(step);
		
		Set<String> resultVariables = this.resultVariables.get( entityId );
		if (resultVariables == null) {
			resultVariables = new HashSet<String>();
			this.resultVariables.put( entityId, resultVariables );
		}
		
		Variable<?> outputVariable 	= step.getOutputVariable();
		Entity entity 				= outputVariable.getEntity();
		String variableName 		= outputVariable.getName();
		
		resultVariables.add(variableName);
		
		if( outputVariable instanceof CategoricalVariable ) {
			resultVariables.add(outputVariable.getInputCategoryIdColumn());
		}
		
		if ( outputVariable instanceof QuantitativeVariable && entity.isInSamplingUnitHierarchy() && workspace.hasSamplingDesign() ) {
			String variablePerHaName = ( (QuantitativeVariable)outputVariable ).getVariablePerHaName();
			resultVariables.add(variablePerHaName);
			
			resultVariables.add( ResultTable.PLOT_AREA_COLUMN_NAME );
		}
		
	}
	
	public Collection<CalculationStep> getAllCalculationSteps(){
		return CollectionUtils.unmodifiableList( this.allCalculationSteps );
	}
	
	public Collection<Integer> activeEntityIds() {
		return this.calculationSteps.keySet();
	}
	
	public Set<String> getResultVariables( int entityId ) {
		return resultVariables.get( entityId );
	}
	
	public List<CalculationStep> getCalculationSteps( int entityId ){
		return this.calculationSteps.get( entityId );
	}
	
	public Workspace getWorkspace() {
		return workspace;
	}
	
}
