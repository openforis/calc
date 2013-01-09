package org.openforis.calc.dataimport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.openforis.calc.geospatial.GeodeticCoordinate;
import org.openforis.calc.io.csv.CsvLine;
import org.openforis.calc.io.csv.CsvReader;
import org.openforis.calc.io.csv.DateFormatException;
import org.openforis.calc.model.Category;
import org.openforis.calc.model.ObservationUnit;
import org.openforis.calc.model.PlotCategory;
import org.openforis.calc.model.PlotMeasurement;
import org.openforis.calc.model.PlotSection;
import org.openforis.calc.model.SamplePlot;
import org.openforis.calc.model.Variable;
import org.openforis.calc.persistence.PlotCategoryDao;
import org.openforis.calc.persistence.PlotMeasurementDao;
import org.openforis.calc.persistence.PlotSectionDao;
import org.openforis.calc.service.SamplingDesignService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Component;

/**
 * 
 * @author G. Miceli
 *
 */
@Component
public class PlotDataCsvImporter extends AbstractFieldDataCsvImporter {
	
	// test - remove
	private static final String PLOT_TEST_FILE = "/home/gino/workspace/tzdata/plots.csv";
	private static final String TEST_URI = "http://www.openforis.org/idm/naforma1";
	private static final String TEST_UNIT = "plot";

	private Set<String> plotIdents;
	
	@Autowired
	private PlotSectionDao surveyedPlotDao;
	@Autowired
	private SamplingDesignService samplingDesignService;
	@Autowired
	private PlotMeasurementDao plotMeasurementDao;
	@Autowired
	private PlotCategoryDao plotCategoryDao;

	private List<Variable> vars;

	public static void main(String[] args)  {
		try {
			ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("applicationContext.xml");
			PlotDataCsvImporter loader = ctx.getBean(PlotDataCsvImporter.class);
			loader.doImport(TEST_URI, TEST_UNIT, PLOT_TEST_FILE);
		} catch ( Throwable ex ) {
			ex.printStackTrace();
		}
	}

	@Override
	protected void beforeImport(CsvReader reader) throws ImportException, IOException {
		log.info("Loading sampling design...");
		samplingDesignService.loadSamplingDesign(getSurvey());
		plotIdents = new HashSet<String>();
		vars = getVariables(reader.getColumnNames());
		log.info("Importing...");
	}

	@Override
	protected void processLine(CsvLine line) throws ImportException, IOException {
		PlotSection section = insertPlotSection(line);
		if ( section != null ) {
			insertPlotValues(section, line, vars);
		}
	}

	private PlotSection insertPlotSection(CsvLine line) throws ImportException {
		ObservationUnit unit = getObservationUnit();
		String plotIdent = null;
		try {
			plotIdent = null;

			String clusterCode = line.getString("cluster_code");
			Integer plotNo = line.getInteger("plot_no");
			String plotSection = line.getString("plot_section");
			String visitType = line.getString("visit_type");
			if ( plotNo == null ) {
				log.warn("Skipping plot with missing plot_no at row "+getRowCount());
				return null;				
			}
			
			plotIdent = getPlotIdentifer(clusterCode, plotNo, plotSection, visitType);
			Date surveyDate = line.getDate("survey_date");
			Double gpsX = line.getDouble("gps_reading_x");
			Double gpsY = line.getDouble("gps_reading_y");
			String gpsSrs = line.getString("gps_reading_srs_id");
			Integer step = line.getInteger("step");
			Double percentShare = line.getDouble("percent_share");
			Double direction = line.getDouble("center_direction");
			Double distance = line.getDouble("center_distance");
			Boolean accessible = line.getBoolean("accessible");
			if ( plotIdents.contains(plotIdent) ) {
				log.warn("Skipping duplicate plot "+plotIdent+" at row "+getRowCount());
				return null;
			}
			SamplePlot splot = unit.getGroundPlot(clusterCode, plotNo);
			if ( splot == null ) {
				log.warn("Skipping unrecognized plot "+plotIdent+" at row "+getRowCount());
				return null;
			}
			Integer plotId = splot.getId();

			GeodeticCoordinate gpsReading = GeodeticCoordinate.toInstance(gpsX, gpsY, gpsSrs);
			if ( gpsReading == null ) {
				log.warn("Skipping plot with invalid gps_reading: "+plotIdent+" at row "+getRowCount());
				return null;
			}
			PlotSection p = new PlotSection();
			p.setSamplePlotId(plotId);
			p.setSection(plotSection);
			p.setSurveyDate(surveyDate);
			p.setVisitType(visitType);
			p.setStep(step);
			p.setGpsReading(gpsReading.toPGGeometry());
			p.setLocation(gpsReading.toPGGeometry()); // TODO correct location
			p.setAccessible(accessible);
			p.setPercentShare(percentShare);
			p.setDirection(direction);
			p.setDistance(distance);
			incrementInsertCount();
			surveyedPlotDao.insert(p);
			plotIdents.add(plotIdent);
			return p;
		} catch (DateFormatException p) {
			log.warn("Skipping plot with invalid date "+plotIdent+" at row "+getRowCount());
			return null;
		} catch (NumberFormatException p) {
			log.warn("Skipping plot with invalid number "+plotIdent+" at row "+getRowCount());
			return null;
		}
	}

	private void insertPlotValues(PlotSection section, CsvLine line, List<Variable> vars) {
		List<PlotMeasurement> pms = new ArrayList<PlotMeasurement>();
		List<PlotCategory> pcs = new ArrayList<PlotCategory>();
		for (Variable var : vars) {
			String name = var.getName();
			if ( var.isNumeric() ) {
				Double value = line.getDouble(name);
				if ( value != null ) {
					PlotMeasurement pm = new PlotMeasurement(section, var, value, false);
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
						PlotCategory pc = new PlotCategory(section, cat, false);
						pcs.add(pc);
					}
				}
			}
		}		
		plotMeasurementDao.insert(pms);
		plotCategoryDao.insert(pcs);
	}
}