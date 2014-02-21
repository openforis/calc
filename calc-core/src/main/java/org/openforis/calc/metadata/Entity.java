package org.openforis.calc.metadata;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Stack;

import javax.persistence.Column;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.collections.functors.InstanceofPredicate;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.openforis.calc.common.NamedUserObject;
import org.openforis.calc.engine.Workspace;
import org.openforis.calc.r.RScript;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Provides metadata about a particular unit of observation, calculation or
 * analysis. Entities are anything which may have attributes for variables
 * associated with it.
 * 
 * @author G. Miceli
 * @author M. Togna
 */
@javax.persistence.Entity
@Table(name = "entity")
public class Entity extends NamedUserObject {
	
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "workspace_id")
	@JsonIgnore
	private Workspace workspace;

	@JsonIgnore
	@Column(name = "data_table")
	private String dataTable;

	@JsonIgnore
	@Column(name = "id_column")
	private String idColumn;

	@JsonIgnore
	@Column(name = "parent_id_column")
	private String parentIdColumn;
	
	@JsonIgnore
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "parent_entity_id")
	@Fetch(FetchMode.SELECT) 
	private Entity parent;
	
	@JsonIgnore
	@Column(name = "sort_order")
	private int sortOrder;
	
	@JsonIgnore
	@Column(name = "input")
	private boolean input;
	
	@JsonIgnore
	@Column(name = "override")
	private boolean override;
	
	@JsonIgnore
	@Column(name = "x_column")
	private String xColumn;
	
	@JsonIgnore
	@Column(name = "y_column")
	private String yColumn;
	
	@JsonIgnore
	@Column(name = "srs_column")
	private String srsColumn;
	
	@JsonIgnore
	@Column(name = "location_column")
	private String locationColumn;
	
//	@JsonIgnore
//	@Column(name = "sampling_unit")
//	private boolean samplingUnit;
	
	@JsonIgnore
	@Column(name = "unit_of_analysis")
	private boolean unitOfAnalysis;

	@JsonIgnore
	@OneToMany(mappedBy = "entity", fetch = FetchType.EAGER, cascade = javax.persistence.CascadeType.ALL, orphanRemoval = true)
	@OrderBy("sortOrder")
	@Fetch(FetchMode.SUBSELECT) 
	@Cascade(CascadeType.ALL)
	private List<Variable<?>> variables = new ArrayList<Variable<?>>();

	@JsonIgnore
	@OneToMany(mappedBy = "parent", fetch = FetchType.EAGER)
	@OrderBy("sortOrder")
	@Fetch(FetchMode.SUBSELECT) 
	@Cascade(CascadeType.ALL)
	private List<Entity> children = new ArrayList<Entity>();

	@JsonIgnore
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "cluster_variable_id")
	private Variable<?> clusterVariable;

	@JsonIgnore
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "unit_no_variable_id")
	private Variable<?> unitNoVariable;

	@JsonIgnore
	@Column(name = "original_id")
	private Integer originalId;

	@Column(name = "plot_area_script")
	private String plotAreaScript;
	
	public Workspace getWorkspace() {
		return this.workspace;
	}
	
	public void setWorkspace(Workspace workspace) {
		this.workspace = workspace;
	}
	
	public String getDataTable() {
		return dataTable;
	}

	public String getDataView() {
		return String.format( "%s_view", getName() );
	}

	public void setDataTable(String table) {
		this.dataTable = table;
	}
	
	public void addVariable(Variable<?> variable) {
		variable.setEntity(this);
		variable.setSortOrder(getNextVariableSortOrder());
		variables.add(variable);
	}
	
	public List<Variable<?>> getVariables() {
		return org.openforis.commons.collection.CollectionUtils.unmodifiableList( variables );
	}

	public Variable<?> getVariableByOriginalId(int id) {
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
	
	public Variable<?> getVariable(String name) {
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
	public Variable<?> findVariable(String name) {
		Entity entity = this;

		while( entity != null ){
			Variable<?> variable = entity.getVariable(name);
		
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
			if ( descendant.isOverride() || hasOverriddenVariables() ) {
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
	
	public int getSortOrder() {
		return sortOrder;
	}
	
	public void setSortOrder(int sortOrder) {
		this.sortOrder = sortOrder;
	}
	
	public void setInput(boolean input) {
		this.input = input;
	}
	
	public boolean isInput() {
		return input;
	}
	
	public void setOverride(boolean override) {
		this.override = override;
	}
	
	public boolean isOverride() {
		return override;
	}
	
	public void setXColumn(String xColumn) {
		this.xColumn = xColumn;
	}

	@JsonIgnore	
	public String getXColumn() {
		return xColumn;
	}

	public void setYColumn(String yColumn) {
		this.yColumn = yColumn;
	}
	
	@JsonIgnore	
	public String getYColumn() {
		return yColumn;
	}
	
	public void setSrsColumn(String srsColumn) {
		this.srsColumn = srsColumn;
	}

	public String getSrsColumn() {
		return srsColumn;
	}

	public String getLocationColumn() {
		return locationColumn;
	}

	public void setLocationColumn(String locationColumn) {
		this.locationColumn = locationColumn;
	}

	@JsonIgnore	
	public boolean isGeoreferenced() {
		// right now only if this is sampling unit or parent entity is sampling unit
		return ( this.isSamplingUnit() ) || ( this.parent!=null && this.parent.isSamplingUnit() ); 
//		return ((xColumn != null && yColumn != null) || locationColumn != null);
	}

	public String getIdColumn() {
		return idColumn;
	}

	public void setIdColumn(String idColumn) {
		this.idColumn = idColumn;
	}

	public String getParentIdColumn() {
		return parentIdColumn;
	}

	public void setParentIdColumn(String parentIdColumn) {
		this.parentIdColumn = parentIdColumn;
	}

	public boolean isSamplingUnit() {
		if( this.getId() == null ){
			throw new IllegalStateException("Id cannot be null");
		}
		return getWorkspace().isSamplingUnit(getId());
	}

//	public void setSamplingUnit(boolean samplingUnit) {
//		this.samplingUnit = samplingUnit;
//	}
	
	public List<Entity> getChildren() {
		return Collections.unmodifiableList(children);
	}

	public boolean isUnitOfAnalysis() {
		return unitOfAnalysis;
	}

	public void setUnitOfAnalysis(boolean unitOfAnalysis) {
		this.unitOfAnalysis = unitOfAnalysis;
	}

	public String getPlotAreaScript() {
		return plotAreaScript;
	}
	
	public RScript getPlotAreaRScript() {
		if(StringUtils.isBlank(plotAreaScript)){
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
	
	public void setPlotAreaScript(String plotAreaScript) {
		this.plotAreaScript = plotAreaScript;
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
				return ((Variable<?>) object).isOverride();
			}
		});
	}
	
	@JsonIgnore
	public Collection<Variable<?>> getNotOverriddenVariables() {
		return getVariables(new Predicate() {
			@Override
			public boolean evaluate(Object object) {
				return ! ((Variable<?>) object).isOverride();
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

	public Variable<?> getUnitNoVariable() {
		return unitNoVariable;
	}

	public void setUnitNoVariable(Variable<?> plotVariable) {
		this.unitNoVariable = plotVariable;
	}
	
	public Integer getOriginalId() {
		return originalId;
	}
	
	public void setOriginalId(Integer originalId) {
		this.originalId = originalId;
	}
	
	@JsonInclude
	public Integer getParentId(){
		if(parent == null){
			return null;
		} else {
			return parent.getId();
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result
				+ ((clusterVariable == null) ? 0 : clusterVariable.hashCode());
		result = prime * result
				+ ((dataTable == null) ? 0 : dataTable.hashCode());
		result = prime * result
				+ ((idColumn == null) ? 0 : idColumn.hashCode());
		result = prime * result + (input ? 1231 : 1237);
		result = prime * result
				+ ((locationColumn == null) ? 0 : locationColumn.hashCode());
		result = prime * result
				+ ((originalId == null) ? 0 : originalId.hashCode());
		result = prime * result + (override ? 1231 : 1237);
		result = prime * result
				+ ((parentIdColumn == null) ? 0 : parentIdColumn.hashCode());
//		result = prime * result + (samplingUnit ? 1231 : 1237);
		result = prime * result + sortOrder;
		result = prime * result
				+ ((srsColumn == null) ? 0 : srsColumn.hashCode());
		result = prime * result
				+ ((unitNoVariable == null) ? 0 : unitNoVariable.hashCode());
		result = prime * result + (unitOfAnalysis ? 1231 : 1237);
		result = prime * result
				+ ((variables == null) ? 0 : variables.hashCode());
		result = prime * result + ((xColumn == null) ? 0 : xColumn.hashCode());
		result = prime * result + ((yColumn == null) ? 0 : yColumn.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		Entity other = (Entity) obj;
		if (clusterVariable == null) {
			if (other.clusterVariable != null)
				return false;
		} else if (!clusterVariable.equals(other.clusterVariable))
			return false;
		if (dataTable == null) {
			if (other.dataTable != null)
				return false;
		} else if (!dataTable.equals(other.dataTable))
			return false;
		if (idColumn == null) {
			if (other.idColumn != null)
				return false;
		} else if (!idColumn.equals(other.idColumn))
			return false;
		if (input != other.input)
			return false;
		if (locationColumn == null) {
			if (other.locationColumn != null)
				return false;
		} else if (!locationColumn.equals(other.locationColumn))
			return false;
		if (originalId == null) {
			if (other.originalId != null)
				return false;
		} else if (!originalId.equals(other.originalId))
			return false;
		if (override != other.override)
			return false;
		if (parentIdColumn == null) {
			if (other.parentIdColumn != null)
				return false;
		} else if (!parentIdColumn.equals(other.parentIdColumn))
			return false;
//		if (samplingUnit != other.samplingUnit)
//			return false;
		if (sortOrder != other.sortOrder)
			return false;
		if (srsColumn == null) {
			if (other.srsColumn != null)
				return false;
		} else if (!srsColumn.equals(other.srsColumn))
			return false;
		if (unitNoVariable == null) {
			if (other.unitNoVariable != null)
				return false;
		} else if (!unitNoVariable.equals(other.unitNoVariable))
			return false;
		if (unitOfAnalysis != other.unitOfAnalysis)
			return false;
		if (variables == null) {
			if (other.variables != null)
				return false;
		} else if (!variables.equals(other.variables))
			return false;
		if (xColumn == null) {
			if (other.xColumn != null)
				return false;
		} else if (!xColumn.equals(other.xColumn))
			return false;
		if (yColumn == null) {
			if (other.yColumn != null)
				return false;
		} else if (!yColumn.equals(other.yColumn))
			return false;
		return true;
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
	
}

