package org.openforis.calc.chain.export;

import java.util.List;

import org.openforis.calc.engine.Workspace;
import org.openforis.calc.r.DbSendQuery;
import org.openforis.calc.r.RScript;
import org.openforis.calc.schema.DataSchema;
import org.openforis.calc.schema.ErrorTable;
import org.openforis.calc.schema.FactTable;
import org.openforis.calc.schema.Schemas;

/**
 * 
 * @author M. Togna
 *
 */
public class CreateErrorTablesROutputScript extends ROutputScript {

	public CreateErrorTablesROutputScript( int index, Workspace workspace , Schemas schemas ) {
		super( "create-error-result-tables.R", createScript(workspace , schemas), Type.SYSTEM, index );
	}

	private static RScript createScript( Workspace workspace, Schemas schemas ){
		RScript r 					= r();
		
		DataSchema schema 			= schemas.getDataSchema();
		List<FactTable> factTables 	= schema.getFactTables();
		
		for ( FactTable factTable : factTables ){
			List<ErrorTable> errorTables = factTable.getErrorTables();
			for ( ErrorTable errorTable : errorTables ){
				
				// drop error table
				DbSendQuery dropErrorTable = r().dbSendQuery( CONNECTION_VAR , psql().dropTableIfExistsLegacy(errorTable) );
				r.addScript( dropErrorTable );
				// create error table
				DbSendQuery createErrorTable = r().dbSendQuery( CONNECTION_VAR , psql().createTable(errorTable, errorTable.fields()) );
				r.addScript( createErrorTable );
			}
		
		}
		return r;
	}

}
