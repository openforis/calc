package org.openforis.calc.engine;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.openforis.calc.chain.ProcessingChain;
import org.openforis.calc.common.UserObject;
import org.openforis.calc.metadata.AoiHierarchy;
import org.openforis.calc.metadata.Entity;
import org.openforis.calc.metadata.Variable;

/**
 * Conceptually, a workspace contains all data, metadata, processing
 * instructions, sampling design and any other information required for
 * calculating results.
 * 
 * @author G. Miceli
 * @author M. Togna
 */
@javax.persistence.Entity
@Table(name = "workspace")
@org.hibernate.annotations.Cache(usage = org.hibernate.annotations.CacheConcurrencyStrategy.READ_WRITE)
public class Workspace extends UserObject {

	static final String DEFAULT_CHAIN_CAPTION = "default";

	@Column(name = "name")
	private String name;

	@Column(name = "collect_survey_uri")
	private String collectSurveyUri;

	@Column(name = "input_schema")
	private String inputSchema;

	@Column(name = "output_schema")
	private String outputSchema;

	@Column(name = "active")
	private boolean active;

	@OneToMany(mappedBy = "workspace", fetch = FetchType.EAGER, cascade = javax.persistence.CascadeType.ALL, orphanRemoval = true)
	@OrderBy("sortOrder")
	@Fetch(FetchMode.SUBSELECT)
	@Cascade(CascadeType.ALL)
	private List<Entity> entities;

	@OneToMany(mappedBy = "workspace", fetch = FetchType.EAGER, cascade = javax.persistence.CascadeType.ALL, orphanRemoval = true)
	@OrderBy("name")
	@Fetch(FetchMode.SUBSELECT)
	@Cascade(CascadeType.ALL)
	private List<AoiHierarchy> aoiHierarchies;

	@OneToMany(mappedBy = "workspace", fetch = FetchType.EAGER, cascade = javax.persistence.CascadeType.ALL, orphanRemoval = true)
	@OrderBy("id")
	@Fetch(FetchMode.SUBSELECT)
	@Cascade(CascadeType.ALL)
	private List<ProcessingChain> processingChains;

	public Workspace() {
		this.processingChains = new ArrayList<ProcessingChain>();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getCollectSurveyUri() {
		return collectSurveyUri;
	}

	public void setCollectSurveyUri(String collectSurveyUri) {
		this.collectSurveyUri = collectSurveyUri;
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
		return org.openforis.commons.collection.CollectionUtils.unmodifiableList(entities);
	}

	public void setEntities(List<Entity> entities) {
		this.entities = entities;
	}

	public List<AoiHierarchy> getAoiHierarchies() {
		return org.openforis.commons.collection.CollectionUtils.unmodifiableList(aoiHierarchies);
	}

	public void setAoiHierarchies(List<AoiHierarchy> aoiHierarchies) {
		this.aoiHierarchies = aoiHierarchies;
	}

	public List<ProcessingChain> getProcessingChains() {
		return org.openforis.commons.collection.CollectionUtils.unmodifiableList(processingChains);
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public void addProcessingChain(ProcessingChain chain) {
		chain.setWorkspace(this);

		processingChains.add(chain);
	}

	public ProcessingChain getDefaultProcessingChain() {
		for (ProcessingChain chain : processingChains) {
			if (chain.getCaption().equals(DEFAULT_CHAIN_CAPTION)) {
				return chain;
			}
		}
		throw new IllegalStateException("Deafault processing chain not found");
	}

	public ProcessingChain getProcessingChainById(int processingChainId) {
		for (ProcessingChain chain : processingChains) {
			if (chain.getId() == processingChainId) {
				return chain;
			}
		}
		return null;
	}

	public Collection<Entity> getRootEntities() {
		return getEntities(new Predicate() {
			@Override
			public boolean evaluate(Object object) {
				return ((Entity) object).getParent() == null;
			}
		});
	}

	public Collection<Entity> getNotOverriddenEntities() {
		return getEntities(new Predicate() {
			@Override
			public boolean evaluate(Object object) {
				Entity e = (Entity) object;
				return !(e.isOverride() || e.hasOverriddenVariables() || e.hasOverriddenDescendants());
			}
		});
	}

	@SuppressWarnings("unchecked")
	private Collection<Entity> getEntities(Predicate predicate) {
		if (entities == null || entities.isEmpty()) {
			return Collections.emptyList();
		} else {
			return CollectionUtils.select(entities, predicate);
		}
	}
	
	public void addEntity(Entity entity) {
		if ( entities == null ) {
			entities = new ArrayList<Entity>();
		}
		entities.add(entity);
	}

	public void removeEntities(Collection<Entity> entities) {
		if (CollectionUtils.isEmpty(this.entities) || CollectionUtils.isEmpty(entities)) {
			return;
		} else {
			this.entities.removeAll(entities);
		}
	}

	public Collection<Entity> removeNotOverriddenEntities() {
		Collection<Entity> notOverriddenEntities = getNotOverriddenEntities();
		removeEntities(notOverriddenEntities);
		return notOverriddenEntities;
	}

	public Entity getEntityById(int id) {
		if (name != null && CollectionUtils.isNotEmpty(entities)) {
			for (Entity e : entities) {
				if (e.getId().equals(id)) {
					return e;
				}
			}
		}
		return null;
	}

	public Entity getEntityByName(String name) {
		if (name != null && CollectionUtils.isNotEmpty(entities)) {
			for (Entity e : entities) {
				if (e.getName().equals(name)) {
					return e;
				}
			}
		}
		return null;
	}

	public Entity getEntityByOriginalId(Integer originalId) {
		if (originalId != null && CollectionUtils.isNotEmpty(entities)) {
			for (Entity e : entities) {
				if (e.getOriginalId() != null && e.getOriginalId().equals(originalId)) {
					return e;
				}
			}
		}
		return null;
	}

	public Collection<Variable<?>> getUserDefinedVariables() {
		Collection<Variable<?>> result = new HashSet<Variable<?>>();
		if ( CollectionUtils.isNotEmpty(entities) ) {
			for (Entity entity : entities) {
				Collection<Variable<?>> variables = entity.getUserDefinedVariables();
				result.addAll(variables);
			}
		}
		return result;
	}
	
}