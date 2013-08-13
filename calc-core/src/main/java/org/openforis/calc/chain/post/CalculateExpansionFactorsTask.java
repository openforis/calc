package org.openforis.calc.chain.post;

import static org.openforis.calc.persistence.postgis.Psql.*;

import java.util.List;

import org.openforis.calc.engine.SqlTask;
import org.openforis.calc.engine.Workspace;
import org.openforis.calc.metadata.AoiHierarchy;
import org.openforis.calc.metadata.AoiHierarchyLevel;
import org.openforis.calc.metadata.Entity;

/**
 * Task responsible for calculating the expansion factor for each stratum in all AOI levels.
 * Results will be stored in a table called _expf in the output schema
 * 
 * @author M. Togna
 * @author G. Miceli
 */
public final class CalculateExpansionFactorsTask extends SqlTask {
	
	public static final String EXPF_TABLE = "_expf";

	@Override
	protected void execute() throws Throwable {
		Workspace ws = getWorkspace();
		Integer wsId = ws.getId();
		String outputSchema = ws.getOutputSchema();
		String expfTable = table(outputSchema, EXPF_TABLE);

		createExpansionFactorTable(expfTable);
		
		insertExpansionFactors(ws, wsId, outputSchema, expfTable);
	}

	private void createExpansionFactorTable(String expfTable) {
		psql()
			.createTable(expfTable, 
					"stratum_id integer not null", 
					"aoi_id integer not null", 
					"entity_id integer not null", 
					"_expf double precision not null")
			.execute();
	}


	private void insertExpansionFactors(Workspace ws, Integer wsId, String outputSchema, String expfTable) {
		List<Entity> entities = ws.getEntities();
		List<AoiHierarchy> hierarchies = ws.getAoiHierarchies();
		
		for ( Entity entity : entities ) {
			if ( entity.isSamplingUnit() ) {
				String factTable = table(outputSchema, entity.getDataTable());
				
				for ( AoiHierarchy hierarchy : hierarchies ) {
					List<AoiHierarchyLevel> levels = hierarchy.getLevels();
					
					for ( AoiHierarchyLevel level : levels ) {
						String aoiFkColumn = quote(level.getFkColumn());
						
						insertExpansionFactors(wsId, expfTable, factTable, aoiFkColumn);
					}
				}
			}
		}
	}

	private void insertExpansionFactors(Integer wsId, String expfTable, String factTable, String aoiFkColumn) {
		psql()
			.insertInto(expfTable, "stratum_id", "aoi_id", "entity_id", "_expf")
			.select("s.stratum_id" , "s.aoi_id" , "s.entity_id" , "s.area / sum(f.weight)")
			.from("calc.stratum_aoi_view s")
			.innerJoin(factTable + " f")
			.on("s.stratum_id = f._stratum_id")
			.and( "s.aoi_id = f."+aoiFkColumn )
			.and("f.weight > 0")
			.where("s.workspace_id = "+wsId)
			.groupBy("s.stratum_id" , "s.aoi_id" , "s.area", "s.entity_id")
			.execute();
	}
}
