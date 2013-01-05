package org.openforis.calc.server.rest;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

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
@Path("/surveys")
@Component 
@Scope("request")
public class SurveyListResource extends CalcResource {

	@Autowired
	private SurveyDao surveyDao;
	
	@GET
    public Result<SurveyRecord> getSurveyList() {
    	return surveyDao.fetchAll();
    }
}
