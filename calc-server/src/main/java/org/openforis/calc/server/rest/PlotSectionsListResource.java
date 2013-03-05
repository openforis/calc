package org.openforis.calc.server.rest;

import javax.ws.rs.GET;

import org.openforis.calc.persistence.PlotSectionViewDao;
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
	private PlotSectionViewDao plotSectionViewDao;

	@Autowired
	private ObservationUnitResource observationUnitResource;

	@GET
	public FlatDataStream getList() {
		return plotSectionViewDao.streamAll(getFields(), observationUnitResource.getObservationUnitId());
	}

}
