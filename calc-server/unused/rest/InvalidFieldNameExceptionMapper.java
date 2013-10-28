package org.openforis.calc.server.rest;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.openforis.calc.persistence.jooq.InvalidFieldNameException;

/**
 * 
 * @author G. Miceli
 *
 */
@Provider
public class InvalidFieldNameExceptionMapper implements ExceptionMapper<InvalidFieldNameException> {

	public Response toResponse(InvalidFieldNameException e) {
		return Response.status(Response.Status.BAD_REQUEST).build();
	}

}
