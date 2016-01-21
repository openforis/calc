/**
 * 
 */
package org.openforis.calc.web.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
	private Map<String, Object> fields;
	private boolean workspaceChanged;
	
	Response() {
		this(null);
	}

	Response( List<ObjectError> errors ) {
		fields = new HashMap<String, Object>();
		if (errors != null && !errors.isEmpty()) {
			setStatus(Status.ERROR);
			this.errors = errors;
		} else {
			setStatus(Status.OK);
			this.errors = new ArrayList<ObjectError>();
		}
		
		this.workspaceChanged = false;
	}

	public Status getStatus() {
		return status;
	}

	void setStatus(Status status) {
		this.status = status;
	}

	void setStatusOk() {
		setStatus( Status.OK );
	}
	
	void setStatusError() {
		setStatus( Status.ERROR );
	}
	
	public List<ObjectError> getErrors() {
		return errors;
	}
	
	void addError( ObjectError objectError){
		this.errors.add( objectError );
	}

	public boolean hasErrors() {
		return status == Status.ERROR;
	}

	public Map<String, Object> getFields() {
		return fields;
	}
	
	public void addField(String name, Object object){
		this.fields.put(name, object);
	}
	
	public boolean isWorkspaceChanged() {
		return workspaceChanged;
	}
	
	public void setWorkspaceChanged(boolean workspaceChanged) {
		this.workspaceChanged = workspaceChanged;
	}
	
	/**
	 * It sets the flag workspaceChanged to true to be parsed by the client
	 */
	public void setWorkspaceChanged() {
		this.setWorkspaceChanged(true);
	}
	
}
