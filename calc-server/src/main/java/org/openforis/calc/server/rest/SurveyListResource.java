package org.openforis.calc.server.rest;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import org.openforis.calc.io.flat.FlatDataStream;
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
public class SurveyListResource extends SubResource<Void> {

	@Autowired
	private SurveyDao surveyDao;

	@GET
    public FlatDataStream getList() {
    	return surveyDao.streamAll(getFields());
    }
	
	@Path("{surveyName}")
	public SurveyResource getSurveyResource(@PathParam("surveyName") String name) {
		return getResource(SurveyResource.class, name);
	}
}
