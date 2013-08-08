package org.openforis.calc.chain.post;

import static org.openforis.calc.persistence.postgis.Psql.quote;

import java.util.List;

import org.openforis.calc.engine.SqlTask;
import org.openforis.calc.engine.Workspace;
import org.openforis.calc.metadata.AoiHierarchy;
import org.openforis.calc.metadata.AoiHierarchyLevel;
import org.openforis.calc.metadata.Entity;

/**
 * Task responsible for calculating the expansion factor for each stratum in all aoi levels.
 * Results will be stored in a table called _expf in the output schema
 * 
 * @author M. Togna
 */
public final class UpdateExpFactorTask extends SqlTask {
	
	private static final String EXPF_TABLE_NAME = "_expf";

	@Override
	protected void execute() throws Throwable {
		Workspace ws = getWorkspace();

		createExpfTable(ws.getOutputSchema());
		
		populateExpfTable(ws);
	}

	private void createExpfTable(String schemaName) {
		psql()
		.createTable(quote(schemaName)+"."+quote(EXPF_TABLE_NAME), "stratum_id integer not null", "aoi_id integer not null", "entity_id integer not null", "expf double precision not null")
		.execute()
		;
	}

	private void populateExpfTable(Workspace ws) {
		List<Entity> entities = ws.getEntities();
		List<AoiHierarchy> hierarchies = ws.getAoiHierarchies();
		
		for ( Entity entity : entities ) {
			// TODO now the expf is calculated only on entities called plot.
			if ( entity.getName().equals("plot") ) {
				String entityDataTable = entity.getDataTable();
				
				for ( AoiHierarchy hierarchy : hierarchies ) {
					List<AoiHierarchyLevel> levels = hierarchy.getLevels();
					
					for ( AoiHierarchyLevel level : levels ) {
						
						psql()
						.insertInto(quote(ws.getOutputSchema()) + "." + quote(EXPF_TABLE_NAME), "stratum_id", "aoi_id", "entity_id", "expf")
						.select("s.stratum_id" , "s.aoi_id" , "s.entity_id" , "s.area / sum(f.weight)")
						.from("calc.stratum_aoi_view s")
						.innerJoin(quote(ws.getOutputSchema()) + "." + quote(entityDataTable) + " f")
						.on("s.stratum_id = f._stratum_id")
						.and( "s.aoi_id = f."+quote(level.getFkColumn()) )
						.and("f.weight > 0")
						.where("s.workspace_id = "+ws.getId())
						.groupBy("s.stratum_id" , "s.aoi_id" , "s.area", "s.entity_id")
						.execute()
						;
					}

				}
				
			}
		}
	}

}
