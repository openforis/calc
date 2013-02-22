package org.openforis.calc.importer;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.openforis.calc.io.flat.FlatRecord;
import org.openforis.calc.model.Interview;
import org.openforis.calc.model.InterviewCategoricalValue;
import org.openforis.calc.model.InterviewNumericValue;
import org.openforis.calc.persistence.ClusterDao;
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
	@Autowired
	private ClusterDao clusterDao;

	public InterviewImporter() {
		setInsertFrequency(1000);
		setReportFrequency(1000);
	}
	
	@Override
	protected boolean processRecord(FlatRecord record) {
		if ( surveyId == null || unitId == null ) {
			throw new NullPointerException("Survey or unit ID not set");
		}
		// cluster code
		String clusterCode = record.getValue("cluster_id", String.class);
		Integer clusterId = getClusterId(clusterCode);
		if ( clusterCode != null && clusterId == null) {
			log.warn("Invalid cluster code: "+clusterCode);
			return false;
		}
		// interview no
		Integer interviewNo = record.getValue("id", Integer.class);
		if ( interviewNo == null ) {
			log.warn("Missing interview number!");
			return false;
		}
		// interview date
		Date interviewDate = record.getValue("interview_date", Date.class);
		
		Interview iv = new Interview();
		iv.setClusterId(clusterId);
		iv.setInterviewNo(interviewNo);
		iv.setInterviewDate(interviewDate);
		// TODO other fields
		
		return true;
//		p.setPlotNo(plotNo);
//		p.setSamplingPhase(phase);
//		p.setObsUnitId(unitId);
//		GeodeticCoordinate plotLocation = GeodeticCoordinate.toInstance(plotX, plotY, srsId);
//		p.setPlotLocation(plotLocation);
//		p.setGroundPlot(groundPlot);
//		p.setPermanentPlot(permanentPlot);
//		interviews.add(p);
	}

	private Integer getClusterId(String clusterCode) {
		if ( clusterCode == null ) {
			return null;
		} else {
			return clusterDao.getIdByKey(surveyId, clusterCode);
		}
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
