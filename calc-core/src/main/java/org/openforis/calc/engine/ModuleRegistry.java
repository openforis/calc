package org.openforis.calc.engine;

import java.util.Set;

/**
 * Responsible for finding Modules on the classpath and instantiating them.
 * 
 * @author G. Miceli
 * @author M. Togna
 */
public final class ModuleRegistry {

	public Set<Module> getModules() {
		throw new UnsupportedOperationException();
	}

	public Module getModule(String name, String version) {
		throw new UnsupportedOperationException();
	}

	public void registerModule(Module module) {
		throw new UnsupportedOperationException();
	}

	public void unregisterModule(Module module) {
		throw new UnsupportedOperationException();
	}

	/**
	 * Reload all Modules in classpath using ServiceLoader. All other modules will be unregistered.
	 */
	public void reloadModules() {
		throw new UnsupportedOperationException();
	}
}