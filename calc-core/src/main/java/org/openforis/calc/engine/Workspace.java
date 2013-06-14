package org.openforis.calc.engine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.openforis.calc.common.UserObject;
import org.openforis.calc.metadata.Entity;

/**
 * Conceptually, a workspace contains all data, metadata, processing instructions, sampling design and any other information required for calculating results.
 * 
 * @author G. Miceli
 * @author M. Togna
 */
@javax.persistence.Entity
@Table(name = "workspace")
public class Workspace extends UserObject {
	@Column(name = "input_schema")
	private String inputSchema;

	@Column(name = "output_schema")
	private String outputSchema;
	
	@OneToMany(mappedBy = "workspace", fetch = FetchType.EAGER)
	@OrderBy("sortOrder")
	@Fetch(FetchMode.SUBSELECT) 
	private List<Entity> entities;
	
	@OneToMany(mappedBy = "workspace", fetch = FetchType.EAGER)
	@OrderBy("id")
	@Fetch(FetchMode.SUBSELECT) 
	private List<ProcessingChain> processingChains;

	public Workspace() {
		this.processingChains = new ArrayList<ProcessingChain>();
	}
	
	public void setInputSchema(String inputSchema) {
		this.inputSchema = inputSchema;
	}

	public String getInputSchema() {
		return this.inputSchema;
	}

	public void setOutputSchema(String outputSchema) {
		this.outputSchema = outputSchema;
	}

	public String getOutputSchema() {
		return this.outputSchema;
	}

	public List<Entity> getEntities() {
		return Collections.unmodifiableList(entities);
	}
	
	public List<ProcessingChain> getProcessingChains() {
		return Collections.unmodifiableList(processingChains);
	}

	public void addProcessingChain(ProcessingChain chain) {
		chain.setWorkspace(this);
		
		processingChains.add(chain);
	}
}