package org.openforis.calc.metadata;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;

import org.apache.commons.collections.CollectionUtils;
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
	private Boolean input;
	
	@Column(name = "override")
	private Boolean override;
	
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

	public List<VariableAggregate> getVariableAggregates() {
		List<VariableAggregate> aggs = new ArrayList<VariableAggregate>();
		for (Variable<?> var : variables) {
			if ( var instanceof QuantitativeVariable ) {
				aggs.addAll(((QuantitativeVariable) var).getAggregates());
			}
		}
		return Collections.unmodifiableList(aggs);
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
	
	public void setInput(Boolean input) {
		this.input = input;
	}
	
	public boolean isInput() {
		return input;
	}
	
	public void setOverride(Boolean override) {
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
}

