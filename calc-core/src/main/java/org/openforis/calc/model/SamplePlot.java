package org.openforis.calc.model;

/**
 * @author G. Miceli
 */
public class SamplePlot extends org.openforis.calc.persistence.jooq.tables.pojos.SamplePlot implements Identifiable {

	private static final long serialVersionUID = 1L;

	private Cluster cluster;
	private ObservationUnit observationUnit;

	public ObservationUnit getObservationUnit() {
		return observationUnit;
	}

	void setObservationUnit(ObservationUnit observationUnit) {
		this.observationUnit = observationUnit;
	}

	void setCluster(Cluster cluster) {
		this.cluster = cluster;
	}

	public Cluster getCluster() {
		return cluster;
	}

	@Override
	public Integer getId() {
		return getSamplePlotId();
	}

	@Override
	public void setId(Integer id) {
		super.setSamplePlotId(id);
	}
	
}
