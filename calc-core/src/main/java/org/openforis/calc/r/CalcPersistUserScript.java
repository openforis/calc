package org.openforis.calc.r;


public class CalcPersistUserScript extends RScript {

	CalcPersistUserScript( RScript previous, String functionName, RVariable filename , int id ) {
		super(previous);
		
		append( "calc." );
		append( functionName );
		append( "( " );
		append( filename.toScript() );
		append( SPACE );
		append( COMMA );
		append( SPACE );
		append( new RScript().variable( id +"" ).toScript() );
		append(" )");
	}

}