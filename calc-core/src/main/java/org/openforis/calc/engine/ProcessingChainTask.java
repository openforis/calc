package org.openforis.calc.engine;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * 
 * @author G. Miceli
 * @author M. Togna
 *
 */
public class ProcessingChainTask extends Task {
	
	private ParameterMap parameters;
	
	protected ProcessingChainTask(Context context) {
		super(context);
	}
	
	protected final ParameterMap parameters() {
		return parameters;
	}

	public static <T extends ProcessingChainTask> T createTask(Class<T> type, Context context, ParameterMap parameters) {
		try {
			Constructor<T> c = type.getConstructor(Context.class);
			T task = c.newInstance(context);
			task.parameters = parameters.deepCopy();
			return task;
		} catch (InstantiationException e) {
			throw new IllegalArgumentException("Invalid task "+type.getClass(), e);
		} catch (IllegalAccessException e) {
			throw new IllegalArgumentException("Invalid task "+type.getClass(), e);
		} catch (NoSuchMethodException e) {
			throw new RuntimeException(e);
		} catch (InvocationTargetException e) {
			throw new RuntimeException(e);
		}
	}
}
