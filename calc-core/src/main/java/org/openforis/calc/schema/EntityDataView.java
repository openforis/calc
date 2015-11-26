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
	
	public Field<Long> getAncestorIdField( Integer entityId ){
		return ancestorIdFields.get( entityId );
	}
	
	public TableField<Record, BigDecimal> getPlotAreaField() {
		return plotAreaField;
	}

}
