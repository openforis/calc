package org.openforis.calc.chain.pre;

import java.util.List;

import org.openforis.calc.engine.SqlTask;
import org.openforis.calc.engine.Workspace;
import org.openforis.calc.metadata.Entity;

/**
 * 
 * @author G. Miceli
 * @author A. Sanchez-Paus Diaz 
 *
 */
public class AssignStrataTask extends SqlTask {
//	private static final String STRATUM_COLUMN = "_stratum";
//
//	@Override
//	protected void execute() throws Throwable {
//		Workspace ws = getWorkspace();
//		setDefaultSchema(ws.getOutputSchema());
//		List<Entity> entities = ws.getEntities();
//		for (Entity entity : entities) {
//			if (entity.isSamplingUnit()) {
//				assignStrata(entity);
//			}
//		}
//	}
//
//	private void assignStrata(Entity entity) {
//		String dataTable = entity.getDataTable();
//		String clusterColumn = entity.getClusterColumn();
//		String unitNoColumn = entity.getUnitNoColumn();
//		// add AOI id column to fact table output schema
//		executeSql("ALTER TABLE %s ADD COLUMN %s INTEGER", dataTable, STRATUM_COLUMN);
//		
//		String update = "UPDATE %s f "+
//					 	"SET %s = su.stratum "+
//					 	"FROM calc.sampling_unit su "+
//					 	"WHERE f.%s = su.unit_no";
//		if ( clusterColumn != null ) {
//			update += " AND f.%s = su.cluster";
//		}
//		
//		// update values
//		executeSql(update, dataTable, STRATUM_COLUMN, unitNoColumn, clusterColumn);
//	}
}
