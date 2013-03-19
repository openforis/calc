package org.openforis.calc.server.rest;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

import org.openforis.calc.service.ObservationService;
import org.openforis.commons.io.flat.FlatDataStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * Root resource containing first-level sub-resource locator methods.
 * 
 * @author M. Togna
 * 
 */
@Component
@Lazy
@Scope("request")
public class PlotSectionsListResource extends SubResource<Void> {

	@Autowired
	private ObservationService observationService;
	@Autowired
	private ObservationUnitResource observationUnitResource;
	@Autowired
	private SurveyResource surveyResource;

	@GET
	public FlatDataStream getList() {

		return observationService.getPlotSectionDataStream(surveyResource.getKey(), observationUnitResource.getKey(), getFields());

	}

	@PATCH
	@Path("/area")
	public Response updatePlotSectionArea(FlatDataStream dataStream) throws URISyntaxException, IOException {

		observationService.updatePlotSectionArea(dataStream);

		return Response.ok(new URI("area")).entity("OK").build();
	}
	
}
