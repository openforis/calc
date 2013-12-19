package org.openforis.calc.schema;

import java.math.BigDecimal;
import java.util.Collection;

import org.jooq.Record;
import org.jooq.Select;
import org.jooq.SelectQuery;
import org.jooq.TableField;
import org.openforis.calc.metadata.Entity;
import org.openforis.calc.metadata.QuantitativeVariable;
import org.openforis.calc.psql.Psql;

/**
 * @author S. Ricci
 * @author Mino Togna
 */
public class EntityDataView extends DataTable {

	private static final long serialVersionUID = 1L;

	private InputSchema schema;

	/**
	 * Select to create this view
	 */
//	private SelectQuery<Record> select;

	private TableField<Record, BigDecimal> plotAreaField;

	public EntityDataView(Entity entity, InputSchema schema) {
		super(entity, getViewName(entity), schema);
		this.schema = schema;
		createPrimaryKeyField();
		createParentIdField();
		// TODO: coordinates disabled now
		// createCoordinateFields();
		createCategoryFields(entity);
		createQuantityFields();
		createTextFields();
//		initSelect();

		ResultTable resultTable = this.schema.getResultTable(entity);
		if (resultTable != null) {
			TableField<Record, BigDecimal> plotArea = resultTable.getPlotArea();
			if (plotArea != null) {
				this.plotAreaField = super.createField(plotArea.getName(), Psql.DOUBLE_PRECISION, this);
			}
		}

	}

	private void createQuantityFields() {
		Collection<QuantitativeVariable> quantitativeVariables = getEntity().getOriginalQuantitativeVariables();
		for (QuantitativeVariable var : quantitativeVariables) {
			createQuantityField(var, var.getOutputValueColumn());
		}
	}



//	private void addUniqueNameFields(SelectQuery<Record> select, Field<?>[] fields) {
//		for (Field<?> field : fields) {
//			Field<?> existingField = getFieldByName(select, field.getName());
//			if (existingField == null) {
//				select.addSelect(field);
//			}
//		}
//	}

//	private Field<?> getFieldByName(SelectQuery<Record> select, String name) {
//		for (Field<?> field : select.getSelect()) {
//			if (field.getName().equals(name)) {
//				return field;
//			}
//		}
//		return null;
//	}

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

	public static String getViewName(Entity entity) {
		return entity.getDataTable() + "_view";
	}

	public Select<?> getSelect() {
		return getSelect( false );
	}
	
	public Select<?> getSelect(boolean joinWithResults) {
		InputTable table = schema.getDataTable(getEntity());
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
		
		// for every ancestor, add join condition and select fields
		Entity currentEntity = getEntity();
		while (currentEntity.getParent() != null) {
			InputTable currentTable = schema.getDataTable(currentEntity);
			Entity parentEntity = currentEntity.getParent();
			InputTable parentTable = schema.getDataTable(parentEntity);

			select.addSelect( parentTable.getIdField() );
			select.addSelect( parentTable.getCategoryValueFields() );
			select.addSelect( parentTable.getCategoryIdFields() );
			select.addSelect( parentTable.getTextFields() );
			
			select.addJoin(parentTable, currentTable.getParentIdField().eq(parentTable.getIdField()));
			
//			addUniqueNameFields(select, parentTable.fields());
			currentEntity = parentEntity;
		}
		
		// add join with results table if exits
		ResultTable resultTable = schema.getResultTable(getEntity());
		if( joinWithResults && resultTable != null ){
			// select output variables from results table
			for (QuantitativeVariable var : getEntity().getOutputVariables()) {
				select.addSelect( resultTable.getQuantityField(var) );
			}
			if( resultTable.getPlotArea() != null ){
				select.addSelect( resultTable.getPlotArea() );
			}
			
			select.addJoin(resultTable, resultTable.getIdField().eq(table.getIdField()));
		}
		
		return select;
	}

	public TableField<Record, BigDecimal> getPlotAreaField() {
		return plotAreaField;
	}

}
