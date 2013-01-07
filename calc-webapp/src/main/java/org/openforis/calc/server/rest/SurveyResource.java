package org.openforis.calc.server.rest;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.WebApplicationException;

import org.jooq.Result;
import org.openforis.calc.persistence.SurveyDao;
import org.openforis.calc.persistence.jooq.tables.records.SurveyRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;


/**
 * Root resource containing first-level sub-resource locator methods.

 * @author G. Miceli
 *
 */
@Component
@Scope("request")
public class SurveyResource extends CalcResource<String> {

	@Autowired
	private SurveyDao surveyDao;

	@GET
    public Result<SurveyRecord> getSurvey() {
    	return surveyDao.fetchByName(getKey());
    }
	
	@Path("/units")
	public ObservationUnitListResource getObservationUnitListResource() {
		return getResource(ObservationUnitListResource.class, null);
	}
	
	int getId() {
		Integer surveyId = surveyDao.findIdByName(getKey());
		if ( surveyId == null ) {
			throw new WebApplicationException(404);
		}
		return surveyId;
	}
}
