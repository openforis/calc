package org.openforis.calc.r;


public class CalcLog extends RScript {

	public enum CalcLogLevel {
		INFO, WARN, DEBUG, ERROR;
	}
	
	CalcLog( RScript previous, CalcLogLevel logLevel, String step , RScript message ) {
		super(previous);
		
		append( "calc." );
		append( logLevel.toString().toLowerCase() );
		append( "('" );
		append( step );
		append( "'" );
		append( SPACE );
		append( COMMA );
		append( SPACE );
		append( message.toScript() );
		append(" )");
	}

}