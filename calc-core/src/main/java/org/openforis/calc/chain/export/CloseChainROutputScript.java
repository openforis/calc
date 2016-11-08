package org.openforis.calc.chain.export;

import org.jooq.UpdateConditionStep;
import org.openforis.calc.engine.Worker;
import org.openforis.calc.engine.Workspace;
import org.openforis.calc.metadata.Entity;
import org.openforis.calc.metadata.Entity.Visitor;
import org.openforis.calc.metadata.EntityManager;
import org.openforis.calc.persistence.jooq.Tables;
import org.openforis.calc.persistence.jooq.tables.records.ProcessingChainRecord;
import org.openforis.calc.psql.CreateViewStep.AsStep;
import org.openforis.calc.psql.DropViewStep;
import org.openforis.calc.r.Paste;
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
		super( "close.R", createScript(index, workspace , schemas , entityManager), Type.SYSTEM , index );
	}

	private static RScript createScript(int index , Workspace workspace, final Schemas schemas, final EntityManager entityManager) {
		final RScript r 			= r();
		final DataSchema schema 	= schemas.getDataSchema();
		// recreate view
		if( workspace.hasSamplingDesign() ){
			Entity samplingUnit = workspace.getSamplingUnit();
			samplingUnit.traverse( new Visitor() {
				@Override
				public void visit(Entity entity) {
					
					EntityDataView view 			= schema.getDataView( entity );
					DropViewStep dropViewIfExists 	= psql().dropViewIfExistsLegacy( view );
					r.addScript(r().dbSendQuery( CONNECTION_VAR , dropViewIfExists ));
					
					AsStep createView 				= psql().createViewLegacy( view ).as( entityManager.getViewSelect(entity, true) );
					r.addScript( r().dbSendQuery( CONNECTION_VAR, createView ) );
					
				}
			});
		}
		UpdateConditionStep<ProcessingChainRecord> updateChainStatus = psql()
			.update(Tables.PROCESSING_CHAIN)
			.set( Tables.PROCESSING_CHAIN.STATUS , Worker.Status.COMPLETED)
			.where( Tables.PROCESSING_CHAIN.ID.eq(workspace.getDefaultProcessingChain().getId()) );
		
		r.addScript( r().dbSendQuery(CONNECTION_VAR, updateChainStatus));
		
		r.addScript( r().dbDisconnect(CONNECTION_VAR) );
		
		r.addScript( r().comment("processing chain end time") );
		
		r.addScript( r().setValue(r().variable("calc.endTime") , r().rScript("Sys.time()") ) );
		
		Paste paste = r().paste(r().rScript("'Processing chain successfully executed in'"), r().rScript("as.numeric((calc.endTime - calc.startTime) , units='secs')"), "' '");
		paste = r().paste(paste, r().rScript("'seconds'"), "' '");
		r.addScript( r().calcInfo(index+"-close.R", paste ));
		
		return r;
	}

}
