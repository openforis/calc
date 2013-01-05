package org.openforis.calc.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openforis.calc.persistence.jooq.tables.pojos.ObservationUnitBase;

/**
 * @author G. Miceli
 */
public class ObservationUnit extends ObservationUnitBase implements ImportableModelObject {

	private static final long serialVersionUID = 1L;
	
	private Map<String, Variable> variableMap;
	
	public void setVariables(List<Variable> variables) {
		this.variableMap = new HashMap<String, Variable>();
		for (Variable var : variables) {
			variableMap.put(var.getName(), var);
		}
	}
	
	public Variable getVariable(String name) {
		if ( variableMap == null ) {
			throw new NullPointerException("variableMap not initialized");
		}
		return variableMap.get(name);
	}
}
