package org.openforis.calc.workspace;

import java.util.ArrayList;

import javax.persistence.Column;
import javax.persistence.Table;

import org.openforis.calc.common.UserObject;
import org.openforis.calc.engine.ProcessingChain;
import org.openforis.calc.metadata.Entity;

/**
 * Conceptually, a workspace contains all data, metadata, processing instructions, sampling design and any other information required for calculating results.
 * 
 * @author G. Miceli
 * @author M. Togna
 */
@Table(name = "workspace")
public final class Workspace extends UserObject {
	private String inputSchema;
	
	private String outputSchema;
	
	private ArrayList<Entity> entities = new ArrayList<Entity>();
	private ArrayList<ProcessingChain> processingChains = new ArrayList<ProcessingChain>();

	@Column(name = "input_schema")
	public void setInputSchema(String inputSchema) {
		this.inputSchema = inputSchema;
	}

	public String getInputSchema() {
		return this.inputSchema;
	}

	@Column(name = "output_schema")
	public void setOutputSchema(String outputSchema) {
		this.outputSchema = outputSchema;
	}

	public String getOutputSchema() {
		return this.outputSchema;
	}
}