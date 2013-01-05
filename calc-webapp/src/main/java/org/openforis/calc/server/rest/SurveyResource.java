package org.openforis.calc.server.rest;

import javax.annotation.PostConstruct;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.WebApplicationException;

import org.jooq.Result;
import org.openforis.calc.model.Survey;
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
@Path("/surveys/{survey}")
@Component
@Scope("request")
public class SurveyResource extends CalcResource {

	@Autowired
	private SurveyDao surveyDao;

	@Autowired
	priv

	@PathParam("survey")
	private String name;

	
	private Survey survey;
	
	@PostConstruct
//	public Result<ObservationUnitRecord> getObservationUnits() {
	public void loadSurvey() { 
		survey = surveyDao.findByName(name);
		if ( survey == null ) {
			throw new WebApplicationException(404);
		}
	}
	
//	@Path("/units")
	@GET
    public Result<SurveyRecord> getSurvey() {
    	return surveyDao.fetchByName(name);
    }
}
