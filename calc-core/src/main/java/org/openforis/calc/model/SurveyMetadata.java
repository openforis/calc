package org.openforis.calc.model;

import java.sql.Date;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.openforis.commons.collection.CollectionUtils;

/**
 * @author G. Miceli
 * @author M. Togna
 */
public class SurveyMetadata {

	private Survey survey;
	private Map<Integer, ObservationUnitMetadata> obsUnitMetadataByUnitId;
	private Map<String, ObservationUnitMetadata> obsUnitMetadataByUnitName;
	private List<AoiHierarchyMetadata> aoiHierarchyMetadata;
	private List<TaxonomicChecklistMetadata> taxonomicChecklists;

	public SurveyMetadata(Survey survey, Collection<ObservationUnitMetadata> ous, List<AoiHierarchyMetadata> aoiHierarchyMetadata, List<TaxonomicChecklistMetadata> taxonomicChecklists) {
		this.survey = survey;
		this.aoiHierarchyMetadata = aoiHierarchyMetadata;
		setTaxonomicChecklists(taxonomicChecklists);
		setObservationUnitMetadata(ous);
	}

	private void setObservationUnitMetadata(Collection<ObservationUnitMetadata> ous) {
		obsUnitMetadataByUnitId = new LinkedHashMap<Integer, ObservationUnitMetadata>();
		obsUnitMetadataByUnitName = new HashMap<String, ObservationUnitMetadata>();
		for ( ObservationUnitMetadata ou : ous ) {
			ou.setSurveyMetadata(this);
			TaxonomicChecklistMetadata checkList = getTaxonomicChecklist( ou.getTaxonomicChecklistId() );
			ou.setTaxonomicChecklistMetadata(checkList);
			obsUnitMetadataByUnitId.put(ou.getObsUnitId(), ou);
			obsUnitMetadataByUnitName.put(ou.getObsUnitName(), ou);
		}
	}

	private TaxonomicChecklistMetadata getTaxonomicChecklist(Integer taxonomicChecklistId) {
		for ( TaxonomicChecklistMetadata checklist : getTaxonomicChecklists() ) {
			if ( checklist.getChecklistId().equals(taxonomicChecklistId) ) {
				return checklist;
			}
		}
		return null;
	}

	private void setTaxonomicChecklists(List<TaxonomicChecklistMetadata> taxonomicChecklists) {
		this.taxonomicChecklists = taxonomicChecklists;
		for ( TaxonomicChecklistMetadata checklist : this.taxonomicChecklists ) {
			checklist.setSurveyMetadata(this);
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
		return CollectionUtils.unmodifiableCollection(obsUnitMetadataByUnitId.values());
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
		return CollectionUtils.unmodifiableList(aoiHierarchyMetadata);
	}

//	public TaxonomicChecklistMetadata getTaxonomicChecklistByObsUnitId(int obsUnitId) {
//		for ( TaxonomicChecklistMetadata checklist : getTaxonomicChecklists() ) {
//			if ( checklist.getObsUnitId().equals(obsUnitId) ) {
//				return checklist;
//			}
//		}
//		return null;
//	}

	public List<TaxonomicChecklistMetadata> getTaxonomicChecklists() {
		return CollectionUtils.unmodifiableList(taxonomicChecklists);
	}

}
