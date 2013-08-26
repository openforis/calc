/**
 * 
 */
package org.openforis.calc.schema;

import java.util.List;

import org.openforis.calc.metadata.Entity;
import org.openforis.calc.metadata.QuantitativeVariable;
import org.openforis.calc.metadata.Variable;
import org.openforis.calc.metadata.VariableAggregate;
import org.openforis.calc.persistence.postgis.Psql;

/**
 * @author G. Miceli
 * 
 */
public class FactTable extends DataTable {

	private static final long serialVersionUID = 1L;
	private static final String FACT_TABLE_NAME_FORMAT = "_%s_fact";
	
	FactTable(Entity entity, OutputSchema schema, OutputDataTable sourceTable) {
		super(entity, getName(entity), schema, sourceTable);
		createCategoryFields(entity);
		createStratumIdField();
		createAoiIdFields();
		createAggregateFields(entity);
	}

	/**
	 * Recursively from root unit of analysis
	 */
	private void createCategoryFields(Entity entity) {
		Entity parent = entity.getParent();
		if ( parent != null && parent.isUnitOfAnalysis() ) {
			createCategoryFields(parent);
		}
		createCategoryFields(entity, false);
	}

	private void createAggregateFields(Entity entity) {
		List<Variable> variables = entity.getVariables();
		for (Variable var : variables) {
			if ( var instanceof QuantitativeVariable ) {
				createAggregateFields((QuantitativeVariable) var);
			}
		}
	}
	
	private void createAggregateFields(QuantitativeVariable var) {
		List<VariableAggregate> aggregates = var.getAggregates();
		for (VariableAggregate agg : aggregates) {
			createField(agg.getName(), Psql.DOUBLE_PRECISION, this);
		}
	}

	private static String getName(Entity entity) {
		return String.format(FACT_TABLE_NAME_FORMAT, entity.getDataTable());
	}
}
