package org.openforis.calc.schema;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.jooq.Field;
import org.jooq.JoinType;
import org.jooq.Record;
import org.jooq.Select;
import org.jooq.SelectQuery;
import org.jooq.TableField;
import org.jooq.impl.SQLDataType;
import org.openforis.calc.metadata.CategoricalVariable;
import org.openforis.calc.metadata.Entity;
import org.openforis.calc.metadata.MultiwayVariable;
import org.openforis.calc.metadata.QuantitativeVariable;
import org.openforis.calc.psql.Psql;
import org.openforis.commons.collection.CollectionUtils;

/**
 * @author Mino Togna
 * @author S. Ricci
 */
public class EntityDataView extends DataTable {

	private static final long serialVersionUID = 1L;

	private DataSchema schema;

	private TableField<Record, BigDecimal> plotAreaField;

	private Map<Integer, Field<Long>> ancestorIdFields;
	
	public EntityDataView(Entity entity, DataSchema schema) {
		super(entity, entity.getDataView(), schema);
		this.schema = schema;
		createPrimaryKeyField();
		createParentIdField();
		createSamplingUnitIdField();
		// TODO: coordinates disabled now
		// createCoordinateFields();
		createCategoryFields(entity);
		createQuantityFields();
		createTextFields();

		createAncestorIdFields();
		
		ResultTable resultTable = this.schema.getResultTable(entity);
		if (resultTable != null) {
			TableField<Record, BigDecimal> plotArea = resultTable.getPlotArea();
			if (plotArea != null) {
				this.plotAreaField = super.createField(plotArea.getName(), Psql.DOUBLE_PRECISION, this);
			}
		}
		
		createWeightField();
	}

	private void createAncestorIdFields() {
		this.ancestorIdFields = new HashMap<Integer, Field<Long>>();
		Entity entity = getEntity();
		Entity parent = entity.getParent();
		while( parent != null ){
			TableField<Record,Long> field = createField( parent.getIdColumn() , SQLDataType.BIGINT, this );
			ancestorIdFields.put( parent.getId() , field );
			
			parent = parent.getParent();
		}
	}

	private void createQuantityFields() {
		// first output variables
		for (QuantitativeVariable var : getEntity().getOutputVariables() ) {
			createQuantityField(var, var.getOutputValueColumn());
		}
		// then all original qty variables up to root
		Entity currentEntity = getEntity();
		while (currentEntity != null) {
			Collection<QuantitativeVariable> quantitativeVariables = currentEntity.getOriginalQuantitativeVariables();
			for (QuantitativeVariable var : quantitativeVariables) {
				createQuantityField(var, var.getOutputValueColumn());
			}
			currentEntity = currentEntity.getParent();
		}
	}

	protected void createCategoryFields(Entity entity) {
		
		Entity currentEntity = getEntity();
		while (currentEntity != null) {
			super.createCategoryValueFields(currentEntity, true);
			super.createCategoryIdFields(currentEntity, true);
			
			currentEntity = currentEntity.getParent();
		}
	}

	@Override
	protected void createQuantityFields(boolean input, boolean variableAggregates) {
		Entity currentEntity = getEntity();
		while (currentEntity != null) {
			createQuantityFields(currentEntity, input, variableAggregates);
			currentEntity = currentEntity.getParent();
		}
	}

	@Override
	protected void createTextFields() {
		Entity currentEntity = getEntity();
		while (currentEntity != null) {
			createTextFields(currentEntity);
			currentEntity = currentEntity.getParent();
		}
	}

	public Collection<Field<Long>> getAncestorIdFields() {
		Collection<Field<Long>> ids = ancestorIdFields.values();
		return CollectionUtils.unmodifiableCollection( ids );
	}
	
	public Select<?> getSelect() {
		return getSelect( true );
	}
	
	public Select<?> getSelect(boolean joinWithResults) {
		Entity entity = getEntity();
		DataTable table = schema.getDataTable(entity);
		SelectQuery<Record> select = new Psql().selectQuery();
		select.addFrom(table);
//		select.addSelect(table.fields());
		
		select.addSelect( table.getIdField() );
////		select.addSelect( table.getCategoryIdFields() );
//		select.addSelect( table.getCategoryValueFields() );
		select.addSelect( table.getTextFields() );
		select.addSelect( table.getAoiIdFields() );
		for (QuantitativeVariable var : entity.getOriginalQuantitativeVariables()) {
			select.addSelect( table.getQuantityField(var) );
		}
		for (CategoricalVariable<?> var : entity.getOriginalCategoricalVariables()) {
			select.addSelect( table.getCategoryIdField(var) );
			select.addSelect( table.getCategoryValueField(var) );
		}
		// add weight column if sampling unit
		if(  entity.isSamplingUnit() ) {
			select.addSelect( table.getWeightField() );
		}
		
		// for every ancestor, add join condition and select fields
		Entity currentEntity = entity;
		while (currentEntity.getParent() != null) {
			DataTable currentTable = schema.getDataTable(currentEntity);
			Entity parentEntity = currentEntity.getParent();
			DataTable parentTable = schema.getDataTable( parentEntity );

			select.addSelect( parentTable.getIdField() );
			select.addSelect( parentTable.getCategoryValueFields() );
			select.addSelect( parentTable.getCategoryIdFields() );
			select.addSelect( parentTable.getTextFields() );
			if(  parentEntity.isSamplingUnit() ) {
				select.addSelect( parentTable.getWeightField() );
			}
			for ( QuantitativeVariable var : parentEntity.getOriginalQuantitativeVariables() ) {
				select.addSelect( parentTable.getQuantityField(var) );
			}
//			if( parentTable.getWeightField() != null ){
//				select.addSelect( parentTable.getWeightField() );
//			}
			select.addJoin( parentTable, currentTable.getParentIdField().eq(parentTable.getIdField()) );
			
			if( parentEntity.isSamplingUnit() && joinWithResults ){
				addJoinWithResultsTable(parentEntity, parentTable, select);
			}
//			addUniqueNameFields( select, parentTable.fields() );
			currentEntity = parentEntity;
		}
		
		if( joinWithResults ){
			// add join with results table if exits
			addJoinWithResultsTable(entity, table, select);
		}
		
		return select;
	}

	private void addJoinWithResultsTable(Entity entity, DataTable table, SelectQuery<Record> select) {
		ResultTable resultTable = schema.getResultTable(entity);
		if( resultTable != null ){
			// select output variables from results table
			for (QuantitativeVariable var : entity.getDefaultProcessingChainQuantitativeOutputVariables()) {
				select.addSelect( resultTable.getQuantityField(var) );
			}
			for ( MultiwayVariable var : entity.getDefaultProcessingChainCategoricalOutputVariables() ){
				select.addSelect( resultTable.getCategoryIdField(var) );
				select.addSelect( resultTable.getCategoryValueField(var) );
			}
			if( resultTable.getPlotArea() != null ) {
				select.addSelect( resultTable.getPlotArea() );
			}
			
			select.addJoin(resultTable, JoinType.LEFT_OUTER_JOIN, resultTable.getIdField().eq(table.getIdField()));
		}
	}

	public TableField<Record, BigDecimal> getPlotAreaField() {
		return plotAreaField;
	}

}
