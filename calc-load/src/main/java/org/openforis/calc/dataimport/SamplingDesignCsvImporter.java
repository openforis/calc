package org.openforis.calc.dataimport;

import java.awt.geom.Point2D;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openforis.calc.model.Cluster;
import org.openforis.calc.model.Plot;
import org.openforis.calc.model.Stratum;
import org.openforis.calc.model.Survey;
import org.openforis.calc.persistence.ClusterDao;
import org.openforis.calc.persistence.PlotDao;
import org.openforis.calc.persistence.StratumDao;
import org.openforis.calc.persistence.SurveyDao;
import org.openforis.calc.service.MetadataService;
import org.openforis.calc.util.TransformationUtils;
import org.openforis.calc.util.csv.CsvLine;
import org.openforis.calc.util.csv.CsvReader;
import org.postgis.PGgeometry;
import org.postgis.Point;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * 
 * @author G. Miceli
 *
 */
public class SamplingDesignCsvImporter {
	
	// test - remove
	protected static final String TEST_FILE = "/home/gino/workspace/tzdesign/sampling-design.csv";
	protected static final String TEST_URI = "http://www.openforis.org/idm/naforma1";
	
	@Autowired
	protected MetadataService metadataService;
	@Autowired
	protected SurveyDao surveyDao;
	@Autowired
	protected StratumDao stratumDao;
	@Autowired
	protected ClusterDao clusterDao;
	@Autowired
	protected PlotDao plotDao;
	
	protected Survey survey;
	
	protected Log log = LogFactory.getLog(getClass());
	
	private int clusterCount;
	private int stratumCount;
	private int plotCount;
	private int groundPlotCount;
	private int permanentPlotCount;
	private int duration;
	private int reportFrequency;
	
	public SamplingDesignCsvImporter() {
		reportFrequency = 10000;
	}
	
	public static void main(String[] args)  {
		try {
			ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("applicationContext.xml");
			SamplingDesignCsvImporter loader = ctx.getBean(SamplingDesignCsvImporter.class);
			loader.importSamplingDesign(TEST_URI, TEST_FILE);
		} catch ( Throwable ex ) {
			ex.printStackTrace();
		}
	}

	public void importSamplingDesign(String uri, String filename) throws ImportException, IOException {
		survey = surveyDao.fetchByUri(uri);
		if ( survey == null ) {
			throw new ImportException("No survey with URI "+uri);
		}
		importSamplingDesign(survey.getId(), filename);
        log.info("Imported "+stratumCount+" strata, "+clusterCount+" clusters, "+plotCount+" plots ("+groundPlotCount+" ground, "
        		+permanentPlotCount+" permanent) in "+duration/1000.0+"s");
	}


	public void importSamplingDesign(int surveyId, String filename) throws ImportException, IOException {
		CsvReader reader = null;
		try {
			long start = System.currentTimeMillis();
			clusterCount = 0;
			stratumCount = 0;
			plotCount = 0;
			groundPlotCount = 0;
			permanentPlotCount = 0;

			Map<Integer, Integer> stratumIds = new HashMap<Integer, Integer>();
			Map<String, Integer> clusterIds = new HashMap<String, Integer>();
			FileReader fileReader = new FileReader(filename);
			reader = new CsvReader(fileReader);
			reader.readHeaderLine();
			CsvLine line;
			while ((line = reader.readNextLine()) != null) {
				Integer stratumNo = line.getInteger("stratum_no");
				String stratumCode = line.getString("stratum_code");
				Integer clusterX = line.getInteger("cluster_x");
				Integer clusterY = line.getInteger("cluster_y");
				Integer clusterNo = line.getInteger("cluster_no");
				String clusterCode = line.getString("cluster_code");
				Integer plotNo = line.getInteger("plot_no");
				Integer plotX = line.getInteger("plot_x");
				Integer plotY = line.getInteger("plot_y");
				Integer phase = line.getInteger("phase");
				String plotCode = line.getString("plot_code");
				Boolean groundPlot = line.getBoolean("ground_plot");
				Boolean permanentPlot = line.getBoolean("permanent_plot");
				
				Integer stratumId = stratumIds.get(stratumNo);
				if ( stratumId == null ) {
					stratumCount += 1;
					Stratum str = new Stratum();
					str.setSurveyId(surveyId);
					str.setNo(stratumNo);
					str.setCode(nvl(stratumCode, stratumNo));
					stratumDao.insert(str);
					stratumIds.put(stratumNo, str.getId());
				}
				
				Integer clusterId = clusterIds.get(clusterCode);
				if ( clusterId == null ) {
					Cluster c = new Cluster();
					c.setSurveyId(surveyId);
					c.setNo(clusterNo);
					c.setCode(nvl(clusterCode, clusterNo));
					c.setXIndex(clusterX);
					c.setYIndex(clusterY);					
					clusterDao.insert(c);
					clusterIds.put(clusterCode, c.getId());
					clusterCount += 1;
				}
				
				if ( groundPlot ) {
					groundPlotCount += 1;
				}
				
				if ( permanentPlot ) {
					permanentPlotCount += 1;
				}
				Plot p = new Plot();
				p.setSurveyId(surveyId);
				p.setCode(plotCode);
				p.setNo(plotNo);
				p.setPhase(groundPlot ? 2 : 1);
				
				// Projection UTM Zone 36 Hemisphere S Datum WGS84 
				Point2D pos = TransformationUtils.toLatLong(plotX, plotY, "EPSG:32736");
				Point point = new Point(pos.getX(), pos.getY());
				point.setSrid(4326);
				PGgeometry geom = new PGgeometry(point);
				p.setLocation(geom);
//				p.setLocation("ST_SetSRID(ST_MakePoint("+plotX+", "+plotY+"), 4326)");
				p.setGroundPlot(groundPlot);
				p.setPermanentPlot(permanentPlot);
				plotDao.insert(p);
				plotCount += 1;
				if ( plotCount % reportFrequency == 0 ) {
					log.info(plotCount+" plots inserted.");
				}
		    }
			duration = (int) (System.currentTimeMillis() - start);
		} finally {
			if ( reader != null ) {
				reader.close();
			}
		}
	}

	private String nvl(String code, Integer no) {
		return code == null ? no+"" : code;
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
}