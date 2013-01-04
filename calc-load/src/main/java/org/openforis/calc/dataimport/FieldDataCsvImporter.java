package org.openforis.calc.dataimport;

import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openforis.calc.geospatial.GeodeticCoordinate;
import org.openforis.calc.model.ObservationUnit;
import org.openforis.calc.model.PlotSurvey;
import org.openforis.calc.model.SamplingDesignIdentifiers;
import org.openforis.calc.model.Survey;
import org.openforis.calc.persistence.ObservationUnitDao;
import org.openforis.calc.persistence.PlotSurveyDao;
import org.openforis.calc.persistence.SurveyDao;
import org.openforis.calc.service.MetadataService;
import org.openforis.calc.service.SamplingDesignService;
import org.openforis.calc.util.csv.CsvLine;
import org.openforis.calc.util.csv.CsvReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.transaction.annotation.Transactional;

/**
 * 
 * @author G. Miceli
 *
 */
public class FieldDataCsvImporter {
	
	// test - remove
	protected static final String PLOT_TEST_FILE = "/home/gino/workspace/tzdata/plots.csv";
	protected static final String TEST_URI = "http://www.openforis.org/idm/naforma1";
	
	@Autowired
	private MetadataService metadataService;
	@Autowired
	private SurveyDao surveyDao;
	@Autowired
	private PlotSurveyDao surveyedPlotDao;
	@Autowired
	private ObservationUnitDao observationUnitDao;
	@Autowired
	private SamplingDesignService samplingDesignService;

//	protected Survey survey;
	
	protected Log log = LogFactory.getLog(getClass());
	
	private int plotCount;
	private int duration;
	private int reportFrequency;
	private SamplingDesignIdentifiers samplingDesignIds;
	private int rowCount;
	private Set<String> plotIdents;
	
	public FieldDataCsvImporter() {
		reportFrequency = 10000;
	}
	
	public static void main(String[] args)  {
		try {
			ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("applicationContext.xml");
			FieldDataCsvImporter loader = ctx.getBean(FieldDataCsvImporter.class);
			loader.importPlotData(TEST_URI, PLOT_TEST_FILE);
		} catch ( Throwable ex ) {
			ex.printStackTrace();
		}
	}

	@Transactional
	synchronized
	public void importPlotData(String uri, String filename) throws ImportException, IOException {
		Survey survey = loadSurvey(uri);
		Integer surveyId = survey.getId();
		ObservationUnit obsUnit = loadObservationUnit(surveyId);
		samplingDesignIds = samplingDesignService.loadGroundPlotIds(surveyId);
		importPlotData(surveyId, obsUnit.getId(), filename);
        log.info("Imported "+plotCount+" field plots in "+duration/1000.0+"s");
	}

	private ObservationUnit loadObservationUnit(Integer surveyId) throws ImportException {
		ObservationUnit obsUnit = observationUnitDao.find(surveyId, "plot", "plot");
		if ( obsUnit == null ) {
			throw new ImportException("No observation unit 'plot' defined in database");			
		}
		return obsUnit;
	}

	private Survey loadSurvey(String uri) throws ImportException {
		Survey survey = surveyDao.findByUri(uri);
		if ( survey == null ) {
			throw new ImportException("No survey with URI "+uri);
		}
		return survey;
	}

	@Transactional
	public void importPlotData(int surveyId, int obsUnitId, String filename) throws ImportException, IOException {
		CsvReader reader = null;
		try {
			long start = System.currentTimeMillis();
			plotCount = 0;
			rowCount = 0;
			FileReader fileReader = new FileReader(filename);
			reader = new CsvReader(fileReader);
			reader.readHeaderLine();
			CsvLine line;
			plotIdents = new HashSet<String>();
			while ((line = reader.readNextLine()) != null) {
				rowCount += 1;
				if ( rowCount % reportFrequency == 0 ) {
					log.info(rowCount+" rows read, "+plotCount+" plots inserted.");
				}
				PlotSurvey plotSurvey = insertPlotSurvey(line, surveyId, obsUnitId);
				if ( plotSurvey != null ) {
					insertPlotValues(line);
				}
		    }
			duration = (int) (System.currentTimeMillis() - start);
		} catch ( Exception ex ) {
			throw new ImportException("Error importing row", ex);
		} finally {
			if ( reader != null ) {
				reader.close();
			}
		}
	}
	
	private PlotSurvey insertPlotSurvey(CsvLine line, int surveyId, int obsUnitId) throws ImportException {
		String plotIdent = null;
		try {
			plotIdent = null;
			String clusterCode = line.getString("cluster_code");
			Integer plotNo = line.getInteger("plot_no");
			String plotSection = line.getString("plot_section");
			String surveyType = line.getString("survey_type");

			plotIdent = clusterCode + " "+plotNo + plotSection+" ("+surveyType+")";
			Integer clusterNo = line.getInteger("cluster_no");
			Date surveyDate = line.getDate("survey_date");
			String plotCode = line.getString("plot_code");
			Double gpsX = line.getDouble("gps_reading_x");
			Double gpsY = line.getDouble("gps_reading_y");
			String gpsSrs = line.getString("gps_reading_srs_id");
			Integer step = line.getInteger("step");
			Boolean accessible = line.getBoolean("accessible");
			if ( plotIdents.contains(plotIdent) ) {
				log.warn("Skipping duplicate plot "+plotIdent+" at row "+rowCount);
				return null;
			}
			
			Integer clusterId =  samplingDesignIds.getClusterIds().getId(clusterCode, clusterNo);
			Integer plotId = samplingDesignIds.getPlotIds().getId(clusterId, plotCode, plotNo);
			if ( plotId == null ) {
				log.warn("Skipping unrecognized plot "+plotIdent+" at row "+rowCount);
				return null;
			}
	
			Integer sectionNo = getPlotSectionNo(plotSection);
			GeodeticCoordinate gpsReading = GeodeticCoordinate.toInstance(gpsX, gpsY, gpsSrs);
			if ( gpsReading == null ) {
				log.warn("Skipping plot with invalid gps_reading: "+plotIdent+" at row "+rowCount);
				return null;
			}
			PlotSurvey p = new PlotSurvey();
			p.setSurveyId(surveyId);
			p.setPlotId(plotId);
			p.setSectionNo(sectionNo);
			p.setSurveyDate(surveyDate);
			p.setSurveyType(surveyType);
			p.setStep(step);
			p.setGpsReading(gpsReading.toPGGeometry());
			p.setLocation(gpsReading.toPGGeometry()); // TODO correct location
			p.setObsUnitId(obsUnitId);
			p.setAccessible(accessible);
			plotCount += 1;
			surveyedPlotDao.insert(p);
			plotIdents.add(plotIdent);
			return p;
		} catch (ParseException p) {
			log.warn("Skipping plot with invalid date "+plotIdent+" at row "+rowCount);
			return null;
		} catch (NumberFormatException p) {
			log.warn("Skipping plot with invalid number "+plotIdent+" at row "+rowCount);
			return null;
		}
	}
	
	private void insertPlotValues(CsvLine line) {
		List<String> colnames = line.getColumnNames();
		for (String col : colnames) {
			
		}
	}

	private Integer getPlotSectionNo(String section) throws ImportException {
		if ( section == null || section.isEmpty() ) {
			return 1;
		} else if ( section.matches("[0-9]+") ) {
			return Integer.valueOf(section);
		} else if ( section.matches("[A-Za-z]") ) {
			// convert A, B, C.. to 1, 2, 3..
			return section.toUpperCase().charAt(0) - 64;
		} else {
			throw new ImportException("Invalid plot section '"+section+"'");
		}
	}

	public int getPlotCount() {
		return plotCount;
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
	
	public int getRowCount() {
		return rowCount;
	}
}