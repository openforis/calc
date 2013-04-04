/**
 * 
 */
package org.openforis.calc.importer;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.openforis.calc.geospatial.GeodeticCoordinate;
import org.openforis.calc.model.PlotCategoricalValue;
import org.openforis.calc.model.PlotNumericValue;
import org.openforis.calc.model.PlotSection;
import org.openforis.calc.persistence.PlotCategoricalValueDao;
import org.openforis.calc.persistence.PlotNumericVariableDao;
import org.openforis.calc.persistence.PlotSectionDao;
import org.openforis.calc.persistence.SamplePlotViewDao;
import org.openforis.commons.io.flat.FlatRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author M. Togna
 * 
 */
@Component
public class PlotSectionImporter extends AbstractObservationImporter<PlotSection, PlotNumericValue, PlotCategoricalValue> {

	@Autowired
	private PlotSectionDao plotSectionDao;
	@Autowired
	private PlotNumericVariableDao plotNumericVariableDao;
	@Autowired
	private PlotCategoricalValueDao plotCategoricalValueDao;
	@Autowired
	private SamplePlotViewDao samplePlotViewDao;

	private Set<String> plotIdents;

	public PlotSectionImporter() {
		super(PlotNumericValue.class, PlotCategoricalValue.class);
		setInsertFrequency(2000);
		setReportFrequency(2000);
	}

	@Override
	protected PlotSection processObservation(FlatRecord record) {
		int readRows = getReadRows();

		String clusterCode = record.getValue("cluster_code", String.class);
		Integer plotNo = record.getValue("plot_no", Integer.class);
		String plotSection = record.getValue("plot_section", String.class);
		String visitType = record.getValue("visit_type", String.class);
		if ( plotNo == null ) {
			log.warn("Skipping plot with missing plot_no at row " + readRows);
			return null;
		}

		String plotIdent = PlotSection.getPlotIdentifer(clusterCode, plotNo, plotSection, visitType);

		Date surveyDate = record.getValue("survey_date", Date.class);
		Double gpsX = record.getValue("gps_reading_x", Double.class);
		Double gpsY = record.getValue("gps_reading_y", Double.class);
		String gpsSrs = record.getValue("gps_reading_srs_id", String.class);
		Integer step = record.getValue("step", Integer.class);
		Double share = record.getValue("percent_share", Double.class);
		Double direction = record.getValue("center_direction", Double.class);
		Double distance = record.getValue("center_distance", Double.class);
		Boolean accessible = record.getValue("accessible", Boolean.class);

		if ( plotIdents.contains(plotIdent) ) {
			log.warn("Skipping duplicate plot " + plotIdent + " at row " + readRows);
			return null;
		}

		Integer obsUnitId = getObservationUnitMetadata().getObsUnitId();
		Integer samplePlotId = samplePlotViewDao.getId(obsUnitId, clusterCode, plotNo);

		if ( samplePlotId == null ) {
			log.warn("Skipping. Sample Plot id not found: " + plotIdent + "at row " + readRows);
			return null;
		}

		GeodeticCoordinate gpsReading = GeodeticCoordinate.toInstance(gpsX, gpsY, gpsSrs);
		if ( gpsReading == null ) {
			log.warn("Skipping plot with invalid gps_reading: " + plotIdent + " at row " + readRows);
			return null;
		}

		Integer plotSectionId = plotSectionDao.nextId();

		PlotSection ps = new PlotSection();
		ps.setPlotSectionId(plotSectionId);
		ps.setSamplePlotId(samplePlotId);
		ps.setPlotSection(plotSection);
		ps.setSurveyDate(surveyDate);
		ps.setVisitType(visitType);
		ps.setStep(step);
		ps.setPlotGpsReading(gpsReading);
		ps.setPlotActualLocation(gpsReading);
		ps.setAccessible(accessible);
		ps.setPlotShare(share);
		ps.setPlotDirection(direction);
		ps.setPlotDistance(distance);
		ps.setClean(true);
		ps.setPrimarySection("A".equals(plotSection));
		
		plotIdents.add(plotIdent);

		return ps;
	}

	@Override
	protected void doInserts(List<PlotSection> obs, List<PlotNumericValue> numVals, List<PlotCategoricalValue> catVals) {
		plotSectionDao.insert(obs);
		plotNumericVariableDao.insert(numVals);
		plotCategoricalValueDao.insert(catVals);
	}

	@Override
	protected void onStart() {
		plotIdents = new HashSet<String>();
	}

}
