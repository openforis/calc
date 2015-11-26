package org.openforis.calc.chain.export;

import java.util.Collection;

import org.jooq.Insert;
import org.jooq.Record;
import org.openforis.calc.engine.Workspace;
import org.openforis.calc.metadata.Entity;
import org.openforis.calc.metadata.Entity.Visitor;
import org.openforis.calc.metadata.EntityManager;
import org.openforis.calc.psql.CreateTableWithFieldsStep;
import org.openforis.calc.psql.CreateViewStep.AsStep;
import org.openforis.calc.psql.DropTableStep.CascadeStep;
import org.openforis.calc.psql.DropViewStep;
import org.openforis.calc.psql.Psql;
import org.openforis.calc.r.RScript;
import org.openforis.calc.schema.DataSchema;
import org.openforis.calc.schema.DataTable;
import org.openforis.calc.schema.EntityDataView;
import org.openforis.calc.schema.ResultTable;
import org.openforis.calc.schema.Schemas;

/**
 * 
 * @author M. Togna
 *
 */
public class ResetResultsROutputScript extends ROutputScript {

	public ResetResultsROutputScript(int index , Workspace workspace, Schemas schemas, EntityManager entityManager) {
		super( "reset-results.R", createScript(workspace , schemas, entityManager), Type.SYSTEM , index );
	}

	private static RScript createScript( Workspace workspace, final Schemas schemas, final EntityManager entityManager ) {
		final RScript r = r();
		
		Collection<Entity> rootEntities = workspace.getRootEntities();
		for ( Entity entity : rootEntities ){
			entity.traverse( new Visitor() {
				@Override
				public void visit(Entity entity) {
					resetResults( entity , schemas , r ,entityManager );			
				}

				
			});
		}		
		return r;
	}

	private static void resetResults(Entity entity, Schemas schemas, RScript r, EntityManager entityManager ){
		DataSchema schema 			= schemas.getDataSchema();
		ResultTable resultsTable 	= schema.getResultTable(entity);
		DataTable dataTable 		= schema.getDataTable(entity);
		EntityDataView dataView 	= schema.getDataView(entity);
		
		DropViewStep dropView = psql().dropViewIfExists( dataView );
		r.addScript( r().dbSendQuery( CONNECTION_VAR , dropView ) );
		
		if( resultsTable != null ) {
			//drop data view first
			CascadeStep dropResults = psql()
				.dropTableIfExists( resultsTable )
				.cascade();
			r.addScript( r().dbSendQuery( CONNECTION_VAR, dropResults) );
			
			CreateTableWithFieldsStep createResults = psql().createTable( resultsTable, resultsTable.fields() );
			r.addScript( r().dbSendQuery( CONNECTION_VAR, createResults) );
			
			Insert<Record> insert = psql()
					.insertInto( resultsTable, resultsTable.getIdField() )
					.select(
							new Psql()
							.select( dataTable.getIdField() )
							.from( dataTable )
					);
			r.addScript( r().dbSendQuery( CONNECTION_VAR, insert) );
			
		}
		
		AsStep createView = psql().createView( dataView ).as( entityManager.getViewSelect(entity) );
		r.addScript( r().dbSendQuery( CONNECTION_VAR, createView) );
		
	}
	
}
