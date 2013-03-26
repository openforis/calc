package org.openforis.calc.model;

/**
 * 
 * @author M. Togna
 * 
 */
public class TaxonomicChecklistMetadata {

//	private static final String SUFFIX = "_taxon";

	private SurveyMetadata surveyMetadata;
	private TaxonomicChecklist taxonomicChecklist;

	public TaxonomicChecklistMetadata(TaxonomicChecklist taxonomicChecklist) {
		this.taxonomicChecklist = taxonomicChecklist;
	}

	public Integer getId() {
		return taxonomicChecklist.getId();
	}

	public Integer getChecklistId() {
		return taxonomicChecklist.getChecklistId();
	}

//	public Integer getObsUnitId() {
//		return taxonomicChecklist.getObsUnitId();
//	}

	public String getChecklistName() {
		return taxonomicChecklist.getChecklistName();
	}

	public String getChecklistLabel() {
		return taxonomicChecklist.getChecklistLabel();
	}

	public String getChecklistDescription() {
		return taxonomicChecklist.getChecklistDescription();
	}

	void setSurveyMetadata(SurveyMetadata surveyMetadata) {
		this.surveyMetadata = surveyMetadata;
	}

	public SurveyMetadata getSurveyMetadata() {
		return surveyMetadata;
	}

//	public ObservationUnitMetadata getObservationUnitMetadata() {
//		return getSurveyMetadata().getObservationUnitMetadataById(getObsUnitId());
//	}

	public String getTableName() {
		return getChecklistName();
	}
}
