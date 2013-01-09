package org.openforis.calc.server.rest;

import javax.ws.rs.GET;

import org.openforis.calc.io.flat.FlatDataStream;
import org.openforis.calc.persistence.SamplePlotDao;
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
public class SamplePlotListResource extends SubResource<Void> {

	@Autowired
	private SamplePlotDao samplePlotDao;
	
	@Autowired
	private ObservationUnitResource observationUnitResource;
	
	@GET
	public FlatDataStream getList() {
		return samplePlotDao.streamAll(getFields(), observationUnitResource.getObservationUnitId());
	}
}
