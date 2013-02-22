package org.openforis.calc.server.rest;

import javax.ws.rs.GET;
import javax.ws.rs.QueryParam;

import org.openforis.calc.io.flat.FlatDataStream;
import org.openforis.calc.persistence.SamplePlotDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * 
 * @author G. Miceli
 * @author M. Togna
 * 
 */
@Component
@Scope("request")
@Lazy
public class SamplePlotListResource extends SubResource<Void> {

	@Autowired
	private SamplePlotDao samplePlotDao;

	@Autowired
	private ObservationUnitResource observationUnitResource;

	@GET
	public FlatDataStream getList(@QueryParam("ground") Boolean groundPlots, @QueryParam("permanent") Boolean permanentPlots) {
		FlatDataStream stream = null;
		int unitId = observationUnitResource.getObservationUnitId();

		if ( Boolean.TRUE.equals(groundPlots) ) {
			stream = samplePlotDao.streamGroundPlots(getFields(), unitId);
		} else if ( Boolean.TRUE.equals(permanentPlots) ) {
			stream = samplePlotDao.streamPermanentPlots(getFields(), unitId);
		} else {
			stream = samplePlotDao.streamAll(getFields(), unitId);
		}

		return stream;
	}

}
