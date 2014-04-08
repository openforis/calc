/**
 * 
 */
package org.openforis.calc.engine;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jooq.Field;
import org.jooq.Record;
import org.jooq.SelectQuery;
import org.openforis.calc.chain.CalculationStep;
import org.openforis.calc.metadata.Entity;
import org.openforis.calc.psql.Psql;
import org.openforis.calc.r.RScript;
import org.openforis.calc.r.RVariable;
import org.openforis.calc.r.SetValue;
import org.openforis.calc.schema.EntityDataView;
import org.openforis.calc.schema.InputTable;
import org.openforis.calc.schema.ResultTable;

/**
 * Calculation step tasks are executed grouped by entity. This class groups
 * them.
 * 
 * @author Mino Togna
 * 
 */
public class CalcJobEntityGroup {

	private Map<Integer, List<CalculationStep>> calculationSteps;
	private Map<Integer, List<CalculationStepRTask>> calculationStepTasks;
	private Map<Integer, Set<String>> outputVariables;
	private Map<Integer, Set<String>> inputVariables;
	private Map<Integer, Set<String>> allOutputVariables;
	private Map<Integer, RScript> plotAreaScripts;

	private CalcJob job;

	public CalcJobEntityGroup(CalcJob job) {
		this.job = job;
		this.calculationSteps = new LinkedHashMap<Integer, List<CalculationStep>>();
	}

	public void addCalculationStep(CalculationStep step) {
		Integer entityId = step.getOutputVariable().getEntity().getId();
		List<CalculationStep> steps = this.calculationSteps.get(entityId);
		if (steps == null) {
			steps = new ArrayList<CalculationStep>();
			this.calculationSteps.put(entityId, steps);
		}
		steps.add(step);
	}

	public void addCalculationStep(List<CalculationStep> steps) {
		for (CalculationStep calculationStep : steps) {
			addCalculationStep(calculationStep);
		}
	}

	// init tasks with the given gonnection
	void init(RVariable connection) {

		this.reset();

		for (int entityId : this.entityIds()) {
			// objects to initialize
			List<CalculationStepRTask> calculationStepTasks = new ArrayList<CalculationStepRTask>();
			Set<String> outputVariables = new HashSet<String>();
			Set<String> inputVariables = new HashSet<String>();
			// temp fix it contains all variables will be saved in the results
			// table
			Set<String> allOutputVariables = new HashSet<String>();

			// used to create tasks
			Entity entity = job.getWorkspace().getEntityById(entityId);
			EntityDataView view = job.getSchemas().getDataSchema().getDataView(entity);
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
				CalculationStepRTask task = new CalculationStepRTask(step, job.getrEnvironment(), connection, dataFrame, plotAreaVariable);
				calculationStepTasks.add(task);

				outputVariables.addAll(task.getOutputVariables());
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
			this.outputVariables.put(entityId, outputVariables);
			this.inputVariables.put(entityId, inputVariables);
			this.allOutputVariables.put(entityId, allOutputVariables);
		}

	}

	private void reset() {
		calculationStepTasks = new HashMap<Integer, List<CalculationStepRTask>>();
		outputVariables = new HashMap<Integer, Set<String>>();
		inputVariables = new HashMap<Integer, Set<String>>();
		allOutputVariables = new HashMap<Integer, Set<String>>();
		this.plotAreaScripts = new HashMap<Integer, RScript>();
	}

	public Collection<Integer> entityIds() {
		return this.calculationSteps.keySet();
	}

	public Collection<CalculationStepRTask> getCalculationStepTasks(int entityId) {
		return calculationStepTasks.get(entityId);
	}
	
	public Collection<String> getOutputVariables(int entityId) {
		return outputVariables.get(entityId);
	}

	public Collection<String> getInputVariables(int entityId) {
		return inputVariables.get(entityId);
	}

	public Collection<String> getAllOutputVariables(int entityId) {
		return allOutputVariables.get(entityId);
	}

	public RScript getPlotAreaScript(int entityId) {
		return this.plotAreaScripts.get(entityId);
	}

}
