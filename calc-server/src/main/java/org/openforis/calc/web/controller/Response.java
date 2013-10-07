/**
 * 
 */
package org.openforis.calc.web.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.validation.ObjectError;

/**
 * @author S. Ricci
 * @author M. Togna
 * 
 */
public class Response {

	public enum Status {
		OK, ERROR;
	}

	private Status status;
	private List<ObjectError> errors;

	Response() {
		this(null);
	}

	Response(List<ObjectError> errors) {
		if (errors != null && !errors.isEmpty()) {
			setStatus(Status.ERROR);
			this.errors = errors;
		} else {
			setStatus(Status.OK);
			this.errors = new ArrayList<ObjectError>();
		}
	}

	public Status getStatus() {
		return status;
	}

	void setStatus(Status status) {
		this.status = status;
	}

	public List<ObjectError> getErrors() {
		return errors;
	}

	public boolean hasErrors() {
		return status == Status.ERROR;
	}

}
