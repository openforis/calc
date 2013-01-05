package org.openforis.calc.model;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author G. Miceli
 */
public class ObservationUnit extends org.openforis.calc.persistence.jooq.tables.pojos.ObservationUnit implements Identifiable {

	private static final long serialVersionUID = 1L;
	
	private Map<String, Variable> variableMap;
	private Map<String, SamplePlot> groundPlotMap;
	
	public void setVariables(Collection<Variable> variables) {
		this.variableMap = new LinkedHashMap<String, Variable>();
		for (Variable var : variables) {
			variableMap.put(var.getName(), var);
		}
	}
	
	public Collection<Variable> getVariables() {
		return Collections.unmodifiableCollection(variableMap.values());
	}
	
	public Variable getVariable(String name) {
		if ( variableMap == null ) {
			throw new NullPointerException("variableMap not initialized");
		}
		return variableMap.get(name);
	}

	void clearGroundPlots () {
		groundPlotMap = new HashMap<String, SamplePlot>();		
	}

	void addGroundPlot(SamplePlot plot) {
		Cluster cluster = plot.getCluster();
		String clusterCode = cluster == null ? null : cluster.getCode();
		String key = getPlotKey(clusterCode, plot.getNo());
		groundPlotMap.put(key, plot);
	}
	
	private String getPlotKey(String clusterCode, int plotNo) {
		if ( clusterCode == null ) {
			return Integer.toString(plotNo);
		} else {
			return clusterCode+"_"+plotNo;
		}
	}

	public SamplePlot getGroundPlot(String clusterCode, int plotNo) {
		String key = getPlotKey(clusterCode, plotNo);
		return groundPlotMap.get(key);
	}
}
