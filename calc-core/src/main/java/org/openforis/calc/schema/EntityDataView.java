package org.openforis.calc.schema;

import java.math.BigDecimal;
import java.util.Collection;

import org.jooq.JoinType;
import org.jooq.Record;
import org.jooq.Select;
import org.jooq.SelectQuery;
import org.jooq.TableField;
import org.openforis.calc.metadata.Entity;
import org.openforis.calc.metadata.MultiwayVariable;
import org.openforis.calc.metadata.QuantitativeVariable;
import org.openforis.calc.psql.Psql;

/**
 * @author S. Ricci
 * @author Mino Togna
 */
public class EntityDataView extends DataTable {

	private static final long serialVersionUID = 1L;

	private DataSchema schema;

	/**
	 * Select to create this view
	 */
//	private SelectQuery<Record> select;

	private TableField<Record, BigDecimal> plotAreaField;

	public EntityDataView(Entity entity, DataSchema schema) {
		super(entity, entity.getDataView(), schema);
		this.schema = schema;
		createPrimaryKeyField();
		createParentIdField();
		// TODO: coordinates disabled now
		// createCoordinateFields();
		createCategoryFields(entity);
		createQuantityFields();
		createTextFields();

		ResultTable resultTable = this.schema.getResultTable(entity);
		if (resultTable != null) {
			TableField<Record, BigDecimal> plotArea = resultTable.getPlotArea();
			if (plotArea != null) {
				this.plotAreaField = super.createField(plotArea.getName(), Psql.DOUBLE_PRECISION, this);
			}
		}
		
		createWeightField();
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

	public Select<?> getSelect() {
		return getSelect( false );
	}
	
	public Select<?> getSelect(boolean joinWithResults) {
		DataTable table = schema.getDataTable(getEntity());
		SelectQuery<Record> select = new Psql().selectQuery();
		select.addFrom(table);
//		select.addSelect(table.fields());
		
		select.addSelect( table.getIdField() );
		select.addSelect( table.getCategoryIdFields() );
		select.addSelect( table.getCategoryValueFields() );
		select.addSelect( table.getTextFields() );
		select.addSelect( table.getAoiIdFields() );
		
		for (QuantitativeVariable var : getEntity().getOriginalQuantitativeVariables()) {
			select.addSelect( table.getQuantityField(var) );
		}
		// add weight column if sampling unit
		if( getEntity().isSamplingUnit() ) {
			select.addSelect( table.getWeightField() );
		}
		
		// for every ancestor, add join condition and select fields
		Entity currentEntity = getEntity();
		while (currentEntity.getParent() != null) {
			DataTable currentTable = schema.getDataTable(currentEntity);
			Entity parentEntity = currentEntity.getParent();
			DataTable parentTable = schema.getDataTable(parentEntity);

			select.addSelect( parentTable.getIdField() );
			select.addSelect( parentTable.getCategoryValueFields() );
			select.addSelect( parentTable.getCategoryIdFields() );
			select.addSelect( parentTable.getTextFields() );
			for ( QuantitativeVariable var : parentEntity.getOriginalQuantitativeVariables() ) {
				select.addSelect( parentTable.getQuantityField(var) );
			}
			
			select.addJoin(parentTable, currentTable.getParentIdField().eq(parentTable.getIdField()));
			
//			addUniqueNameFields( select, parentTable.fields() );
			currentEntity = parentEntity;
		}
		
		// add join with results table if exits
		ResultTable resultTable = schema.getResultTable(getEntity());
		if( resultTable != null ){
			// select output variables from results table
			for (QuantitativeVariable var : getEntity().getDefaultProcessingChainQuantitativeOutputVariables()) {
				select.addSelect( resultTable.getQuantityField(var) );
			}
			for ( MultiwayVariable var : getEntity().getDefaultProcessingChainCategoricalOutputVariables() ){
				select.addSelect( resultTable.getCategoryIdField(var) );
				select.addSelect( resultTable.getCategoryValueField(var) );
			}
			if( resultTable.getPlotArea() != null ) {
				select.addSelect( resultTable.getPlotArea() );
			}
			
			select.addJoin(resultTable, JoinType.LEFT_OUTER_JOIN, resultTable.getIdField().eq(table.getIdField()));
		}
		
		return select;
	}

	public TableField<Record, BigDecimal> getPlotAreaField() {
		return plotAreaField;
	}

}
