package org.openforis.calc.chain.export;

import org.jooq.impl.SchemaImpl;
import org.openforis.calc.persistence.DBProperties;
import org.openforis.calc.psql.Psql;
import org.openforis.calc.r.DbConnect;
import org.openforis.calc.r.Paste;
import org.openforis.calc.r.RScript;
import org.openforis.calc.r.RVariable;
import org.openforis.calc.r.SetValue;
import org.openforis.calc.schema.Schemas;

/**
 * 
 * @author M. Togna
 *
 */
public class InitChainROutputScript extends ROutputScript {

	public InitChainROutputScript(int index , DBProperties dbProperties , Schemas schemas) {
		super( "init.R", createScript(dbProperties , schemas), Type.SYSTEM , index );
	}

	private static RScript createScript(DBProperties dbProperties, Schemas schemas) {
		RScript r = r();
		
		r.addScript( RScript.getCalcCommonScript() );
		
		// create driver
		RVariable driver = r().variable("driver");
		SetValue setDriver = r().setValue(driver, r().dbDriver("PostgreSQL"));
		r.addScript( setDriver);
		
		// create db connection
		DbConnect dbConnect = r().dbConnect( driver, dbProperties.getHost(), dbProperties.getDatabase(), dbProperties.getUser(), dbProperties.getPassword(), dbProperties.getPort() );
		r.addScript( r().setValue( CONNECTION_VAR, dbConnect) );
		
		// set search path to current and public schemas
		r.addScript(r().dbSendQuery( CONNECTION_VAR, new Psql().setDefaultSchemaSearchPath(schemas.getDataSchema(), new SchemaImpl("public"))));
		
		// set current dir 
//		RScript dirname = r().rScript( "dirname(sys.frame(1)$ofile)" );
//		RScript setDirName = r().setValue( SCRIPT_DIR , dirname );
//		r.addScript( setDirName );
//		
//		Paste paste = r().paste( SCRIPT_DIR, r().variable("\"user\""), ".Platform$file.sep" );
//		SetValue setUserScriptDir = r().setValue( USER_SCRIPT_DIR , paste );
//		r.addScript(setUserScriptDir);
		return r;
	}


}
