package org.openforis.calc.chain.pre;

import static org.openforis.calc.persistence.jooq.Tables.CATEGORY;

import java.util.Collection;

import org.jooq.Select;
import org.openforis.calc.engine.Task;
import org.openforis.calc.metadata.CategoricalVariable;
import org.openforis.calc.psql.Psql.Privilege;
import org.openforis.calc.schema.CategoryDimensionTable;
import org.openforis.calc.schema.OutputSchema;
import org.springframework.transaction.annotation.Transactional;

/**
 * Copies category tables into the output schema.  Fails if output schema already exists.
 * 
 * @author A. Sanchez-Paus Diaz
 * @author G. Miceli
 */
public final class CreateCategoryDimensionTablesTask extends Task {
	@Override
	@Transactional
	protected void execute() throws Throwable {
		OutputSchema outputSchema = getOutputSchema();
		Collection<CategoryDimensionTable> tables = outputSchema.getCategoryDimensionTables();
		for (CategoryDimensionTable t : tables) {
			CategoricalVariable<?> var = t.getVariable();
			if ( !var.isDegenerateDimension() ) {
				Integer varId = var.getId();
				
				Select<?> select = psql()
					.select(CATEGORY.ID			.as(t.getIdField().getName()), 
							CATEGORY.CODE		.as(t.getCodeField().getName()),
							CATEGORY.CAPTION	.as(t.getCaptionField().getName()),
							CATEGORY.DESCRIPTION.as(t.getDescriptionField().getName()), 
							CATEGORY.SORT_ORDER	.as(t.getSortOrderField().getName()),
							CATEGORY.VALUE      .as(t.getValueField().getName()))
					.from(CATEGORY)
					.where(CATEGORY.VARIABLE_ID.eq(varId));
				
				if ( isDebugMode() ) {
					psql()
						.dropTableIfExists(t)
						.execute();
				}
				
				psql()
					.createTable(t)
					.as(select) 
					.execute();
			
				psql()
					.alterTable(t)
					.addPrimaryKey(t.getPrimaryKey())
					.execute();
				
				psql()
					.grant(Privilege.ALL)
					.on(t)
					.to(getSystemUser())
					.execute();
			}
		}
	}
}