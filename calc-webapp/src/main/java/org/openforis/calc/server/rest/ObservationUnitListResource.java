package org.openforis.calc.server.rest;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import org.openforis.calc.persistence.ObservationUnitDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * 
 * @author G. Miceli
 *
 */
@Component
@Scope("request")
public class ObservationUnitListResource extends CalcResource<Void> {

	@Autowired
	private ObservationUnitDao observationUnitDao;
	
	@Autowired
	private SurveyResource surveyResource;
	
	@GET
	public String getObservationUnits() {
//		observationUnitDao.find
		return "units for survey: "+surveyResource.getId();
	}
	
	@Path("{unitName}")
	public ObservationUnitResource getObservationUnitResource(@PathParam("unitName") String name) {
		return getResource(ObservationUnitResource.class, name);
	}
}
