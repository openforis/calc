package org.openforis.calc.module;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.openforis.calc.nls.Captionable;

/**
 * A Module is a bundling of a set of Operations, commonly grouped by problem domain (e.g. allometric tree modeling). Implementations of this class are discovered using {@link java.util.ServiceLoader}; to be discoverable, the implementation's fully qualified name must be declared in the module JAR's META-INF/services/org.openforis.calc.engine.Module.
 * 
 * @author G. Miceli
 * @author M. Togna
 */
public abstract class Module implements Captionable {
	private final String name;
	private final String version;
	private final Map<String, Operation<?>> operations;

	protected Module(String name, String version) {
		this.name = name;
		this.version = version;
		this.operations = new HashMap<String, Operation<?>>();
	}

	protected final void registerOperation(Operation<?> operation) {
		if ( operations.containsKey(operation.getName()) ) {
			throw new IllegalArgumentException(String.format("Opertion with '%s' already registered in module '%s'", operation, this));
		}
		operations.put(operation.getName(), operation);
	}
	
	public final Set<Operation<?>> getOperations() {
		Set<Operation<?>> set = new HashSet<Operation<?>>(operations.values());
		return Collections.unmodifiableSet(set);
	}
	
	public final String getName() {
		return name;
	}
	
	public final String getVersion() {
		return version;
	}
	
	@Override
	public String toString() {
		return String.format("%s-%s", name, version);
	}

	public Operation<?> getOperation(String operationName) {
		return operations.get(operationName);
	}
}