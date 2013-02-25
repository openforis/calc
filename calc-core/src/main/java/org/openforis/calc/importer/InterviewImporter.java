package org.openforis.calc.importer;

import java.util.Date;
import java.util.List;

import org.openforis.calc.geospatial.GeodeticCoordinate;
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
public class InterviewImporter extends AbstractObservationImporter<Interview, 
	InterviewNumericValue, InterviewCategoricalValue> {

	@Autowired
	private InterviewDao interviewDao;
	@Autowired
	private InterviewCategoricalValueDao interviewCategoricalValueDao;
	@Autowired
	private InterviewNumericValueDao interviewNumericValueDao;
	@Autowired
	private ClusterDao clusterDao;
	
	public InterviewImporter() {
		super(InterviewNumericValue.class, InterviewCategoricalValue.class);
		setInsertFrequency(1000);
		setReportFrequency(1000);
	}
	
	protected Interview processObservation(FlatRecord record) {
		String clusterCode = record.getValue("cluster_id", String.class);
		Integer clusterId = getClusterId(clusterCode);
		if ( clusterCode != null && clusterId == null) {
			log.warn("Invalid cluster code: "+clusterCode);
			return null;
		}
		// interview no
		Integer interviewNo = record.getValue("id", Integer.class);
		if ( interviewNo == null ) {
			log.warn("Missing interview number!");
			return null;
		}
		// interview date
		Date interviewDate = record.getValue("interview_date", Date.class, null);
		// location
		Double locationX = record.getValue("location_x", Double.class, null);
		Double locationY = record.getValue("location_y", Double.class, null);
		String srsId = record.getValue("location_srs_id", String.class);
		if ( locationX == null || locationY == null || srsId == null ) {
			log.warn("Missing or incomplete location");
			return null;
		}
		GeodeticCoordinate location = GeodeticCoordinate.toInstance(locationX, locationX, srsId);
		
		Interview iv = new Interview();
		iv.setClusterId(clusterId);
		iv.setInterviewNo(interviewNo);
		iv.setInterviewDate(interviewDate);
		iv.setInterviewLocation(location);
		return iv;
	}

	private Integer getClusterId(String clusterCode) {
		if ( clusterCode == null ) {
			return null;
		} else {
			Integer surveyId = getObservationUnitMetadata().getSurveyId();
			return clusterDao.getIdByKey(surveyId, clusterCode);
		}
	}

	@Override
	protected void doInserts(List<Interview> interviews, List<InterviewNumericValue> numVals, List<InterviewCategoricalValue> catVals) {
		interviewDao.insert(interviews);
		interviewNumericValueDao.insert(numVals);
		interviewCategoricalValueDao.insert(catVals);
	}
}
