package org.openforis.calc.metadata;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
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
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.openforis.calc.common.NamedUserObject;
import org.openforis.calc.engine.Workspace;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Provides metadata about a particular unit of observation, calculation or
 * analysis. Entities are anything which may have attributes for variables
 * associated with it.
 * 
 * @author G. Miceli
 * @author M. Togna
 */
/**
 * @author G. Miceli
 * @author M. Togna
 *
 */
@javax.persistence.Entity
@Table(name = "entity")
public class Entity extends NamedUserObject {
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "workspace_id")
	@JsonIgnore
	private Workspace workspace;

	@Column(name = "data_table")
	private String dataTable;

	@Column(name = "id_column")
	private String idColumn;

	@Column(name = "parent_id_column")
	private String parentIdColumn;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "parent_entity_id")
	private Entity parent;
	
	@Column(name = "sort_order")
	private int sortOrder;
	
	@Column(name = "input")
	private boolean input;
	
	@Column(name = "override")
	private boolean override;
	
	@Column(name = "x_column")
	private String xColumn;
	
	@Column(name = "y_column")
	private String yColumn;
	
	@Column(name = "srs_column")
	private String srsColumn;
	
	@Column(name = "location_column")
	private String locationColumn;
	
	@Column(name = "sampling_unit")
	private boolean samplingUnit;
	
	@Column(name = "unit_of_analysis")
	private boolean unitOfAnalysis;

	@OneToMany(mappedBy = "entity", fetch = FetchType.EAGER)
	@OrderBy("sortOrder")
	@Fetch(FetchMode.SUBSELECT) 
	@Cascade(CascadeType.ALL)
	private List<Variable<?>> variables = new ArrayList<Variable<?>>();

	@OneToMany(mappedBy = "parent", fetch = FetchType.EAGER)
	@OrderBy("sortOrder")
	@Fetch(FetchMode.SUBSELECT) 
	@Cascade(CascadeType.ALL)
	private List<Entity> children = new ArrayList<Entity>();

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "cluster_variable_id")
	private Variable<?> clusterVariable;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "unit_no_variable_id")
	private Variable<?> unitNoVariable;
	
	@Column(name = "original_id")
	private Integer originalId;

	public Workspace getWorkspace() {
		return this.workspace;
	}
	
	public void setWorkspace(Workspace workspace) {
		this.workspace = workspace;
	}
	
	public String getDataTable() {
		return dataTable;
	}

	public void setDataTable(String table) {
		this.dataTable = table;
	}
	
	public void addVariable(Variable<?> variable) {
		variable.setEntity(this);
		variables.add(variable);
	}
	
	public List<Variable<?>> getVariables() {
		return Collections.unmodifiableList(variables);
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
	
	public List<VariableAggregate> getVariableAggregates() {
		List<VariableAggregate> aggs = new ArrayList<VariableAggregate>();
		for (Variable<?> var : variables) {
			if ( var instanceof QuantitativeVariable ) {
				aggs.addAll(((QuantitativeVariable) var).getAggregates());
			}
		}
		return Collections.unmodifiableList(aggs);
	}
	
	public boolean hasOverriddenVariables() {
		return ! getOverriddenVariables().isEmpty();
	}
	
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

	public String getXColumn() {
		return xColumn;
	}

	public void setYColumn(String yColumn) {
		this.yColumn = yColumn;
	}
	
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

	public boolean isGeoreferenced() {
		return ((xColumn != null && yColumn != null) || locationColumn != null);
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
		return samplingUnit;
	}

	public void setSamplingUnit(boolean samplingUnit) {
		this.samplingUnit = samplingUnit;
	}
	
	public List<Entity> getChildren() {
		return Collections.unmodifiableList(children);
	}

	public boolean isUnitOfAnalysis() {
		return unitOfAnalysis;
	}

	public void setUnitOfAnalysis(boolean unitOfAnalysis) {
		this.unitOfAnalysis = unitOfAnalysis;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public List<CategoricalVariable<?>> getCategoricalVariables() {
		return Collections.unmodifiableList(selectInstancesOf((List)variables, CategoricalVariable.class));
	}

	public List<QuantitativeVariable> getQuantitativeVariables() {
		return Collections.unmodifiableList(selectInstancesOf(variables, QuantitativeVariable.class));
	}

	public List<TextVariable> getTextVariables() {
		return Collections.unmodifiableList(selectInstancesOf(variables, TextVariable.class));
	}
	
	public Collection<Variable<?>> getOverriddenVariables() {
		return getVariables(new Predicate() {
			@Override
			public boolean evaluate(Object object) {
				return ((Variable<?>) object).isOverride();
			}
		});
	}
	
	public Collection<Variable<?>> getNotOverriddenVariables() {
		return getVariables(new Predicate() {
			@Override
			public boolean evaluate(Object object) {
				return ! ((Variable<?>) object).isOverride();
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
	
	public void removeVariables(Collection<Variable<?>> variables) {
		if ( CollectionUtils.isNotEmpty(variables) && CollectionUtils.isNotEmpty(this.variables) ) {
			this.variables.removeAll(variables);
		}
	}

	public int getVariableNextSortOrder() {
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
		result = prime * result + (samplingUnit ? 1231 : 1237);
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
		if (samplingUnit != other.samplingUnit)
			return false;
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

}

