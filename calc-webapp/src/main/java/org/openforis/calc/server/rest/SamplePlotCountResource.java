package org.openforis.calc.server.rest;

import javax.ws.rs.GET;

import org.openforis.calc.io.flat.FlatDataStream;
import org.openforis.calc.persistence.SamplePlotCntViewDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * 
 * @author Mino Togna
 * 
 */
@Component
@Scope("request")
@Lazy
public class SamplePlotCountResource extends SubResource<Void> {

	@Autowired
	private SamplePlotCntViewDao samplePlotCntViewDao;

	@Autowired
	private ObservationUnitResource observationUnitResource;

	@GET
	public FlatDataStream getCounts() {

		int obsUnitId = observationUnitResource.getObservationUnitId();
		return samplePlotCntViewDao.getCountsByObsUnit(obsUnitId );

	}

}
