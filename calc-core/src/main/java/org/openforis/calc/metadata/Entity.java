package org.openforis.calc.metadata;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Stack;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.collections.functors.InstanceofPredicate;
import org.apache.commons.lang3.StringUtils;
import org.openforis.calc.chain.CalculationStep;
import org.openforis.calc.chain.ProcessingChain;
import org.openforis.calc.engine.Workspace;
import org.openforis.calc.persistence.jooq.tables.pojos.EntityBase;
import org.openforis.calc.r.RScript;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Provides metadata about a particular unit of observation, calculation or
 * analysis. Entities are anything which may have attributes for variables
 * associated with it.
 * 
 * @author G. Miceli
 * @author M. Togna
 */
public class Entity extends EntityBase {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@JsonIgnore
	private Workspace workspace;
	@JsonIgnore
	private Entity parent;
	@JsonIgnore
//	@OrderBy("sortOrder")
	private List<Variable<?>> variables = new ArrayList<Variable<?>>();
	@JsonIgnore
//	@OrderBy("sortOrder")
	private List<Entity> children = new ArrayList<Entity>();

	@JsonIgnore
	private Variable<?> clusterVariable;
	
//	@JsonIgnore
//	private Variable<?> unitNoVariable;

	public Workspace getWorkspace() {
		return this.workspace;
	}
	
	public void setWorkspace(Workspace workspace) {
		this.workspace = workspace;
	}
	
	public String getDataView() {
		return String.format( "%s_view", getName() );
	}

	public void addVariable(Variable<?> variable) {
		variable.setEntity( this );
//		variable.setSortOrder( getNextVariableSortOrder() );
		variables.add( variable );
	}
	
	public List<Variable<?>> getVariables() {
		return org.openforis.commons.collection.CollectionUtils.unmodifiableList( variables );
	}

	public Variable<?> getVariableByOriginalId( int id ) {
		if ( variables != null && ! variables.isEmpty() ) {
			for (Variable<?> var : variables) {
				if ( var.getOriginalId() != null && var.getOriginalId().equals(id) ) {
					return var;
				}
			}
		}
		return null;
	}
	
	public Variable<?> getVariableById( int id ) {
		for ( Variable<?> var : getVariables() ) {
			if ( var.getId().equals(id) ) {
				return var;
			}
		}
		return null;
	}
	
	public Variable<?> getVariableByName(String name) {
		if ( variables == null || variables.isEmpty() ) {
			return null;
		} else {
			for (Variable<?> v : variables) {
				if ( v.getName().equals(name) ) {
					return v;
				}
			}
			return null;
		}
	}
	
	/**
	 * Find the variable with the given name in the hierarchy up to root entity
	 * @param name
	 * @return
	 */
	public Variable<?> findVariableByName(String name) {
		Entity entity = this;

		while( entity != null ){
			Variable<?> variable = entity.getVariableByName(name);
		
			if( variable != null ){
				return variable;
			}
			
			entity = entity.getParent();
		}
		
		return null;
	}

	/**
	 * Find the variable with the given id in the hierarchy up to root entity
	 * @param name
	 * @return
	 */
	public Variable<?> findVariableById( int id ) {
		Entity entity = this;

		while( entity != null ) {
			Variable<?> variable = entity.getVariableById( id );
			if( variable != null ){
				return variable;
			}
			entity = entity.getParent();
		}
		
		return null;
	}

	
	@JsonIgnore
	public List<VariableAggregate> getVariableAggregates() {
		List<VariableAggregate> aggs = new ArrayList<VariableAggregate>();
		for (Variable<?> var : variables) {
			if ( var instanceof QuantitativeVariable ) {
				aggs.addAll(((QuantitativeVariable) var).getAggregates());
			}
		}
		return Collections.unmodifiableList(aggs);
	}
	
	@JsonIgnore
	public boolean hasOverriddenVariables() {
		return ! getOverriddenVariables().isEmpty();
	}
	
	@JsonIgnore
	public boolean hasOverriddenDescendants() {
		Stack<Entity> stack = new Stack<Entity>();
		stack.addAll(getChildren());
		while ( ! stack.isEmpty() ) {
			Entity descendant = stack.pop();
			if ( descendant.getOverride() || hasOverriddenVariables() ) {
				return true;
			}
			stack.addAll(descendant.getChildren());
		}
		return false;
	}
	
	public Entity getParent() {
		return parent;
	}

	public void setParent(Entity parent) {
		this.parent = parent;
	}
	
	@JsonIgnore	
	public boolean isGeoreferenced() {
		// right now only if this is sampling unit or parent entity is sampling unit
		return ( this.isSamplingUnit() ) || ( this.parent!=null && this.parent.isSamplingUnit() ); 
//		return ((xColumn != null && yColumn != null) || locationColumn != null);
	}

	public boolean isSamplingUnit() {
		if( this.getId() == null ){
			return false;
		}
		return getWorkspace().isSamplingUnit(getId());
	}

	public List<Entity> getChildren() {
		return Collections.unmodifiableList(children);
	}
	
	public void addChild( Entity entity ) {
		if ( children == null ) {
			children = new ArrayList<Entity>();
		}
		entity.setParent(this);
		children.add(entity);
	}
	
	public void removeChild(Entity entity) {
		if ( CollectionUtils.isNotEmpty(children) ) {
			entity.setParent(null);
			children.remove(entity);
		}
	}

	public RScript getPlotAreaRScript() {
		String plotAreaScript = getPlotAreaScript();
		if( StringUtils.isBlank(plotAreaScript) ) {
			return null;
		} else {
			RScript plotArea = new RScript().rScript( plotAreaScript , getHierarchyVariables() );
			return plotArea;
		}
	}
	
	public List<Variable<?>> getHierarchyVariables() {
		List<Variable<?>> variables = new ArrayList<Variable<?>>();

		Entity entity = this;
		while(entity != null){
			variables.addAll(entity.getVariables());
			entity = entity.getParent();
		}
		
		return variables;
	}
	
	public String getResultsTable() {
		return String.format( "_%s_results" , getName() );
	}

	public String getTemporaryResultsTable() {
		return String.format( "_%s_temp_results" , getName() );
	}
	
	
//	@JsonIgnore
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public List<CategoricalVariable<?>> getCategoricalVariables() {
		return Collections.unmodifiableList(selectInstancesOf((List)variables, CategoricalVariable.class));
	}

//	@JsonIgnore
	public List<QuantitativeVariable> getQuantitativeVariables() {
		return Collections.unmodifiableList(selectInstancesOf(variables, QuantitativeVariable.class));
	}

//	@JsonIgnore
	public List<TextVariable> getTextVariables() {
		return Collections.unmodifiableList(selectInstancesOf(variables, TextVariable.class));
	}
	
	@JsonIgnore
	public Collection<Variable<?>> getUserDefinedVariables() {
		Collection<Variable<?>> result = new HashSet<Variable<?>>();
		if ( CollectionUtils.isNotEmpty(variables) ) {
			for (Variable<?> v: variables) {
				if ( v.getOriginalId() == null ) {
					result.add(v);
				}
			}
		}
		return result;
	}
	
	@JsonIgnore
	public Collection<Variable<?>> getOverriddenVariables() {
		return getVariables(new Predicate() {
			@Override
			public boolean evaluate(Object object) {
				return ((Variable<?>) object).getOverride();
			}
		});
	}
	
	@JsonIgnore
	public Collection<Variable<?>> getNotOverriddenVariables() {
		return getVariables(new Predicate() {
			@Override
			public boolean evaluate(Object object) {
				return ! ((Variable<?>) object).getOverride();
			}
		});
	}
	
	// TODO for now simulated this way. 
	
	@SuppressWarnings("unchecked")
	@JsonIgnore
	public Collection<QuantitativeVariable> getOriginalQuantitativeVariables() {
		return CollectionUtils.select(getQuantitativeVariables(), new Predicate() {
			@Override
			public boolean evaluate(Object object) {
				return ((QuantitativeVariable)object).getOriginalId() != null;
			}
		});
	}
	
	@SuppressWarnings("unchecked")
	@JsonIgnore
	public Collection<Variable<?>> getOriginalVariables() {
		return CollectionUtils.select(getVariables(), new Predicate() {
			@Override
			public boolean evaluate(Object object) {
				return ((Variable<?>)object).getOriginalId() != null;
			}
		});
	}
	
	@SuppressWarnings("unchecked")
	@JsonIgnore
	public Collection<QuantitativeVariable> getOutputVariables() {
		return CollectionUtils.select(getQuantitativeVariables(), new Predicate() {
			@Override
			public boolean evaluate(Object object) {
				return ((QuantitativeVariable)object).getOriginalId() == null;
			}
		});
	}
	
	/**
	 * Returns the output variables linked to the entity associated to the default processing chain
	 * @param entity
	 * @return 
	 */
	@JsonIgnore
	public List<QuantitativeVariable> getDefaultProcessingChainOutputVariables() {
		List<QuantitativeVariable> variables = new ArrayList<QuantitativeVariable>();
		
		ProcessingChain processingChain = getWorkspace().getDefaultProcessingChain();
		List<CalculationStep> steps = processingChain.getCalculationSteps();
		for (CalculationStep step : steps) {
			QuantitativeVariable variable = (QuantitativeVariable) step.getOutputVariable();
			Entity outputEntity = variable.getEntity();
			if( outputEntity.getId().equals( this.getId() ) ){
				variables.add( variable );
			}
		}
		if( this.isSamplingUnit() ){
			QuantitativeVariable weightVar = getOutputVariable("weight");
			if( weightVar != null ) {
				variables.add( weightVar);
			}
		}
		
		return variables;
	}
	
	private Collection<Variable<?>> getVariables(Predicate predicate) {
		@SuppressWarnings("unchecked")
		Collection<Variable<?>> result = CollectionUtils.select(variables, predicate);
		return result;
	}

	public void removeVariable(Variable<?> variable) {
		this.variables.remove(variable);
	}
	
	public void removeVariables(Collection<Variable<?>> vars) {
		if ( CollectionUtils.isNotEmpty(vars) && CollectionUtils.isNotEmpty(this.variables) ) {
			this.variables.removeAll(vars);
		}
	}

	@JsonIgnore
	protected int getNextVariableSortOrder() {
		int result = 0;
		for ( Variable<?> v: variables ) {
			result = Math.max(v.getSortOrder(), result);
		}
		return result + 1;
	}

	// TODO move to Open Foris commons
	static <I,O extends I> List<O> selectInstancesOf(List<I> items, Class<O> type) {
		List<O> out = new ArrayList<O>();
		CollectionUtils.select(items, new InstanceofPredicate(type), out);
		return out;
	}

	public Variable<?> getClusterVariable() {
		return clusterVariable;
	}

	public void setClusterVariable(Variable<?> clusterVariable) {
		this.clusterVariable = clusterVariable;
	}

	/**
	 * Returns a quantity variable with the id passed as argument.
	 * @param variableId
	 * @return
	 */
	@JsonIgnore
	public QuantitativeVariable getQtyVariableById(int variableId) {
		for (QuantitativeVariable variable : getQuantitativeVariables()) {
			if(variable.getId().equals(variableId)){
				return variable;
			}
		}
		return null;
	}

	@JsonIgnore
	public QuantitativeVariable getQtyVariablePerHaById(int variableId) {
		for (QuantitativeVariable variable : getQuantitativeVariables()) {
			QuantitativeVariable variablePerHa = variable.getVariablePerHa();
			if(variablePerHa != null && variablePerHa.getId().equals(variableId)) {
				return variablePerHa;
			}
		}
		return null;
	}

	// returns true if at least one quantitative variable or output variable has an aggregate function associated
	// or if this is the sampling unit
	public boolean isAggregable() {
		// for now only if output variables have been defined
		return getOutputVariables().size() > 0 ;
//		
//		if( this.isSamplingUnit() ) {
//			return true;
//		}
//		
//		
//		for (QuantitativeVariable var : getQuantitativeVariables()) {
//			if( var.getAggregates().size() > 0 ){
//				return true;
//			}
//			QuantitativeVariable variablePerHa = var.getVariablePerHa();
//			if( variablePerHa != null ){
//				if( variablePerHa.getAggregates().size() >0 ){
//					return true;
//				}
//			}
//		}
//		
//		return false;
	}

	public QuantitativeVariable getOutputVariable(String variable) {
		for (QuantitativeVariable var : this.getQuantitativeVariables()) {
			if( var.getName().equals(variable) ){
				return var;
			}
		}
		return null;
	}
	
	@Override
	public int hashCode() {
		Integer id = getId();
		String name = getName();
		
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		Integer id = getId();
		String name = getName();
		
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Entity other = (Entity) obj;
		if (id == null) {
			if (other.getId() != null)
				return false;
		} else if (!id.equals(other.getId()))
			return false;
		if (name == null) {
			if (other.getName() != null)
				return false;
		} else if (!name.equals(other.getName()))
			return false;
		return true;
	}
	
	
}

