package org.openforis.calc.engine;

/**
 * 
 * @author G. Miceli
 * @author M. Togna
 *
 */
public class CalculationStepTask extends Task {
	
	private ParameterMap parameters;
	
	protected final ParameterMap parameters() {
		return parameters;
	}
	
	public static <T extends CalculationStepTask> T createTask(Class<T> type, TaskContext context, ParameterMap parameters) {
		T task = Task.createTask(type, context);
		task.parameters = parameters.deepCopy();
		return task;
	}
}
