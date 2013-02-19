package org.openforis.calc.dataimport;

import java.awt.geom.Point2D;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openforis.calc.geospatial.TransformationUtils;
import org.openforis.calc.io.flat.FlatDataStream;
import org.openforis.calc.io.flat.FlatRecord;
import org.openforis.calc.model.Cluster;
import org.openforis.calc.model.SamplePlot;
import org.openforis.calc.model.Stratum;
import org.openforis.calc.persistence.ClusterDao;
import org.openforis.calc.persistence.SamplePlotDao;
import org.openforis.calc.persistence.StratumDao;
import org.postgis.PGgeometry;
import org.postgis.Point;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 
 * @author G. Miceli
 * @author Mino Togna
 * 
 */
@Component
public class SamplingDesignImporter {

	protected Log log = LogFactory.getLog(getClass());

	private int clusterCount;
	private int stratumCount;
	private int plotCount;
	private int groundPlotCount;
	private int permanentPlotCount;
	private int duration;
	private int reportFrequency;

	private int insertFrequency;
	
	@Autowired
	private ClusterDao clusterDao;

	@Autowired
	private StratumDao stratumDao;

	@Autowired
	private SamplePlotDao samplePlotDao;

	public SamplingDesignImporter() {
		reportFrequency = 10000;
		insertFrequency = 10000;
	}

	
	@Transactional
	synchronized
	public void importSamplingDesign(int surveyId, int unitId,String srsId, FlatDataStream stream) throws ImportException, IOException {
		try {
			long start = System.currentTimeMillis();
			clusterCount = 0;
			stratumCount = 0;
			plotCount = 0;
			groundPlotCount = 0;
			permanentPlotCount = 0;

			Map<Integer, Integer> stratumIds = new HashMap<Integer, Integer>();
			Map<String, Integer> clusterIds = new HashMap<String, Integer>();
			List<Cluster> newClusters = new ArrayList<Cluster>();
			List<Stratum> newStrata = new ArrayList<Stratum>();
			List<SamplePlot> newSamplePlots = new ArrayList<SamplePlot>();
			
			FlatRecord record;
			while ( (record = stream.nextRecord()) != null ) {
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

				// Projection UTM Zone 36 Hemisphere S Datum WGS84
				Point2D pos = TransformationUtils.toLatLong(plotX, plotY, srsId);
				Point point = new Point(pos.getX(), pos.getY());
				point.setSrid(4326);
				PGgeometry geom = new PGgeometry(point);
				p.setPlotLocation(geom);
				// p.setLocation("ST_SetSRID(ST_MakePoint("+plotX+", "+plotY+"), 4326)");
				p.setGroundPlot(groundPlot);
				p.setPermanentPlot(permanentPlot);
				newSamplePlots.add(p);
				plotCount += 1;
				if ( plotCount % reportFrequency == 0 ) {
					log.info(plotCount + " plots inserted.");
				}
				if ( plotCount % insertFrequency == 0 ) {
					batchStore(newStrata, newClusters, newSamplePlots);
				}
			}

			if ( plotCount % insertFrequency != 0 ) {
				batchStore(newStrata, newClusters, newSamplePlots);
				log.info(plotCount + " plots inserted.");
			}
			duration = (int) (System.currentTimeMillis() - start);
		} finally {
		}
	}

	private void batchStore(List<Stratum> newStrata, List<Cluster> newClusters, List<SamplePlot> newSamplePlots) {
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

	public int getDuration() {
		return duration;
	}

	public int getReportFrequency() {
		return reportFrequency;
	}

	public void setReportFrequency(int reportFrequency) {
		this.reportFrequency = reportFrequency;
	}

	public int getInsertFrequency() {
		return insertFrequency;
	}

	public void setInsertFrequency(int insertFrequency) {
		this.insertFrequency = insertFrequency;
	}
}
