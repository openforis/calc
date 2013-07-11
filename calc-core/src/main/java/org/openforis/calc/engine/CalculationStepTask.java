package org.openforis.calc.engine;

/**
 * 
 * @author G. Miceli
 * @author M. Togna
 * 
 */
public class CalculationStepTask extends Task {

	private ParameterMap parameters;
	private CalculationStep calculationStep;

	protected final ParameterMap parameters() {
		return parameters;
	}

	public static <T extends CalculationStepTask> T createTask(Class<T> type, TaskContext context, CalculationStep step) {
		T task = Task.createTask(type, context);

		task.setCalculationStep(step);

		return task;
	}

	public CalculationStep getCalculationStep() {
		return calculationStep;
	}

	private void setCalculationStep(CalculationStep calculationStep) {
		this.calculationStep = calculationStep;
		
		ParameterMap parameterMap = calculationStep.parameters();
		this.parameters = parameterMap.deepCopy();
	}
}
