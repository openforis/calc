/**
 * 
 */
package org.openforis.calc.schema;

import java.math.BigDecimal;

import org.jooq.Field;
import org.openforis.calc.engine.Workspace;
import org.openforis.calc.metadata.Entity;
import org.openforis.calc.metadata.QuantitativeVariable;
import org.openforis.calc.persistence.jooq.AbstractJooqDao;
import org.springframework.stereotype.Repository;

/**
 * @author S. Ricci
 * TODO these operations are not linked to the input schema. find the right position for these methods 
 */
@Repository
public class InputSchemaDao extends AbstractJooqDao {

	public void addUserDefinedVariableColumn(QuantitativeVariable v) {
		InputTable table = getEntityDataTable(v);

		Field<BigDecimal> field = table.getQuantityField(v);
		
		psql()
			.alterTable(table)
			.addColumn(field)
			.execute();
	}

	public void dropUserDefinedVariableColumn(QuantitativeVariable v) {
		InputTable table = getEntityDataTable(v);

		Field<BigDecimal> field = table.getQuantityField(v);
		
		psql()
			.alterTable(table)
			.dropColumn(field, true)
			.execute();
	}
	
	private InputTable getEntityDataTable(QuantitativeVariable v) {
		Entity entity = v.getEntity();
		if(entity == null) {
			entity = v.getSourceVariable().getEntity();
		}
			
		Workspace ws = entity.getWorkspace();
		
		Schemas schemas = new Schemas(ws);
		InputSchema inputSchema = schemas.getInputSchema();
		
		
		InputTable table = inputSchema.getDataTable(entity);
		return table;
	}
}
