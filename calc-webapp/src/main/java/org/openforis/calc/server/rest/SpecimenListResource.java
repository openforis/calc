package org.openforis.calc.server.rest;

import javax.ws.rs.GET;

import org.openforis.calc.io.flat.FlatDataStream;
import org.openforis.calc.persistence.SpecimenViewDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * Root resource containing first-level sub-resource locator methods.
 * 
 * @author G. Miceli
 * 
 */
@Component
@Lazy
@Scope("request")
public class SpecimenListResource extends SubResource<Void> {

	@Autowired
	private SpecimenViewDao specimenViewDao;

	@Autowired
	private ObservationUnitResource observationUnitResource;

	@Autowired
	private SurveyResource surveyResource;
	
	@GET
	public FlatDataStream getList() {
		return specimenViewDao.streamAll(getFields(), surveyResource.getSurvey().getSurveyName(), observationUnitResource.getObservationUnitId());
	}

	// @Path("{surveyName}")
	// public SpecimenResource getSpecimenResource(@PathParam("specimenName") String name) {
	// return getResource(SpecimenResource.class, name);
	// }
}
