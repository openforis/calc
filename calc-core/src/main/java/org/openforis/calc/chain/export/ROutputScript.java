/**
 * 
 */
package org.openforis.calc.chain.export;

import org.openforis.calc.psql.Psql;
import org.openforis.calc.r.RScript;
import org.openforis.calc.r.RVariable;

/**
 * @author M. Togna
 *
 */
public abstract class ROutputScript {
	static final RVariable CONNECTION_VAR 	= r().variable( "connection" );
	static final RVariable SCRIPT_DIR 		= r().variable( "scriptDir" );
	static final RVariable USER_SCRIPT_DIR 	= r().variable( "userScriptDir" );
	
	static final String FILE_SEPARATOR 	= "/";
	static final String DASH 			= "-";
	
	public enum Type{
		SYSTEM , USER;
		
		public String toString() {
			return super.toString().toLowerCase();
		};
	}
	
	private RScript rScript;
	private String fileName;
//	private Type type;
	private int index;
	
	ROutputScript( String fileName , RScript rScript ) {
		this.fileName 	= fileName;
		this.rScript 	= rScript;
	}

	ROutputScript( String fileName ,RScript rScript, Type type , int index ) {
		this.rScript 	= rScript;
//		this.type 		= type;
		this.index 		= index;
		
		this.fileName 	= type.toString() + FILE_SEPARATOR + index + DASH +  fileName;
	}

//	public String getDirectory() {
//		return directory;
//	}

	public RScript getRScript() {
		return rScript;
	}

	public String getFileName() {
		return fileName;
	}

	public int getIndex() {
		return index;
	}

	static RScript r(){
		return new RScript();
	}
	
	static Psql psql(){
		return new Psql();
	}
	
}
