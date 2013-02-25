package org.openforis.calc.model;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 
 * @author G. Miceli
 * @author M. Togna
 * 
 */
public class ObservationUnitMetadata extends ObservationUnit {

	private static final long serialVersionUID = 1L;
	
	private ObservationUnit observationUnit;
	private Map<String, VariableMetadata> variableMap;
	private SurveyMetadata surveyMetadata;

	public ObservationUnitMetadata(ObservationUnit observationUnit, Collection<VariableMetadata> variables) {
		this.observationUnit = observationUnit;
		setVariableMetadata(variables);
	}

	private void setVariableMetadata(Collection<VariableMetadata> variables) {
		this.variableMap = new LinkedHashMap<String, VariableMetadata>();
		for ( VariableMetadata var : variables ) {
			variableMap.put(var.getVariableName(), var);
			var.setObservationUnitMetadata( this );
		}
	}

	public Collection<VariableMetadata> getVariableMetadata() {
		return Collections.unmodifiableCollection(variableMap.values());
	}

	public VariableMetadata getVariableMetadataByName(String name) {
		if ( variableMap == null ) {
			throw new NullPointerException("variableMap not initialized");
		}
		return variableMap.get(name);
	}

	public Integer getObsUnitId() {
		return observationUnit.getObsUnitId();
	}

	public Integer getSurveyId() {
		return observationUnit.getSurveyId();
	}

	public String getObsUnitName() {
		return observationUnit.getObsUnitName();
	}

	public String getObsUnitType() {
		return observationUnit.getObsUnitType();
	}

	public Integer getObsUnitParentId() {
		return observationUnit.getObsUnitParentId();
	}

	public String getObsUnitDescription() {
		return observationUnit.getObsUnitDescription();
	}

	public String getObsUnitLabel() {
		return observationUnit.getObsUnitLabel();
	}

	public ObservationUnitMetadata getObsUnitParent() {
		Integer obsUnitParentId = getObsUnitParentId();
		if ( obsUnitParentId == null ) {
			return null;
		} else {
			return surveyMetadata.getObservationUnitMetadataById(obsUnitParentId);
		}
	}

	void setSurveyMetadata(SurveyMetadata surveyMetadata) {
		this.surveyMetadata = surveyMetadata;
	}
	
	public SurveyMetadata getSurveyMetadata() {
		return surveyMetadata;
	}
	
	public Type getObsUnitTypeEnum() {
		return observationUnit.getObsUnitTypeEnum();
	}

	public Integer getId() {
		return observationUnit.getId();
	}

	public boolean isPlot() {
		return observationUnit.isPlot();
	}

	public boolean isSpecimen() {
		return observationUnit.isSpecimen();
	}

	public boolean isInterview() {
		return observationUnit.isInterview();
	}
}
