/**
 * 
 */
package org.openforis.calc.chain.export;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openforis.calc.chain.CalculationStep;
import org.openforis.calc.chain.ProcessingChain;
import org.openforis.calc.engine.CalcJob;
import org.openforis.calc.engine.CalculationStepRTask;
import org.openforis.calc.engine.Workspace;
import org.openforis.calc.metadata.CategoricalVariable;
import org.openforis.calc.metadata.Entity;
import org.openforis.calc.metadata.QuantitativeVariable;
import org.openforis.calc.metadata.Variable;
import org.openforis.calc.r.Div;
import org.openforis.calc.r.RScript;
import org.openforis.calc.r.RVariable;
import org.openforis.calc.schema.ResultTable;

/**
 * Calculation step tasks are executed grouped by entity. This class groups
 * them.
 * 
 * @author Mino Togna
 * 
 */
public class CalculationStepsGroup {

	private Map<Integer, List<CalculationStep>> calculationSteps;
	private Map<Integer, List<CalculationStepRTask>> calculationStepTasks;
	private Map<Integer, Set<String>> outputVariables;
	private Map<Integer, Set<String>> inputVariables;
	private Map<Integer, Set<String>> resultVariables;
	private Map<Integer, RScript> plotAreaScripts;

	private CalcJob job;
	private Workspace workspace;

	@Deprecated
	public CalculationStepsGroup( CalcJob job ){
		this.job = job;
		this.calculationSteps = new LinkedHashMap<Integer, List<CalculationStep>>();
		
	}
	
	public CalculationStepsGroup( Workspace workspace ){
		this.workspace 			= workspace;
		this.initCalculationSteps( Boolean.TRUE );
	}
	
	private void initCalculationSteps( boolean onlyActive ){

		ProcessingChain processingChain = this.workspace.getDefaultProcessingChain();
		this.calculationSteps 			= new LinkedHashMap<Integer, List<CalculationStep>>();
		this.resultVariables			= new LinkedHashMap<Integer, Set<String>>();
		
		List<CalculationStep> steps = processingChain.getCalculationSteps();
		for (CalculationStep calculationStep : steps) {
			if( !onlyActive || calculationStep.getActive() ){
				this.addCalculationStep(calculationStep);
			}
		}
		
	}
	
	public void addCalculationStep(CalculationStep step) {
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
	
	public Collection<Integer> entityIds() {
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
	
	@Deprecated
	// init tasks with the given gonnection
	public void init(RVariable connection) {

		this.reset();

		for (int entityId : this.entityIds()) {
			// objects to initialize
			List<CalculationStepRTask> calculationStepTasks = new ArrayList<CalculationStepRTask>();
			Set<String> rawOutputVariables = new HashSet<String>();
			Set<String> inputVariables = new HashSet<String>();
			// temp fix it contains all variables will be saved in the results
			// table
			Set<String> allOutputVariables = new HashSet<String>();

			// used to create tasks
			Entity entity = job.getWorkspace().getEntityById(entityId);
//			EntityDataView view = job.getSchemas().getDataSchema().getDataView(entity);
//			InputTable table = job.getSchemas().getInputSchema().getDataTable(entity);
//			Field<?> primaryKeyField = view.getPrimaryKey().getFields().get(0);
//			String primaryKey = primaryKeyField.getName();
			RVariable dataFrame = job.r().variable(entity.getName());

			// create calc steps

			// plot area script if available
			RScript plotArea = entity.getPlotAreaRScript();
			// plot_area variable hardcoded now
			RVariable plotAreaVariable = null;
			if (plotArea != null) {
				plotAreaVariable = job.r().variable(dataFrame, ResultTable.PLOT_AREA_COLUMN_NAME);
				allOutputVariables.add(ResultTable.PLOT_AREA_COLUMN_NAME);
				inputVariables.addAll(plotArea.getVariables());

				plotAreaScripts.put(entityId, plotArea);
			}

			// create a task for each step
			for (CalculationStep step : this.calculationSteps.get(entityId)) {
				CalculationStepRTask task = new CalculationStepRTask(step, job, connection, dataFrame, plotAreaVariable);
				calculationStepTasks.add(task);

				rawOutputVariables.addAll(task.getOutputVariables());
				inputVariables.addAll(task.getInputVariables());
				allOutputVariables.addAll(task.getAllOutputVariables());
			}
			
			// assign dataframe from select r script
//			SelectQuery<Record> select = new Psql().selectQuery();
//			select.addFrom(view);
//			select.addSelect(view.getIdField());
//			for (String var : inputVariables ) {
//				select.addSelect(view.field(var));
//			}
//			SetValue assignDataFrame = job.r().setValue( dataFrame, job.r().dbGetQuery(connection, select) );
			
			
			
			
			this.calculationStepTasks.put(entityId, calculationStepTasks);
			this.outputVariables.put(entityId, rawOutputVariables);
			this.inputVariables.put(entityId, inputVariables);
			this.resultVariables.put(entityId, allOutputVariables);
		}

	}

	@Deprecated
	private void reset() {
		calculationStepTasks = new HashMap<Integer, List<CalculationStepRTask>>();
		outputVariables = new HashMap<Integer, Set<String>>();
		inputVariables = new HashMap<Integer, Set<String>>();
		resultVariables = new HashMap<Integer, Set<String>>();
		this.plotAreaScripts = new HashMap<Integer, RScript>();
	}

	
	@Deprecated
	public Collection<CalculationStepRTask> getCalculationStepTasks(int entityId) {
		return calculationStepTasks.get(entityId);
	}
	@Deprecated
	public Collection<String> getOutputVariables(int entityId) {
		return outputVariables.get(entityId);
	}
	@Deprecated
	public Collection<String> getInputVariables(int entityId) {
		return inputVariables.get(entityId);
	}

	@Deprecated
	public Collection<String> getAllOutputVariables(int entityId) {
		return resultVariables.get(entityId);
	}
	@Deprecated
	public RScript getPlotAreaScript(int entityId) {
		return this.plotAreaScripts.get(entityId);
	}
	@Deprecated
	public Set< QuantitativeVariable > uniqueOutputQuantitativeVariables(){
		Set< QuantitativeVariable > uniqueOutputVariables = new HashSet< QuantitativeVariable > ();
		
		for ( List<CalculationStep> list : this.calculationSteps.values() ){
			for ( CalculationStep calculationStep : list ){
				Variable<?> variable = calculationStep.getOutputVariable();
				if( variable instanceof QuantitativeVariable ){
					uniqueOutputVariables.add( (QuantitativeVariable) variable );
				}
			}
		}
		
		return uniqueOutputVariables;
	}

}
