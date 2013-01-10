package org.openforis.calc.server.rest;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.openforis.calc.io.flat.FlatDataStream;
import org.openforis.calc.model.ObservationUnit;
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
public class ObservationUnitResource extends SubResource<String> {

	@Autowired
	private ObservationUnitDao observationUnitDao;

	@Autowired
	private SurveyResource surveyResource;

	@GET
	public FlatDataStream getUnit() {
		int surveyId = surveyResource.getSurveyId();
		return observationUnitDao.streamByName(getFields(), surveyId, getKey());
	}

	@Path("sample-plots")
	public SamplePlotListResource getSamplePlotListResource() {
		assertType("plot");
		return getResource(SamplePlotListResource.class);
	}

	@Path("ground-plots")
	public GroundPlotListResource getGroundPlotListResource() {
		assertType("plot");
		return getResource(GroundPlotListResource.class);
	}

	@Path("permanent-plots")
	public PermanentPlotListResource getPermanentPlotListResource() {
		assertType("plot");
		return getResource(PermanentPlotListResource.class);
	}

	private void assertType(String type) {
		ObservationUnit unit = getObservationUnit();
		if ( !type.equals(unit.getObsUnitType()) ) {
			throw new WebApplicationException(Response.Status.NOT_FOUND);
		}
	}

	ObservationUnit getObservationUnit() {
		int surveyId = surveyResource.getSurveyId();

		ObservationUnit unit = observationUnitDao.find(surveyId, getKey());
		if ( unit == null ) {
			throw new WebApplicationException(Response.Status.NOT_FOUND);
		}
		return unit;
	}

	int getObservationUnitId() {
		return getObservationUnit().getId();
	}
}
