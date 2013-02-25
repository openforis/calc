package org.openforis.calc.model;


/**
 * @author G. Miceli
 * @author M. Togna 
 */
public class ObservationUnit extends org.openforis.calc.persistence.jooq.tables.pojos.ObservationUnit implements Identifiable {

	private static final long serialVersionUID = 1L;

	public enum Type {
		PLOT, SPECIMEN, INTERVIEW;
		
		public static Type get(String name) {
			return name == null ? null : Type.valueOf(name.toUpperCase());
		}
	}
	
//	private Map<String, SamplePlot> groundPlotMap;
//
//	void clearGroundPlots() {
//		groundPlotMap = new HashMap<String, SamplePlot>();
//	}
//
//	void addGroundPlot(SamplePlot plot) {
//		Cluster cluster = plot.getCluster();
//		String clusterCode = cluster == null ? null : cluster.getClusterCode();
//		String key = getPlotKey(clusterCode, plot.getPlotNo());
//		groundPlotMap.put(key, plot);
//	}
//
//	private String getPlotKey(String clusterCode, int plotNo) {
//		if ( clusterCode == null ) {
//			return Integer.toString(plotNo);
//		} else {
//			return clusterCode + "_" + plotNo;
//		}
//	}
//
//	public SamplePlot getGroundPlot(String clusterCode, int plotNo) {
//		String key = getPlotKey(clusterCode, plotNo);
//		return groundPlotMap.get(key);
//	}

	@Override
	public Integer getId() {
		return super.getObsUnitId();
	}

	@Override
	public void setId(Integer id) {
		super.setObsUnitId(id);
	}
	
	public ObservationUnit.Type getObsUnitTypeEnum() {
		return Type.get(getObsUnitType());
	}
	
	public boolean isPlot() {
		return getObsUnitTypeEnum() == Type.PLOT;
	}
	
	public boolean isSpecimen() {
		return getObsUnitTypeEnum() == Type.SPECIMEN;
	}

	public boolean isInterview() {
		return getObsUnitTypeEnum() == Type.INTERVIEW;
	}
}
