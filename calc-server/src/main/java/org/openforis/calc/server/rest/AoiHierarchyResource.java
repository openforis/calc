package org.openforis.calc.server.rest;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

import org.openforis.calc.io.flat.FlatDataStream;
import org.openforis.calc.persistence.AoiHierarchyDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;


/**
 * @author M. Togna
 *
 */
@Component
@Scope("request")
public class AoiHierarchyResource extends SubResource<String> {

	@Autowired
	private AoiHierarchyDao aoiHierarchyDao;

	@GET
    public FlatDataStream getSurveyData() {
    	return aoiHierarchyDao.streamByName(getFields(), getKey());
    }
	
	@Path("/aois")
	public AoiListResource getAoiListResource(){
		return getResource(AoiListResource.class);
	}
}
