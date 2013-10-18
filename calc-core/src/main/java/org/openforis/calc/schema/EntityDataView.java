package org.openforis.calc.schema;

import org.jooq.Field;
import org.jooq.Record;
import org.jooq.Select;
import org.jooq.SelectQuery;
import org.openforis.calc.metadata.Entity;
import org.openforis.calc.psql.Psql;

/**
 * 
 * @author S. Ricci
 * 
 */
public class EntityDataView extends DataTable {

	public static final String VIEW_SUFFIX = "_view";

	private static final long serialVersionUID = 1L;

	private InputSchema inputSchema;

	/**
	 * Select to create this view
	 */
	private SelectQuery<Record> select;

	public EntityDataView(Entity entity, InputSchema schema) {
		super(entity, getViewName(entity), schema);
		this.inputSchema = schema;

		createPrimaryKeyField();
		createParentIdField();
		// TODO: coordinates disabled now
		// createCoordinateFields();
		createQuantityFields(true);
		createTextFields();
		initSelect();
	}

	private void initSelect() {
		InputTable table = inputSchema.getDataTable(getEntity());
		select = new Psql().selectQuery();
		select.addFrom(table);
		select.addSelect(table.fields());

		// for every ancestor, add join condition and select fields
		Entity currentEntity = getEntity();
		while (currentEntity.getParent() != null) {
			InputTable currentTable = inputSchema.getDataTable(currentEntity);
			Entity parentEntity = currentEntity.getParent();
			InputTable parentTable = inputSchema.getDataTable(parentEntity);

			select.addJoin(parentTable, currentTable.getParentIdField().eq(parentTable.getIdField()));

			addUniqueNameFields(select, parentTable.fields());
			currentEntity = parentEntity;
		}

	}

	private void addUniqueNameFields(SelectQuery<Record> select, Field<?>[] fields) {
		for (Field<?> field : fields) {
			Field<?> existingField = getFieldByName(select, field.getName());
			if (existingField == null) {
				select.addSelect(field);
			}
		}
	}

	private Field<?> getFieldByName(SelectQuery<Record> select, String name) {
		for (Field<?> field : select.getSelect()) {
			if ( field.getName().equals(name) ) {
				return field;
			}
		}
		return null;
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
		return entity.getDataTable() + VIEW_SUFFIX;
	}

	public Select<?> getSelect() {
		return select;
	}

}
