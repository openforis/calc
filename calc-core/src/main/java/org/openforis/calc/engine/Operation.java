package org.openforis.calc.engine;

import org.openforis.calc.common.ReflectionUtils;
import org.openforis.calc.nls.Captionable;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Describes an operation which may be run as part of a user-defined processing chain. The actual work is done by the appropriate Task implementation.
 * 
 * @author G. Miceli
 * @author M. Togna
 */
public abstract class Operation<T extends CalculationStepTask> implements Captionable {
	
	@JsonIgnore
	private Module module;
	private String name;

	protected Operation(Module module, String name) {
		this.module = module;
		this.name = name;
	}

	public final T createTask(TaskContext context, CalculationStep step) {
		Class<T> type = ReflectionUtils.extractGenericType(getClass());
		return CalculationStepTask.createTask(type, context, step);
	}

	public String getName() {
		return name;
	}

	public Module getModule() {
		return module;
	}
}
