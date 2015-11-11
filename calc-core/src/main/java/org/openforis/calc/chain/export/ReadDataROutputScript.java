package org.openforis.calc.chain.export;

import org.openforis.calc.engine.Workspace;
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
		
		for (Integer entityId : group.entityIds()) {
			Entity entity = workspace.getEntityById( entityId );
			
			RScript readEntity = createReadEntityScript( schemas , entity );
			r.addScript( readEntity );
		}
		
		return r;
	}

	private static SetValue createReadEntityScript(Schemas schemas, Entity entity) {
		EntityDataView view 		= schemas.getDataSchema().getDataView( entity );
		RVariable dataFrame 		= r().variable( entity.getName() );
		DbGetQuery select 			= r().dbGetQuery( CONNECTION_VAR, "select * from " + view.getSchema().getName() + "."+ view.getName() );
		SetValue setDataframeValue 	= r().setValue( dataFrame, select );
		return setDataframeValue;
	}

}
