package org.openforis.calc.importer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openforis.calc.geospatial.GeodeticCoordinate;
import org.openforis.calc.io.flat.FlatRecord;
import org.openforis.calc.model.Cluster;
import org.openforis.calc.model.SamplePlot;
import org.openforis.calc.model.Stratum;
import org.openforis.calc.persistence.ClusterDao;
import org.openforis.calc.persistence.SamplePlotDao;
import org.openforis.calc.persistence.StratumDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Not thread-safe, callers must be synchronized
 * 
 * @author G. Miceli
 * @author M. Togna
 * 
 */
@Component
public class SamplingDesignImporter extends AbstractImporter {

	private int clusterCount;
	private int stratumCount;
	private int plotCount;
	private int groundPlotCount;
	private int permanentPlotCount;
	private Map<Integer, Integer> stratumIds;
	private Map<String, Integer> clusterIds;
	private List<Cluster> newClusters;
	private List<Stratum> newStrata;
	private List<SamplePlot> newSamplePlots;
	private Integer surveyId;
	private Integer unitId;
	private String srsId;
	
	@Autowired
	private ClusterDao clusterDao;

	@Autowired
	private StratumDao stratumDao;

	@Autowired
	private SamplePlotDao samplePlotDao;

	@Override
	protected void processRow(FlatRecord record) {
		if ( surveyId == null || unitId == null || srsId == null ) {
			throw new NullPointerException("Survey, unit and/or SRS not set");
		}
		Integer stratumNo = record.getValue("stratum_no", Integer.class);
		Integer clusterX = record.getValue("cluster_x", Integer.class);
		Integer clusterY = record.getValue("cluster_y", Integer.class);
		Integer clusterNo = record.getValue("cluster_no", Integer.class);
		String clusterCode = record.getValue("cluster_code", String.class);
		Integer plotNo = record.getValue("plot_no", Integer.class);
		Integer plotX = record.getValue("plot_x", Integer.class);
		Integer plotY = record.getValue("plot_y", Integer.class);
		Integer phase = record.getValue("phase", Integer.class);
		Boolean groundPlot = record.getValue("ground_plot", Boolean.class);
		Boolean permanentPlot = record.getValue("permanent_plot", Boolean.class);

		Integer stratumId = stratumIds.get(stratumNo);
		if ( stratumId == null ) {
			stratumCount += 1;
			Stratum str = new Stratum();
			stratumId = stratumDao.nextId();
			str.setSurveyId(surveyId);
			str.setStratumNo(stratumNo);
			str.setId(stratumId);
			newStrata.add(str);
			stratumIds.put(stratumNo, stratumId);
		}

		Integer clusterId = clusterIds.get(clusterCode);
		if ( clusterId == null ) {
			Cluster c = new Cluster();
			clusterId = clusterDao.nextId();
			c.setClusterId(clusterId);
			c.setSurveyId(surveyId);
			c.setClusterCode(nvl(clusterCode, clusterNo));
			c.setClusterX(clusterX);
			c.setClusterY(clusterY);
			newClusters.add(c);
			clusterIds.put(clusterCode, clusterId);
			clusterCount += 1;
		}

		if ( groundPlot ) {
			groundPlotCount += 1;
		}

		if ( permanentPlot ) {
			permanentPlotCount += 1;
		}
		
		SamplePlot p = new SamplePlot();				
		p.setStratumId(stratumId);
		p.setClusterId(clusterId);
		p.setPlotNo(plotNo);
		p.setSamplingPhase(phase);
		p.setObsUnitId(unitId);
		GeodeticCoordinate plotLocation = GeodeticCoordinate.toInstance(plotX, plotY, srsId);
		p.setPlotLocation(plotLocation);
		p.setGroundPlot(groundPlot);
		p.setPermanentPlot(permanentPlot);
		newSamplePlots.add(p);
		plotCount += 1;
	}
	
	@Override
	protected void performInserts() {
		batchInsert(newStrata, newClusters, newSamplePlots);
	}

	@Override
	protected void onStart() {
		clusterCount = 0;
		stratumCount = 0;
		plotCount = 0;
		groundPlotCount = 0;
		permanentPlotCount = 0;
		
		stratumIds = new HashMap<Integer, Integer>();
		clusterIds = new HashMap<String, Integer>();
		newClusters = new ArrayList<Cluster>();
		newStrata = new ArrayList<Stratum>();
		newSamplePlots = new ArrayList<SamplePlot>();
	}

	@Override
	protected void cleanup() {
		stratumIds = null;
		clusterIds = null;
		newClusters = null;
		newStrata = null;
		newSamplePlots = null;
	}

	private void batchInsert(List<Stratum> newStrata, List<Cluster> newClusters, List<SamplePlot> newSamplePlots) {
		stratumDao.insert(newStrata);
		clusterDao.insert(newClusters);
		samplePlotDao.insert(newSamplePlots);
		newStrata.clear();
		newClusters.clear();
		newSamplePlots.clear();
	}

	private String nvl(String code, Integer no) {
		return code == null ? no + "" : code;
	}

	public int getPlotCount() {
		return plotCount;
	}

	public int getPermanentPlotCount() {
		return permanentPlotCount;
	}

	public int getGroundPlotCount() {
		return groundPlotCount;
	}

	public int getStratumCount() {
		return stratumCount;
	}

	public int getClusterCount() {
		return clusterCount;
	}

	public int getSurveyId() {
		return surveyId;
	}

	public void setSurveyId(int surveyId) {
		this.surveyId = surveyId;
	}

	public int getUnitId() {
		return unitId;
	}

	public void setUnitId(int unitId) {
		this.unitId = unitId;
	}

	public String getSrsId() {
		return srsId;
	}

	public void setSrsId(String srsId) {
		this.srsId = srsId;
	}
}
