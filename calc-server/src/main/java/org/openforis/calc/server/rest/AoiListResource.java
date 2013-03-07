package org.openforis.calc.server.rest;

import javax.ws.rs.GET;

import org.openforis.calc.persistence.AoiDao;
import org.openforis.commons.io.flat.FlatDataStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;


/**
 * @author M. Togna
 *
 */
@Component 
@Scope("request")
@Lazy
public class AoiListResource extends SubResource<Void> {

	@Autowired
	private AoiDao aoiDao;

	@Autowired
	private AoiHierarchyResource aoiHierarchyResource;
	
	@GET
    public FlatDataStream getList() {
    	return aoiDao.streamByHierarchyName(getFields(), aoiHierarchyResource.getKey());
    }
	
//	@Path("{aoiHierarchyName}")
//	public AoiHierarchyResource getSurveyResource(@PathParam("aoiHierarchyName") String name) {
//		return getResource(AoiHierarchyResource.class, name);
//	}
}
