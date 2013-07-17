package org.openforis.calc.collect;

import java.util.ArrayList;
import java.util.List;

import org.openforis.calc.engine.Workspace;
import org.openforis.calc.metadata.BinaryVariable;
import org.openforis.calc.metadata.CategoricalVariable;
import org.openforis.calc.metadata.Entity;
import org.openforis.calc.metadata.QuantitativeVariable;
import org.openforis.calc.metadata.Variable;
import org.openforis.collect.relational.model.CodeTable;
import org.openforis.collect.relational.model.Column;
import org.openforis.collect.relational.model.DataColumn;
import org.openforis.collect.relational.model.DataTable;
import org.openforis.collect.relational.model.RelationalSchema;
import org.openforis.collect.relational.model.Table;
import org.openforis.idm.metamodel.AttributeDefinition;
import org.openforis.idm.metamodel.BooleanAttributeDefinition;
import org.openforis.idm.metamodel.CodeAttributeDefinition;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.metamodel.NumberAttributeDefinition;

/**
 * Converts IDM survey metadata and Collect RDB schema into Calc metadata
 *   
 * @author G. Miceli
 * @author S. Ricci
 *
 */
public class CollectMetadataConverter {
	
	/**
	 * Converts a RDB {@link RelationalSchema} into a list of Calc {@link Entity} objects
	 * 
	 * @param workspace
	 * @param schema
	 * @return
	 */
	public List<Entity> convert(Workspace workspace, RelationalSchema schema) {
		List<Entity> entities = new ArrayList<Entity>();
		
		List<Table<?>> tables = schema.getTables();
		for (Table<?> table : tables) {
			if ( table instanceof DataTable ) {
				Entity entity = convert(workspace, (DataTable) table);
				entities.add(entity);
			} else if ( table instanceof CodeTable ) {
				
			}
		}
		return entities;
	}

	/**
	 * Converts a RDB {@link DataTable} into a Calc {@link Entity}
	 * 
	 * @param workspace
	 * @param table
	 * @return
	 */
	protected Entity convert(Workspace workspace, DataTable table) {
		NodeDefinition nodeDefn = table.getNodeDefinition();
		if ( nodeDefn instanceof EntityDefinition || 
				nodeDefn instanceof AttributeDefinition && nodeDefn.isMultiple() ) {
			Entity entity = new Entity();
			entity.setName(table.getName());
			entity.setWorkspace(workspace);
			List<Column<?>> columns = table.getColumns();
			for (Column<?> column : columns) {
				if ( column instanceof DataColumn ) {
					Variable variable = convert(column);
					if ( variable != null ) {
						entity.addVariable(variable);
					}
				}
			}
			return entity;
		} else {
			throw new IllegalArgumentException("Entity definition or multiple attribute definition associated to DataTable expected");
		}
	}

	/**
	 * Converts a {@link Column} related to a single attribute definition into a {@link Variable}.
	 * Columns are translated into a specific {@link Variable} type according to the associated {@link NodeDefinition} type.
	 * 
	 * @param column
	 * @return
	 */
	protected Variable convert(Column<?> column) {
		NodeDefinition columnFieldDefn = ((DataColumn) column).getNodeDefinition();
		AttributeDefinition attributeDefn;
		if ( columnFieldDefn instanceof AttributeDefinition ) {
			attributeDefn = (AttributeDefinition) columnFieldDefn;
		} else {
			attributeDefn = (AttributeDefinition) columnFieldDefn.getParentDefinition();
		}
		Variable variable = null;
		if ( attributeDefn instanceof AttributeDefinition && ! attributeDefn.isMultiple() ) {
			if ( attributeDefn instanceof BooleanAttributeDefinition ) {
				variable = new BinaryVariable();
			} else if ( attributeDefn instanceof CodeAttributeDefinition && 
					columnFieldDefn.getName().equals(CodeAttributeDefinition.CODE_FIELD) ) {
				variable = new CategoricalVariable();
			} else if ( attributeDefn instanceof NumberAttributeDefinition ) {
				if ( columnFieldDefn.getName().equals(NumberAttributeDefinition.VALUE_FIELD) ) {
					variable = new QuantitativeVariable();
					//TODO set unit...
				}
			}
			if ( variable != null ) {
				variable.setName(column.getName()); //TODO check this
				variable.setValueColumn(column.getName());
			}
		}
		return variable;
	}
}
