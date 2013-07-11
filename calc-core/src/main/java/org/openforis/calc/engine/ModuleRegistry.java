package org.openforis.calc.engine;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.springframework.stereotype.Repository;

/**
 * Responsible for finding Modules on the classpath and instantiating them.
 * 
 * @author G. Miceli
 * @author M. Togna
 */
@Repository
public class ModuleRegistry {
	private Map<String, Module> modules;

	public ModuleRegistry() {
		this.modules = new HashMap<String, Module>();
	}
	
	public Set<Module> getModules() {
		Set<Module> m = new HashSet<Module>(modules.values());
		return Collections.unmodifiableSet(m);
	}

	public Module getModule(String name, String version) {
		String key = moduleKey(name, version);
		return modules.get(key);
	}

	public void registerModule(Module module) {
		String key = moduleKey(module);
		modules.put(key,  module);
	}

	public void unregisterModule(Module module) {
		String key = moduleKey(module);
		modules.remove(key);
	}

	/**
	 * Reload all Modules in classpath using {@link ServiceLoader}. All other
	 * modules will be unregistered.
	 */
	@PostConstruct
	public void reloadModules() {
		ServiceLoader<Module> serviceLoader = ServiceLoader.load(Module.class);
		modules.clear();
		for (Module module : serviceLoader) {
			registerModule(module);
		}
	}

	private String moduleKey(String name, String version) {
		return String.format("%s-%s", name, version);
	}

	private String moduleKey(Module module) {
		return moduleKey(module.getName(), module.getVersion());
	}

	/**
	 * Convenient method to get Operation directly 
	 * 
	 * @param step
	 * @return
	 */
	public Operation<?> getOperation(String moduleName, String moduleVersion, String operationName) {
		Module module = getModule(moduleName, moduleVersion);
		if ( module == null ) {
			return null;
		}
		Operation<?> operation = module.getOperation(operationName);
		return operation;
	}

	/**
	 * Convenience method to get Operation directly 
	 * 
	 * @param step
	 * @return
	 */
	public Operation<?> getOperation(CalculationStep step) {
		return getOperation(step.getModuleName(), step.getModuleVersion(), step.getOperationName());
	}

}