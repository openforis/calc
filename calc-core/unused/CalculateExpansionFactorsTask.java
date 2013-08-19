package org.openforis.calc.chain.post;

import static org.openforis.calc.persistence.postgis.Psql.quote;

import java.util.List;

import org.openforis.calc.engine.Task;
import org.openforis.calc.engine.Workspace;
import org.openforis.calc.metadata.AoiHierarchy;
import org.openforis.calc.metadata.AoiHierarchyLevel;
import org.openforis.calc.metadata.Entity;
import org.openforis.calc.persistence.postgis.Psql;

/**
 * Task responsible for calculating the expansion factor for each stratum in all AOI levels.
 * Results will be stored in a table called _expf in the output schema
 * 
 * @author M. Togna
 * @author G. Miceli
 */
public final class CalculateExpansionFactorsTask extends Task {
	
	private static final String EXPF_TABLE_NAME = "_expf";

	@Override
	protected void execute() throws Throwable {
		Workspace ws = getWorkspace();
		List<Entity> entities = ws.getEntities();
		List<AoiHierarchy> hierarchies = ws.getAoiHierarchies();
		
		for ( Entity entity : entities ) {
			// TODO now the expf is calculated only on entities called plot.
			if ( entity.getName().equals("plot") ) {
				String table = quote(ws.getOutputSchema()) + "." + quote(entity.getDataTable());
				
				for ( AoiHierarchy hierarchy : hierarchies ) {
					List<AoiHierarchyLevel> levels = hierarchy.getLevels();
					
					for ( AoiHierarchyLevel level : levels ) {
						String expfColumn = "_"+level.getName()+"_expf";
						String levelFkColumn = quote(level.getFkColumn());
						Integer wsId = ws.getId();
						
						psql()
							.alterTable(table)
							.addColumn(expfColumn, Psql.FLOAT8)
							.execute();
						
						Psql select = new Psql()
								.select("s.stratum_id" , "s.aoi_id" , "s.entity_id" , "s.area / sum(f.weight) as expf")
								.from("calc.stratum_aoi_view s")
								.innerJoin(table + " f")
								.on("s.stratum_id = f._stratum_id")
								.and( "s.aoi_id = f."+levelFkColumn )
								.and("f.weight > 0")
								.where("s.workspace_id = "+wsId)
								.groupBy("s.stratum_id" , "s.aoi_id" , "s.area", "s.entity_id");

						psql()
							.with("tmp", select)
							.update(table + " f")
							.set(expfColumn+"= tmp.expf")
							.from("tmp")
							.where("tmp.stratum_id = f.stratum_id")
							.and("tmp.aoid_id = )
							.insertInto( + quote(EXPF_TABLE_NAME), "stratum_id", "aoi_id", "entity_id", "expf")
							.execute()
						;
					}

				}
				
			}
		}
	}

}
