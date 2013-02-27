package org.openforis.calc.model;

import java.sql.Date;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author G. Miceli
 */
public class SurveyMetadata {
	
	private Survey survey;
	private Map<Integer, ObservationUnitMetadata> obsUnitMetadataByUnitId;
	private Map<String, ObservationUnitMetadata> obsUnitMetadataByUnitName;
	private List<AoiHierarchyMetadata> aoiHierarchyMetadata;
	
	public SurveyMetadata(Survey survey, Collection<ObservationUnitMetadata> ous, List<AoiHierarchyMetadata> aoiHierarchyMetadata) {
		this.survey = survey;
		this.aoiHierarchyMetadata = aoiHierarchyMetadata;
		setObservationUnitMetadata(ous);
	}
	
	private void setObservationUnitMetadata(Collection<ObservationUnitMetadata> ous) {
		obsUnitMetadataByUnitId = new LinkedHashMap<Integer, ObservationUnitMetadata>();
		obsUnitMetadataByUnitName = new HashMap<String, ObservationUnitMetadata>();
		for ( ObservationUnitMetadata ou : ous ) {
			ou.setSurveyMetadata( this );
			obsUnitMetadataByUnitId.put( ou.getObsUnitId(), ou );
			obsUnitMetadataByUnitName.put( ou.getObsUnitName(), ou );
		}
	}

	public ObservationUnitMetadata getObservationUnitMetadataByName(String name) {
		if ( obsUnitMetadataByUnitId == null ) {
			throw new NullPointerException("observationUnits not initialized");
		}
		return obsUnitMetadataByUnitName.get(name);
	}

	public ObservationUnitMetadata getObservationUnitMetadataById(int obsUnitId) {
		return obsUnitMetadataByUnitId.get(obsUnitId);
	}

	public Collection<ObservationUnitMetadata> getObservationMetadata() {
		return Collections.unmodifiableCollection(obsUnitMetadataByUnitId.values());
	}

	public Integer getSurveyId() {
		return survey.getSurveyId();
	}

	public String getSurveyUri() {
		return survey.getSurveyUri();
	}

	public String getSurveyName() {
		return survey.getSurveyName();
	}

	public Date getSurveyStartDate() {
		return survey.getSurveyStartDate();
	}

	public Date getSurveyEndDate() {
		return survey.getSurveyEndDate();
	}

	public String getSurveyLabel() {
		return survey.getSurveyLabel();
	}

	public String getSurveyDescription() {
		return survey.getSurveyDescription();
	}
	
	public List<AoiHierarchyMetadata> getAoiHierarchyMetadata() {
		return Collections.unmodifiableList(aoiHierarchyMetadata);
	}
}
