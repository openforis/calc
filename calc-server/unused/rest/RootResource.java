package org.openforis.calc.server.rest;

import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;

import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;


/**
 * Root resource containing first-level sub-resource locator methods.

 * @author G. Miceli
 *
 */
@Path("/")
@Component 
@Lazy
@Scope("request")
public class RootResource extends Resource {

	@QueryParam("f")
	private String fields;
	
//	@Path("surveys")
//	public SurveyListResource getSurveyListResource() {
//		return getResource(SurveyListResource.class);
//	}
	
	String[] getFields() {
		if ( fields == null ) {
			return null;
		} else {
			return fields.split(",");
		}
	}
}
