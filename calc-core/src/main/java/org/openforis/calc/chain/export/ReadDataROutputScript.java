package org.openforis.calc.chain.export;

import java.util.List;

import org.openforis.calc.engine.Workspace;
import org.openforis.calc.metadata.AuxiliaryTable;
import org.openforis.calc.metadata.Entity;
import org.openforis.calc.r.DbGetQuery;
import org.openforis.calc.r.RScript;
import org.openforis.calc.r.RVariable;
import org.openforis.calc.r.SetValue;
import org.openforis.calc.schema.EntityDataView;
import org.openforis.calc.schema.Schemas;

/**
 * 
 * @author M. Togna
 *
 */
public class ReadDataROutputScript extends ROutputScript {

	public ReadDataROutputScript(int index , CalculationStepsGroup group, Schemas schemas) {
		super( "read-data.R", createScript(group , schemas), Type.SYSTEM , index );
	}

	private static RScript createScript(CalculationStepsGroup group, Schemas schemas) {
		RScript r = r();
		
		Workspace workspace = group.getWorkspace();
		if( workspace.hasSamplingDesign() ){
			// add read base unit script
			Entity baseUnit 	= workspace.getSamplingUnit();
			
			RScript readEntity 	= createReadEntityScript(schemas, baseUnit);
			r.addScript( readEntity );
		}
		
		for (Integer entityId : group.activeEntityIds()) {
			Entity entity = workspace.getEntityById( entityId );
			
			RScript readEntity = createReadEntityScript( schemas , entity );
			r.addScript( readEntity );
		}
		
		List<AuxiliaryTable> tables = workspace.getAuxiliaryTables();
		for (AuxiliaryTable table : tables) {
			String name 	= table.getName();
			String schema 	= table.getSchema();
			RScript script 	= createReadDataScript(name , schema , name);
			r.addScript( script );
		}
		
		return r;
	}

	private static SetValue createReadEntityScript(Schemas schemas, Entity entity) {
		EntityDataView view 	= schemas.getDataSchema().getDataView( entity );
		String dataFrameName 	= entity.getName();
		String schema 			= view.getSchema().getName();
		String table	 		= view.getName();
		return createReadDataScript(dataFrameName, schema, table);
	}

	private static SetValue createReadDataScript(String dataFrameName, String schema, String table) {
		RVariable dataFrame 		= r().variable( dataFrameName );
		DbGetQuery select 			= r().dbGetQuery( CONNECTION_VAR, "select * from " + schema + "."+ table );
		SetValue setDataframeValue 	= r().setValue( dataFrame, select );
		return setDataframeValue;
	}

}
