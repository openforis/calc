package org.openforis.calc.model;

import java.util.ArrayList;
import java.util.List;

import org.openforis.calc.persistence.jooq.tables.pojos.SurveyBase;

/**
 * @author G. Miceli
 */
public class Survey extends SurveyBase implements ModelObject {

	private static final long serialVersionUID = 1L;
	
	private List<ObservationUnit> observationUnits;
	
	public void setObservationUnits(List<ObservationUnit> observationUnits) {
		this.observationUnits = new ArrayList<ObservationUnit>(observationUnits);
	}

	public ObservationUnit getObservationUnit(String name, String type) {
		if ( observationUnits == null ) {
			throw new NullPointerException("observationUnits not initialized");
		}
		for (ObservationUnit uo : observationUnits) {
			if ( uo.getName().equals(name) && uo.getType().equals(type) ) {
				return uo;
			}
		}
		return null;
	}
}
