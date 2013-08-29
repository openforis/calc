package org.openforis.calc.chain.post;


import static org.jooq.impl.DSL.sum;
import static org.openforis.calc.persistence.jooq.Tables.STRATUM_AOI_VIEW;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;

import org.jooq.Field;
import org.openforis.calc.engine.Task;
import org.openforis.calc.engine.Workspace;
import org.openforis.calc.metadata.AoiHierarchy;
import org.openforis.calc.metadata.AoiHierarchyLevel;
import org.openforis.calc.metadata.Entity;
import org.openforis.calc.persistence.jooq.tables.StratumAoiViewTable;
import org.openforis.calc.psql.Psql;
import org.openforis.calc.schema.DataTable;
import org.openforis.calc.schema.ExpansionFactorTable;
import org.openforis.calc.schema.FactTable;
import org.openforis.calc.schema.OutputSchema;

/**
 * Task responsible for calculating the expansion factor for each stratum in all AOI levels.
 * Results will be stored in a table called _expf in the output schema
 * 
 * @author M. Togna
 * @author G. Miceli
 */
public final class CalculateExpansionFactorsTask extends Task {
	
	public static final String EXPF_TABLE = "_expf";
	
	@Override
	protected void execute() throws Throwable {
		Workspace workspace = getWorkspace();
		Integer workspaceId = workspace.getId();
		OutputSchema outputSchema = getOutputSchema();
		ExpansionFactorTable expf = outputSchema.getExpansionFactorTable();  
				
		if ( isDebugMode() ) {
			psql()
				.dropTableIfExists(expf)
				.execute();
		}

		psql()	
			.createTable(expf, expf.fields())
			.execute();
		
		psql()
			.alterTable(expf)
			.addPrimaryKey(expf.getPrimaryKey())
			.execute();
		
		List<AoiHierarchy> hierarchies = workspace.getAoiHierarchies();
		Collection<FactTable> factTables = outputSchema.getFactTables();
		for (DataTable factTable : factTables) {
			Entity entity = factTable.getEntity();
			if ( entity.isSamplingUnit() ) {
				for ( AoiHierarchy hierarchy : hierarchies ) {
					List<AoiHierarchyLevel> levels = hierarchy.getLevels();
					for ( AoiHierarchyLevel level : levels ) {
						Field<Integer> aoiId = factTable.getAoiIdField(level);

						StratumAoiViewTable s = STRATUM_AOI_VIEW.as("s");
						Field<Integer> stratumId = factTable.getStratumIdField();
						Field<BigDecimal> weight = factTable.getWeightField();
						
						psql()
							.insertInto(expf, expf.STRATUM_ID, expf.AOI_ID, expf.ENTITY_ID, expf.EXPF)
							.select(new Psql()
								.select(s.STRATUM_ID, s.AOI_ID, s.ENTITY_ID, s.AREA.divide(sum(weight)))
								.from(s)
								.join(factTable)
									.on(s.STRATUM_ID.eq(stratumId))
									.and(s.AOI_ID.eq(aoiId))
									.and(weight.gt(BigDecimal.ZERO))
								.where(s.WORKSPACE_ID.eq(workspaceId))
								.groupBy(s.STRATUM_ID, s.AOI_ID, s.ENTITY_ID, s.AREA)
							)
							.execute();
					}
				}
			}
		}
	}
}

