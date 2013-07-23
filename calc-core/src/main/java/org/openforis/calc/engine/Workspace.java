package org.openforis.calc.engine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.openforis.calc.chain.ProcessingChain;
import org.openforis.calc.common.UserObject;
import org.openforis.calc.metadata.AoiHierarchy;
import org.openforis.calc.metadata.Entity;

/**
 * Conceptually, a workspace contains all data, metadata, processing instructions, sampling design and any other information required for calculating results.
 * 
 * @author G. Miceli
 * @author M. Togna
 */
@javax.persistence.Entity
@Table(name = "workspace")
@org.hibernate.annotations.Cache(usage = org.hibernate.annotations.CacheConcurrencyStrategy.READ_WRITE)
public class Workspace extends UserObject {
	
	@Column(name = "input_schema")
	private String inputSchema;

	@Column(name = "output_schema")
	private String outputSchema;
	
	@OneToMany(mappedBy = "workspace", fetch = FetchType.EAGER)
	@OrderBy("sortOrder")
	@Fetch(FetchMode.SUBSELECT) 
	@Cascade(CascadeType.ALL)
	private List<Entity> entities;
	
	@OneToMany(mappedBy = "workspace", fetch = FetchType.EAGER)
	@OrderBy("name")
	@Fetch(FetchMode.SUBSELECT)
	@Cascade(CascadeType.ALL)
	private List<AoiHierarchy> aoiHierarchies;

	@OneToMany(mappedBy = "workspace", fetch = FetchType.EAGER)
	@OrderBy("id")
	@Fetch(FetchMode.SUBSELECT) 
	@Cascade(CascadeType.ALL)
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
		if (entities != null) {
			return Collections.unmodifiableList(entities);
		} else {
			return null;
		}
	}
	
	public void setEntities(List<Entity> entities) {
		this.entities = entities;
	}
	
	public List<AoiHierarchy> getAoiHierarchies() {
		return aoiHierarchies;
	}

	public void setAoiHierarchies(List<AoiHierarchy> aoiHierarchies) {
		this.aoiHierarchies = aoiHierarchies;
	}

	public List<ProcessingChain> getProcessingChains() {
		if (processingChains != null) {
			return Collections.unmodifiableList(processingChains);
		} else {
			return null;
		}
	}

	public void addProcessingChain(ProcessingChain chain) {
		chain.setWorkspace(this);
		
		processingChains.add(chain);
	}
	
	public ProcessingChain getProcessingChainById(int processingChainId) {
		for (ProcessingChain chain : processingChains) {
			if ( chain.getId() == processingChainId ) {
				return chain;
			}
		}
		return null;
	}

}