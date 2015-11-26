package org.openforis.calc.chain.export;

import org.openforis.calc.engine.Workspace;
import org.openforis.calc.metadata.Entity;
import org.openforis.calc.metadata.Entity.Visitor;
import org.openforis.calc.metadata.EntityManager;
import org.openforis.calc.psql.CreateViewStep.AsStep;
import org.openforis.calc.psql.DropViewStep;
import org.openforis.calc.r.RScript;
import org.openforis.calc.schema.DataSchema;
import org.openforis.calc.schema.EntityDataView;
import org.openforis.calc.schema.Schemas;

/**
 * 
 * @author M. Togna
 *
 */
public class CloseChainROutputScript extends ROutputScript {

	public CloseChainROutputScript(int index , Workspace workspace , Schemas schemas ,EntityManager entityManager) {
		super( "close.R", createScript(workspace , schemas , entityManager), Type.SYSTEM , index );
	}

	private static RScript createScript(Workspace workspace, final Schemas schemas, final EntityManager entityManager) {
		final RScript r 			= r();
		final DataSchema schema 	= schemas.getDataSchema();
		// recreate view
		if( workspace.hasSamplingDesign() ){
			Entity samplingUnit = workspace.getSamplingUnit();
			samplingUnit.traverse( new Visitor() {
				@Override
				public void visit(Entity entity) {
					
					EntityDataView view 			= schema.getDataView( entity );
					DropViewStep dropViewIfExists 	= psql().dropViewIfExists( view );
					r.addScript(r().dbSendQuery( CONNECTION_VAR , dropViewIfExists ));
					
					AsStep createView 				= psql().createView( view ).as( entityManager.getViewSelect(entity, true) );
					r.addScript( r().dbSendQuery( CONNECTION_VAR, createView ) );
					
				}
			});
		}
					
		
		r.addScript( r().dbDisconnect(CONNECTION_VAR) );
		
		return r;
	}

}
