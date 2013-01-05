package org.openforis.calc.dataimport;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.openforis.calc.geospatial.GeodeticCoordinate;
import org.openforis.calc.model.Category;
import org.openforis.calc.model.ObservationUnit;
import org.openforis.calc.model.PlotCategory;
import org.openforis.calc.model.PlotMeasurement;
import org.openforis.calc.model.PlotSurvey;
import org.openforis.calc.model.SamplingDesignIdentifiers;
import org.openforis.calc.model.Variable;
import org.openforis.calc.persistence.PlotCategoryDao;
import org.openforis.calc.persistence.PlotMeasurementDao;
import org.openforis.calc.persistence.PlotSurveyDao;
import org.openforis.calc.service.SamplingDesignService;
import org.openforis.calc.util.csv.CsvLine;
import org.openforis.calc.util.csv.CsvReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * 
 * @author G. Miceli
 *
 */
public class PlotDataCsvImporter extends AbstractFieldDataCsvImporter {
	
	// test - remove
	protected static final String PLOT_TEST_FILE = "/home/gino/workspace/tzdata/plots.csv";
	protected static final String TEST_URI = "http://www.openforis.org/idm/naforma1";

	private SamplingDesignIdentifiers samplingDesignIds;
	private Set<String> plotIdents;
	
	@Autowired
	private PlotSurveyDao surveyedPlotDao;
	@Autowired
	private SamplingDesignService samplingDesignService;
	@Autowired
	private PlotMeasurementDao plotMeasurementDao;
	@Autowired
	private PlotCategoryDao plotCategoryDao;
	
	private List<Variable> vars;
	private ObservationUnit obsUnit;

	public static void main(String[] args)  {
		try {
			ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("applicationContext.xml");
			PlotDataCsvImporter loader = ctx.getBean(PlotDataCsvImporter.class);
			loader.doImport(TEST_URI, PLOT_TEST_FILE);
		} catch ( Throwable ex ) {
			ex.printStackTrace();
		}
	}

	@Override
	protected void beforeImport(CsvReader reader) throws ImportException, IOException {
		Integer surveyId = getSurvey().getId();
		obsUnit = loadObservationUnitMetadata(surveyId);
		samplingDesignIds = samplingDesignService.loadGroundPlotIds(surveyId);
		vars = getVariables(reader.getColumnNames(), obsUnit);
		plotIdents = new HashSet<String>();
	}

	@Override
	protected void processLine(CsvLine line) throws ImportException, IOException {
		PlotSurvey plotSurvey = insertPlotSurvey(line, obsUnit);
		if ( plotSurvey != null ) {
			insertPlotValues(plotSurvey, obsUnit, line, vars);
		}
	}

	private PlotSurvey insertPlotSurvey(CsvLine line, ObservationUnit unit) throws ImportException {
		String plotIdent = null;
		try {
			plotIdent = null;

			String clusterCode = line.getString("cluster_code");
			Integer plotNo = line.getInteger("plot_no");
			String plotSection = line.getString("plot_section");
			String surveyType = line.getString("survey_type");

			plotIdent = getPlotIdentifer(clusterCode, plotNo, plotSection, surveyType);
			Integer clusterNo = line.getInteger("cluster_no");
			Date surveyDate = line.getDate("survey_date");
			String plotCode = line.getString("plot_code");
			Double gpsX = line.getDouble("gps_reading_x");
			Double gpsY = line.getDouble("gps_reading_y");
			String gpsSrs = line.getString("gps_reading_srs_id");
			Integer step = line.getInteger("step");
			Boolean accessible = line.getBoolean("accessible");
			if ( plotIdents.contains(plotIdent) ) {
				log.warn("Skipping duplicate plot "+plotIdent+" at row "+getRowCount());
				return null;
			}
			
			Integer clusterId =  samplingDesignIds.getClusterIds().getId(clusterCode, clusterNo);
			Integer plotId = samplingDesignIds.getPlotIds().getId(clusterId, plotCode, plotNo);
			if ( plotId == null ) {
				log.warn("Skipping unrecognized plot "+plotIdent+" at row "+getRowCount());
				return null;
			}
	
			Integer sectionNo = parsePlotSectionNo(plotSection);
			GeodeticCoordinate gpsReading = GeodeticCoordinate.toInstance(gpsX, gpsY, gpsSrs);
			if ( gpsReading == null ) {
				log.warn("Skipping plot with invalid gps_reading: "+plotIdent+" at row "+getRowCount());
				return null;
			}
			PlotSurvey p = new PlotSurvey();
			p.setSurveyId(unit.getSurveyId());
			p.setPlotId(plotId);
			p.setSectionNo(sectionNo);
			p.setSurveyDate(surveyDate);
			p.setSurveyType(surveyType);
			p.setStep(step);
			p.setGpsReading(gpsReading.toPGGeometry());
			p.setLocation(gpsReading.toPGGeometry()); // TODO correct location
			p.setObsUnitId(unit.getId());
			p.setAccessible(accessible);			
			incrementInsertCount();
			surveyedPlotDao.insert(p);
			plotIdents.add(plotIdent);
			return p;
		} catch (ParseException p) {
			log.warn("Skipping plot with invalid date "+plotIdent+" at row "+getRowCount());
			return null;
		} catch (NumberFormatException p) {
			log.warn("Skipping plot with invalid number "+plotIdent+" at row "+getRowCount());
			return null;
		}
	}

	private void insertPlotValues(PlotSurvey plotSurvey, ObservationUnit unit, CsvLine line, List<Variable> vars) {
		List<PlotMeasurement> pms = new ArrayList<PlotMeasurement>();
		List<PlotCategory> pcs = new ArrayList<PlotCategory>();
		for (Variable var : vars) {
			String name = var.getName();
			if ( var.isNumeric() ) {
				Double value = line.getDouble(name);
				if ( value != null ) {
					PlotMeasurement pm = new PlotMeasurement(plotSurvey, var, value, false);
					pms.add(pm);
				}
			}
			if ( var.isCategorical() ) {
				String code = line.getString(name);
				if ( code != null ) {
					// TODO doesn't work for categorical variables
					Category cat = var.getCategory(code);
					if ( cat == null ) {
						log.warn("Skipping unknown code "+code);
					} else {
						PlotCategory pc = new PlotCategory(plotSurvey, cat, false);
						pcs.add(pc);
					}
				}
			}
		}		
		plotMeasurementDao.insert(pms);
		plotCategoryDao.insert(pcs);
	}
}