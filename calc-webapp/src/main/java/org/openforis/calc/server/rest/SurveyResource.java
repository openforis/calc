package org.openforis.calc.server.rest;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.openforis.calc.io.flat.FlatDataStream;
import org.openforis.calc.model.Survey;
import org.openforis.calc.persistence.SurveyDao;
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
public class SurveyResource extends SubResource<String> {

	@Autowired
	private SurveyDao surveyDao;

	@GET
    public FlatDataStream getSurveyData() {
    	return surveyDao.streamByName(getFields(), getKey());
    }
	
	@Path("/units")
	public ObservationUnitListResource getObservationUnitListResource() {
		return getResource(ObservationUnitListResource.class);
	}

	Survey getSurvey() {
		Survey survey = surveyDao.findByName(getKey());
		if ( survey == null ) {
			throw new WebApplicationException(Response.Status.NOT_FOUND);
		}
		return survey;
	}

	int getSurveyId() {
		Survey survey = getSurvey();
		return survey.getId();
	}
}
