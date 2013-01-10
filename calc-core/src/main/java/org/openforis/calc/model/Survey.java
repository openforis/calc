package org.openforis.calc.model;


/**
 * @author G. Miceli
 */
public class Survey extends org.openforis.calc.persistence.jooq.tables.pojos.Survey implements Identifiable {

	private static final long serialVersionUID = 1L;

//	private Map<Integer, Cluster> clustersById;
//	private Map<String, Cluster> clustersByCode;
//
//	private void setClusters(Collection<Cluster> clusters) {
//		clustersById = new LinkedHashMap<Integer, Cluster>();
//		clustersByCode = new LinkedHashMap<String, Cluster>();
//		for ( Cluster cluster : clusters ) {
//			clustersById.put(cluster.getId(), cluster);
//			clustersByCode.put(cluster.getClusterCode(), cluster);
//		}
//	}
//
//	public Cluster getClusterById(int id) {
//		return clustersById.get(id);
//	}
//
//	public Cluster getClusterByCode(String code) {
//		return clustersByCode.get(code);
//	}
//
//	private void setGroundPlots(Collection<SamplePlot> groundPlots) {
//		for ( SamplePlot plot : groundPlots ) {
//			Integer clusterId = plot.getClusterId();
//			if ( clusterId != null ) {
//				Cluster cluster = getClusterById(clusterId);
//				plot.setCluster(cluster);
//			}
//			ObservationUnit ou = getObservationUnitById(plot.getObsUnitId());
//			plot.setObservationUnit(ou);
//			ou.addGroundPlot(plot);
//		}
//	}
//
//	private void setStrata(Collection<Stratum> strata) {
//		// TODO Auto-generated method stub
//
//	}
//
//	public void setSamplingDesign(Collection<Cluster> clusters, Collection<Stratum> strata, Collection<SamplePlot> groundPlots) {
//		if ( observationUnitsById == null ) {
//			throw new NullPointerException("Observation units metadata must be set first");
//		}
//		setClusters(clusters);
//		setStrata(strata);
//		setGroundPlots(groundPlots);
//	}
//
	@Override
	public Integer getId() {
		return super.getSurveyId();
	}

	@Override
	public void setId(Integer id) {
		super.setSurveyId(id);
	}
}
