package org.openforis.calc.importer;

import java.util.ArrayList;
import java.util.List;

import org.openforis.calc.io.flat.FlatRecord;
import org.openforis.calc.model.Interview;
import org.openforis.calc.model.InterviewCategoricalValue;
import org.openforis.calc.model.InterviewNumericValue;
import org.openforis.calc.persistence.InterviewCategoricalValueDao;
import org.openforis.calc.persistence.InterviewDao;
import org.openforis.calc.persistence.InterviewNumericValueDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Not thread-safe, callers must manage synchronization
 * 
 * @author G. Miceli
 * 
 */
@Component
public class InterviewImporter extends AbstractFlatFileImporter {

	private Integer surveyId;
	private Integer unitId;
	private List<Interview> interviews;
	private List<InterviewCategoricalValue> categoricalValues;
	private List<InterviewNumericValue> numericValues;
	
	@Autowired
	private InterviewDao interviewDao;
	@Autowired
	private InterviewCategoricalValueDao interviewCategoricalValueDao;
	@Autowired
	private InterviewNumericValueDao interviewNumericValueDao;
	
	public InterviewImporter() {
		setInsertFrequency(1000);
		setReportFrequency(1000);
	}
	
	@Override
	protected void processRecord(FlatRecord record) {
		if ( surveyId == null || unitId == null ) {
			throw new NullPointerException("Survey or unit ID not set");
		}
		//TODO
//		Integer stratumNo = record.getValue("stratum_no", Integer.class);
//		Integer clusterX = record.getValue("cluster_x", Integer.class);
//		Integer clusterY = record.getValue("cluster_y", Integer.class);
//		Integer clusterNo = record.getValue("cluster_no", Integer.class);
//		String clusterCode = record.getValue("cluster_code", String.class);
//		Integer plotNo = record.getValue("plot_no", Integer.class);
//		Integer plotX = record.getValue("plot_x", Integer.class);
//		Integer plotY = record.getValue("plot_y", Integer.class);
//		Integer phase = record.getValue("phase", Integer.class);
//		Boolean groundPlot = record.getValue("ground_plot", Boolean.class);
//		Boolean permanentPlot = record.getValue("permanent_plot", Boolean.class);
//		
//		SamplePlot p = new SamplePlot();				
//		p.setPlotNo(plotNo);
//		p.setSamplingPhase(phase);
//		p.setObsUnitId(unitId);
//		GeodeticCoordinate plotLocation = GeodeticCoordinate.toInstance(plotX, plotY, srsId);
//		p.setPlotLocation(plotLocation);
//		p.setGroundPlot(groundPlot);
//		p.setPermanentPlot(permanentPlot);
//		interviews.add(p);
	}
	
	@Override
	protected void performInserts() {
		interviewDao.insert(interviews);
		interviewCategoricalValueDao.insert(categoricalValues);
		interviewNumericValueDao.insert(numericValues);
		interviews.clear();
		categoricalValues.clear();
		numericValues.clear();
	}

	@Override
	protected void onStart() {
		numericValues = new ArrayList<InterviewNumericValue>();
		categoricalValues = new ArrayList<InterviewCategoricalValue>();
		interviews = new ArrayList<Interview>();
	}

	@Override
	protected void cleanup() {
		interviews = null;
		categoricalValues = null;
		numericValues = null;
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
}
