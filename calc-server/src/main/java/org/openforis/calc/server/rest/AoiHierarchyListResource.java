package org.openforis.calc.server.rest;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import org.openforis.calc.persistence.AoiHierarchyDao;
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
public class AoiHierarchyListResource extends SubResource<Void> {

	@Autowired
	private AoiHierarchyDao aoiHierarchyDao;

	@GET
    public FlatDataStream getList() {
    	return aoiHierarchyDao.streamAll(getFields());
    }
	
	@Path("{aoiHierarchyName}")
	public AoiHierarchyResource getSurveyResource(@PathParam("aoiHierarchyName") String name) {
		return getResource(AoiHierarchyResource.class, name);
	}
}
