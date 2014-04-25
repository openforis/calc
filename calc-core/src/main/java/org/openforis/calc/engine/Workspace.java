package org.openforis.calc.engine;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.openforis.calc.chain.CalculationStep;
import org.openforis.calc.chain.ProcessingChain;
import org.openforis.calc.metadata.AoiHierarchy;
import org.openforis.calc.metadata.Entity;
import org.openforis.calc.metadata.EquationList;
import org.openforis.calc.metadata.SamplingDesign;
import org.openforis.calc.metadata.Stratum;
import org.openforis.calc.metadata.Variable;
import org.openforis.calc.persistence.jooq.tables.pojos.WorkspaceBase;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Conceptually, a workspace contains all data, metadata, processing
 * instructions, sampling design and any other information required for
 * calculating results.
 * 
 * @author G. Miceli
 * @author M. Togna
 */
public class Workspace extends WorkspaceBase {

	private static final long serialVersionUID = 1L;

	public static final String DEFAULT_CHAIN_CAPTION = "default";

   	private List<Entity> entities;

   	private List<Stratum> strata;

	private List<AoiHierarchy> aoiHierarchies;

	private List<ProcessingChain> processingChains;

	private List<EquationList> equationLists;
	
	private SamplingDesign samplingDesign;
	
	public Workspace() {
		this.processingChains = new ArrayList<ProcessingChain>();
	}

	/**
	 * TODO remove getInputSchema and replace it with getDataSchema
	 * @return
	 */
	@JsonInclude
	public String getDataSchema(){
		return getInputSchema();
	} 

	public List<Entity> getEntities() {
		return org.openforis.commons.collection.CollectionUtils.unmodifiableList( entities );
	}

	public void setEntities(List<Entity> entities) {
		this.entities = entities;
	}

	public List<AoiHierarchy> getAoiHierarchies() {
		return org.openforis.commons.collection.CollectionUtils.unmodifiableList( aoiHierarchies );
	}

	public void setAoiHierarchies(List<AoiHierarchy> aoiHierarchies) {
		this.aoiHierarchies = aoiHierarchies;
	}
	
	public void addAoiHierarchy(AoiHierarchy aoiHierarchy) {
		if (aoiHierarchies == null ){
			aoiHierarchies = new ArrayList<AoiHierarchy>();
		}
		aoiHierarchy.setWorkspace( this );
		this.aoiHierarchies.add(aoiHierarchy);
	}
	
	public List<ProcessingChain> getProcessingChains() {
		return org.openforis.commons.collection.CollectionUtils.unmodifiableList( processingChains );
	}

	public List<Stratum> getStrata() {
		return org.openforis.commons.collection.CollectionUtils.unmodifiableList( strata );
	}
	
	public void setStrata(List<Stratum> strata) {
		this.strata = strata;
	}
	
	public void emptyStrata() {
		setStrata(new ArrayList<Stratum>());
	}
	
	public void addStratum(Stratum stratum) {
		if( this.strata == null ){
			emptyStrata();
		}
		this.strata.add(stratum);
		stratum.setWorkspace(this);
	}
	
	public SamplingDesign getSamplingDesign() {
		return samplingDesign;
	}
	
	public void setSamplingDesign( SamplingDesign samplingDesign ) {
		this.samplingDesign = samplingDesign;
		if( samplingDesign != null ){
			samplingDesign.setWorkspace( this );
		}
	}
	
	public List<EquationList> getEquationLists() {
		return org.openforis.commons.collection.CollectionUtils.unmodifiableList( equationLists );
	}
	
	public void setEquationLists(List<EquationList> equationLists) {
		if( equationLists != null ){
			for (EquationList equationList : equationLists) {
				this.addEquationList(equationList);
			}
		}
	}
	
	public void addEquationList( EquationList equationList ){
		if( this.equationLists == null ){
			this.equationLists = new ArrayList<EquationList>();
		}
		this.equationLists.add( equationList );
		equationList.setWorkspace( this );
	}
	
	@JsonInclude
	public String getPhase1PlotTableName() {
		return String.format( "_phase1_plot_%s" , this.getName() );
	}
	
	public void addProcessingChain(ProcessingChain chain) {
		chain.setWorkspace(this);

		processingChains.add(chain);
	}

	@JsonIgnore	
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
	
	@JsonIgnore	
	public Collection<Entity> getRootEntities() {
		return getEntities(new Predicate() {
			@Override
			public boolean evaluate(Object object) {
				return ((Entity) object).getParent() == null;
			}
		});
	}
	
	@JsonIgnore	
	public Collection<Entity> getNotOverriddenEntities() {
		return getEntities(new Predicate() {
			@Override
			public boolean evaluate(Object object) {
				Entity e = (Entity) object;
				return !(e.getOverride() || e.hasOverriddenVariables() || e.hasOverriddenDescendants());
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
		if (entities == null) {
			entities = new ArrayList<Entity>();
		}
		entity.setWorkspace( this );
		entities.add( entity );
	}

	public void removeEntities(Collection<Entity> entities) {
		if (CollectionUtils.isEmpty(this.entities) || CollectionUtils.isEmpty(entities)) {
			return;
		} else {
			for (Entity entity : entities) {
				removeEntity(entity);
			}
		}
	}

	/**
	 * Removes the specified entity and all the dependencies to it
	 */
	public void removeEntity(Entity entity) {
		Entity p = entity.getParent();
		if ( p != null ) {
			p.removeChild(entity);
		}
		this.entities.remove(entity);
		//TODO remove descendants
	}

	public Collection<Entity> removeNotOverriddenEntities() {
		Collection<Entity> notOverriddenEntities = getNotOverriddenEntities();
		removeEntities(notOverriddenEntities);
		return notOverriddenEntities;
	}

	public Entity getEntityById(int id) {
		if (getName() != null && CollectionUtils.isNotEmpty(entities)) {
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
	
	@JsonIgnore	
	public Collection<Variable<?>> getUserDefinedVariables() {
		Collection<Variable<?>> result = new HashSet<Variable<?>>();
		if (CollectionUtils.isNotEmpty(entities)) {
			for (Entity entity : entities) {
				Collection<Variable<?>> variables = entity.getUserDefinedVariables();
				result.addAll(variables);
			}
		}
		return result;
	}

	public Variable<?> getVariableByName(String name) {
		List<Entity> entities = getEntities();
		for (Entity entity : entities) {
			Variable<?> v = entity.getVariableByName(name);
			if (v != null) {
				return v;
			}
		}
		return null;
	}
	
	public Variable<?> getVariableById( Integer id ) {
		if( id == null ){
			return null;
		}
		List<Entity> entities = getEntities();
		for (Entity entity : entities) {
			Variable<?> v = entity.getVariableById(id);
			if (v != null) {
				return v;
			}
		}
		return null;
	}
	
	public List<CalculationStep> getCalculationStepsByVariable(int variableId) {
		List<CalculationStep> result = new ArrayList<CalculationStep>();
		for (ProcessingChain processingChain : getProcessingChains()) {
			for (CalculationStep calculationStep : processingChain.getCalculationSteps()) {
				Variable<?> outputVariable = calculationStep.getOutputVariable();
				if ( outputVariable.getId() == variableId ) {
					result.add(calculationStep);
				}
			}
		}
		return result;
	}

	/** =====================================
	 * 		Sampling Desing utility methods
	 * 	=====================================
	 */
	public boolean isSamplingUnit(int entityId) {
		if( this.samplingDesign != null ){
			Integer samplingUnitId = this.samplingDesign.getSamplingUnitId();
			if( samplingUnitId != null ){
				return samplingUnitId.equals( entityId );
			}
		}
		return false;
	}
	
	@JsonIgnore
	public Entity getSamplingUnit() {
		SamplingDesign sd = this.getSamplingDesign();
		if( sd != null ) {
			return sd.getSamplingUnit();
		}
		return null;
	}
	
	@JsonIgnore
	public boolean hasSamplingDesign() {
		return getSamplingUnit() != null;
	}
	
	public boolean hasStratifiedSamplingDesign() {
		return this.hasSamplingDesign() && getSamplingDesign().getStratified();
	}

	public boolean hasClusterSamplingDesign() {
		return this.hasSamplingDesign() && getSamplingDesign().getCluster();
	}

	public List<Variable<?>> getVariables() {
		List<Variable<?>> variables = new ArrayList<Variable<?>>();
		List<Entity> entities = getEntities();
		for (Entity entity : entities) {
			variables.addAll( entity.getVariables() );
		}
		return variables;
	}

}