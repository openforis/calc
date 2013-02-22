package org.openforis.calc.server.rest;

import javax.ws.rs.GET;
import javax.ws.rs.QueryParam;

import org.openforis.calc.io.flat.FlatDataStream;
import org.openforis.calc.persistence.SamplePlotCntViewDao;
import org.openforis.calc.persistence.SamplePlotVisitedCntViewDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * 
 * @author M. Togna
 * 
 */
@Component
@Scope("request")
@Lazy
public class SamplePlotCountResource extends SubResource<Void> {

	@Autowired
	private SamplePlotCntViewDao samplePlotCntViewDao;

	@Autowired
	private SamplePlotVisitedCntViewDao samplePlotVisitedCntViewDao;

	@Autowired
	private ObservationUnitResource observationUnitResource;

	@GET
	@Deprecated
	public FlatDataStream getCounts(@QueryParam("observed") Boolean observed) {
//		int obsUnitId = observationUnitResource.getObservationUnitId();
//		FlatDataStream stream = null;
//		if ( Boolean.TRUE.equals(observed) ) {
//			stream = samplePlotVisitedCntViewDao.getCountsByObsUnit(obsUnitId);
//		} else {
//			stream = samplePlotCntViewDao.getCountsByObsUnit(obsUnitId);
//		}
//		return stream;
		return null;
	}

}
