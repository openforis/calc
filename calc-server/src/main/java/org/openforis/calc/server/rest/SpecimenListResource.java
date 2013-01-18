package org.openforis.calc.server.rest;

import javax.ws.rs.GET;

import org.openforis.calc.io.flat.FlatDataStream;
import org.openforis.calc.service.ObservationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * @author G. Miceli
 * @author Mino Togna
 */
@Component
@Lazy
@Scope("request")
public class SpecimenListResource extends SubResource<Void> {

	@Autowired
	private ObservationUnitResource observationUnitResource;

	@Autowired
	private ObservationService observationService;

	@Autowired
	private SurveyResource surveyResource;

	@GET
	public FlatDataStream getList() {
		String observationUnitName = observationUnitResource.getKey();
		String surveyName = surveyResource.getKey();

		return observationService.getSpecimenDataStream(surveyName, observationUnitName, getFields());
	}
}
