package org.openforis.calc.engine;

import java.util.Collections;
import java.util.List;
import java.util.ArrayList;

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
	private final List<Operation<?>> operations;

	protected Module(String name, String version) {
		this.name = name;
		this.version = version;
		this.operations = new ArrayList<Operation<?>>();
	}

	protected final void registerOperation(Operation<?> operation) {
		operations.add(operation);
	}
	
	public final List<Operation<?>> getOperations() {
		return Collections.unmodifiableList(operations);
	}
	
	public final String getName() {
		return name;
	}
	
	public final String getVersion() {
		return version;
	}
}