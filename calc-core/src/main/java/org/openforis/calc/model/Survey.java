package org.openforis.calc.model;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;


/**
 * @author G. Miceli
 */
public class Survey extends org.openforis.calc.persistence.jooq.tables.pojos.Survey implements Identifiable {

	private static final long serialVersionUID = 1L;
	
	private Map<Integer, ObservationUnit> observationUnitsById;
	private Map<String, ObservationUnit> observationUnitsByName;
	private Map<Integer, Cluster> clustersById;
	private Map<String, Cluster> clustersByCode;
	
	public void setObservationUnits(Collection<ObservationUnit> ous) {
		observationUnitsById = new LinkedHashMap<Integer, ObservationUnit>();
		observationUnitsByName = new HashMap<String, ObservationUnit>(); 
		for (ObservationUnit ou : ous) {
			ou.clearGroundPlots();
			observationUnitsById.put(ou.getId(), ou);
			observationUnitsByName.put(ou.getName(), ou);
		}
	}
	
	private void setClusters(Collection<Cluster> clusters) {
		clustersById = new LinkedHashMap<Integer, Cluster>();
		clustersByCode = new LinkedHashMap<String, Cluster>();
		for (Cluster cluster : clusters) {
			clustersById.put(cluster.getId(), cluster);
			clustersByCode.put(cluster.getCode(), cluster);
		}
	}
	
	public Cluster getClusterById(int id) {
		return clustersById.get(id);
	}
	
	public Cluster getClusterByCode(String code) {
		return clustersByCode.get(code);
	}
	
	public ObservationUnit getObservationUnitByName(String name) {
		if ( observationUnitsById == null ) {
			throw new NullPointerException("observationUnits not initialized");
		}
		return observationUnitsByName.get(name);
	}

	public void setSamplingDesign(
			Collection<Cluster> clusters, 
			Collection<Stratum> strata, 
			Collection<SamplePlot> groundPlots) {
		if ( observationUnitsById == null ) {
			throw new NullPointerException("Observation units metadata must be set first");
		}
		setClusters(clusters);
		setStrata(strata);
		setGroundPlots(groundPlots);
	}

	private void setGroundPlots(Collection<SamplePlot> groundPlots) {
		for (SamplePlot plot : groundPlots) {
			Integer clusterId = plot.getClusterId();
			if ( clusterId != null ) {
				Cluster cluster = getClusterById(clusterId);
				plot.setCluster(cluster);
			}
			ObservationUnit ou = getObservationUnitById(plot.getObsUnitId());
			plot.setObservationUnit(ou);
			ou.addGroundPlot(plot);
		}
	}

	public ObservationUnit getObservationUnitById(int obsUnitId) {
		return observationUnitsById.get(obsUnitId);
	}

	private void setStrata(Collection<Stratum> strata) {
		// TODO Auto-generated method stub
		
	}
	
	public Collection<ObservationUnit> getObservationUnits() {
		return observationUnitsById == null ? null : Collections.unmodifiableCollection(observationUnitsById.values());
	}
}
