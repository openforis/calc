package org.openforis.calc.engine;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.openforis.calc.chain.CalculationStep;
import org.openforis.calc.chain.ProcessingChain;
import org.openforis.calc.metadata.AoiHierarchy;
import org.openforis.calc.metadata.CategoricalVariable;
import org.openforis.calc.metadata.Category;
import org.openforis.calc.metadata.CategoryHierarchy;
import org.openforis.calc.metadata.CategoryLevel;
import org.openforis.calc.metadata.Entity;
import org.openforis.calc.metadata.EquationList;
import org.openforis.calc.metadata.ErrorSettings;
import org.openforis.calc.metadata.QuantitativeVariable;
import org.openforis.calc.metadata.SamplingDesign;
import org.openforis.calc.metadata.Stratum;
import org.openforis.calc.metadata.StratumAoi;
import org.openforis.calc.metadata.Variable;
import org.openforis.calc.metadata.WorkspaceSettings;
import org.openforis.calc.persistence.jooq.tables.pojos.WorkspaceBase;
import org.openforis.calc.schema.Schemas;

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
	private static final long serialVersionUID 	= 1L;

	public static final String DEFAULT_URI 		= "http://calc-default-uri";
	public static final String DEFAULT_SCHEMA 	= "calc-temp-schema";

	public static final String DEFAULT_CHAIN_CAPTION = "default";

   	private List<Entity> entities;

   	private List<Stratum> strata;
   	
   	private List<StratumAoi> strataAois;

	private List<AoiHierarchy> aoiHierarchies;

	private List<ProcessingChain> processingChains;

	private List<EquationList> equationLists;
	
	private List<Category> categories;
	
	private SamplingDesign samplingDesign;
	
	private ErrorSettings errorSettings; 
	
	private WorkspaceSettings settings;
	
	public Workspace() {
		this.processingChains = new ArrayList<ProcessingChain>();
	}

	/**
	 * TODO remove getInputSchema and replace it with getDataSchema
	 * @return
	 */
	@Override
	// TODO rename to dataSchema
	public String getInputSchema(){
		return super.getInputSchema();
	}
	
	public String getExtendedSchemaName(){
		return this.getInputSchema() + "_ext";
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
	
	public Stratum getStratumById(Integer id){
		for (Stratum stratum : getStrata()) {
			if(stratum.getId().equals(id) ){
				return stratum;
			}
		}
		return null;
	}
	
	public void setStrata(List<Stratum> strata) {
		this.strata = strata;
		for ( Stratum stratum : this.strata ) {
			stratum.setWorkspace(this);
		}
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
	
	@JsonIgnore
	public boolean hasStrataAois(){
		return getStrataAois().size() > 0;
	}
	
	public List<StratumAoi> getStrataAois() {
		return org.openforis.commons.collection.CollectionUtils.unmodifiableList( strataAois );
	}
	
	public void setStrataAois(Collection<StratumAoi> strataAois) {
		this.strataAois = new ArrayList<StratumAoi>( strataAois );
		for (StratumAoi stratumAoi : strataAois) {
			stratumAoi.setWorkspace( this );
		}
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
	
	public EquationList getEquationListById( long listId ) {
		List<EquationList> equationLists2 = getEquationLists();
		for (EquationList equationList : equationLists2) {
			if( equationList.getId().equals(listId) ) {
				return equationList;
			}
		}
		return null;
	}
	
	public void addEquationList( EquationList equationList ){
		if( this.equationLists == null ){
			this.equationLists = new ArrayList<EquationList>();
		}
		EquationList list = this.getEquationListById( equationList.getId() );
		if( list != null ){
			this.equationLists.remove( list );
		}
		
		this.equationLists.add( equationList );
		equationList.setWorkspace( this );
	}
	
	public void deleteEquationList( EquationList equationList ) {
		if( this.equationLists != null ) {
			Iterator<EquationList> iterator = this.equationLists.iterator();
			while( iterator.hasNext() ) {
				EquationList list = iterator.next();
				if( list.getName().equals(equationList.getName()) ){
					iterator.remove();
					return;
				}
			}
		}
	}
	
	public ErrorSettings getErrorSettings(){
		if( errorSettings == null ){
			this.setErrorSettings( new ErrorSettings() );
		}
		return errorSettings;
	}
	
	public void setErrorSettings(ErrorSettings errorSettings) {
		this.errorSettings = errorSettings;
		if( this.errorSettings != null ){
			this.errorSettings.setWorkspace( this );
		}
	}
	
	public WorkspaceSettings getSettings() {
		return settings;
	}
	
	public void setSettings(WorkspaceSettings settings) {
		this.settings = settings;
		if( settings != null ){
			settings.setWorkspace(this);
		}
	}
	
	/**
	 * Returns the categories associated to the workspace ordered by 'caption'
	 * @return
	 */
	public List<Category> getCategories() {
		if( categories == null ){
			return Collections.emptyList();
		} else {
			List<Category> list = new ArrayList<Category>( categories );
			Collections.sort( list, new Comparator<Category>() {
				public int compare(Category o1, Category o2) {
					return o1.getCaption().compareToIgnoreCase( o2.getCaption() );
				}
			});
			
			return org.openforis.commons.collection.CollectionUtils.unmodifiableList( list );
		}
		
	}
	
	public void setCategories(List<Category> categories) {
		this.categories = new ArrayList<Category>();
		for (Category category : categories) {
			addCategory( category );
		}
	}
	
	public void addCategory( Category category ){
		if( this.categories == null ){
			this.categories = new ArrayList<Category>();
		}
		category.setWorkspace( this );
		this.categories.add( category );
	}
	
	public void removeCategory( Category category ){
		if( this.categories == null ){
			return;
		}
		this.categories.remove(category);
	}
	
	public Category getCategoryById( Integer categoryId ){
		for ( Category category : getCategories() ){
			if( category.getId().equals(categoryId) ){
				return category;
			}
		}
		return null;
	}
	public CategoryLevel getCategoryLevelById( Integer categoryLevelId ){
		for ( Category category : getCategories() ){
			List<CategoryHierarchy> hierarchies = category.getHierarchies();
			for ( CategoryHierarchy hierarchy : hierarchies ){
				List<CategoryLevel> levels = hierarchy.getLevels();
				for ( CategoryLevel level : levels ) {
					if( level.getId().equals(categoryLevelId) ){
						return level;
					}
				}
			}
		}
		return null;
	}
	
	public CategoryLevel getCategoryLevelByTableName( String tableName ) {
		for ( Category category : getCategories() ) {
			List<CategoryHierarchy> hierarchies = category.getHierarchies();
			for ( CategoryHierarchy hierarchy : hierarchies ){
				List<CategoryLevel> levels = hierarchy.getLevels();
				for ( CategoryLevel level : levels ) {
					if( level.getTableName().equals(tableName) ){
						return level;
					}
				}
			}
		}
		return null;
	}
	
	@JsonInclude
	public String getPhase1PlotTableName() {
		return String.format( "_phase1_plot_%s" , this.getName() );
	}
	
	@JsonInclude
	public String getPrimarySUTableName() {
//		return String.format( "_primary_sampling_unit_%s" , this.getName() );
		return "_primary_sampling_unit";
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

	public Entity getEntityById(Integer id) {
		if ( id!=null && getName() != null && CollectionUtils.isNotEmpty(entities)) {
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
	
	public Variable<?> getVariableByOriginalId( Integer originalId ) {
		if( originalId == null ){
			return null;
		}
		List<Entity> entities = getEntities();
		for (Entity entity : entities) {
			Variable<?> v = entity.getVariableByOriginalId( originalId );
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
	
	public List<CategoricalVariable<?>> getVariablesByCategory(Category category){
		ArrayList<CategoricalVariable<?>> variables = new ArrayList<CategoricalVariable<?>>();
		
		for (Variable<?> variable : getVariables()) {
			if( variable instanceof CategoricalVariable ){
				for (CategoryHierarchy hierarchy : category.getHierarchies()) {
					for (CategoryLevel level : hierarchy.getLevels()) {
						Long levelId = level.getId().longValue();
						if( levelId.equals(variable.getCategoryLevelId()) ){
							variables.add( (CategoricalVariable<?>) variable );
						}
					}
				}
			}
		}
		
		return variables;
	}
	
	/** =====================================
	 * 		Sampling Design utility methods
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
		Entity samplingUnit = getSamplingUnit();
		return samplingUnit != null;
	}
	
	@JsonIgnore
	public boolean hasStratifiedSamplingDesign() {
		return this.hasSamplingDesign() && getSamplingDesign().getStratified();
	}
	
	@JsonIgnore
	public boolean hasClusterSamplingDesign() {
		return this.hasSamplingDesign() && getSamplingDesign().getCluster();
	}

	@JsonIgnore
	public boolean has2PhasesSamplingDesign() {
		return this.hasSamplingDesign() && getSamplingDesign().getTwoPhases();
	}
	
	@JsonIgnore
	public boolean has2StagesSamplingDesign() {
		return this.hasSamplingDesign() && getSamplingDesign().getTwoStages() != null && getSamplingDesign().getTwoStages();
	}
	
	@JsonIgnore
	public List<Variable<?>> getVariables() {
		List<Variable<?>> variables = new ArrayList<Variable<?>>();
		List<Entity> entities = getEntities();
		for (Entity entity : entities) {
			variables.addAll( entity.getVariables() );
		}
		return variables;
	}
	
	void removeInputCategories(){
		if( this.categories != null ){
			Iterator<Category> iterator = this.categories.iterator();
			while( iterator.hasNext() ){
				Category category = iterator.next();
				if( !category.isUserDefined() ){
					iterator.remove();
				}
			}
		}
	}
	
	/**
	 * Returns a new instance of Schemas object
	 */
	public Schemas schemas() {
		return new Schemas( this );
	}

	@JsonIgnore
	public QuantitativeVariable getAreaVariable() {
		if( this.hasSamplingDesign() ){
			QuantitativeVariable areaVar = new QuantitativeVariable();
			areaVar.setEntity(  this.getSamplingUnit() );
			areaVar.setId( -1 );
			areaVar.setName( "area" );
			areaVar.setCaption( "Area" );
			
			return areaVar;
		}
		return null;
	}

}