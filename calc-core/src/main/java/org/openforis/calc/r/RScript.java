/**
 * 
 */
package org.openforis.calc.r;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.openforis.calc.metadata.Variable;

/**
 * @author Mino Togna
 * 
 *         R scripts wrapper
 */
public class RScript {
	// =====================================
	// R symbols
	// =====================================
	public static final String DOLLAR				= "$";
	public static final String SPACE 				= " ";
	public static final String ASSIGN 				= "<-";
	public static final String COMMA 				= ",";
	public static final String NEW_LINE 			= "\n";
	public static final String NULL 				= "NULL";
	public static final String NOT 					= "!";
	public static final String PLATFORM$FILE_SEP 	= ".Platform$file.sep";
	
	// common static R scripts
	private static RScript CALC_COMMON_SCRIPT;
	private static RScript ERROR_ESTIMATION_SCRIPT;
	
	// previous r script
	private RScript previous;
	// stringbuilder that contains the script
	private StringBuilder sb;
	
	private Set<String> variables;

	public RScript() {
		this( (Collection<Variable<?>>)null );
	}

	public RScript(Collection<Variable<?>> variables) {
		this.sb = new StringBuilder();
		this.variables = new HashSet<String>();
		if ( variables != null ) {
			parseVariables(variables);
		}
	}

	protected RScript(RScript previous) {
		this((Collection<Variable<?>>)null);
		this.previous = previous;
	}

	// =====================================
	// R public functions
	// =====================================
	public RVariable variable(String dataframe, String name) {
		return new RVariable(this, dataframe, name);
	}

	public RVariable variable(RScript dataframe, String name) {
		return new RVariable(this, dataframe.toScript(), name);
	}

	public RVariable variable(String name) {
		return this.variable((String) null, name);
	}

	public AsCharacter asCharacter(RScript script) {
		return new AsCharacter(this, script);
	}

	public SetValue setValue(RVariable variable, RScript script) {
		return new SetValue(this, variable, script);
	}

	public DbConnect dbConnect(RVariable driver, String host, String database, String user, String password, int port) {
		return new DbConnect(this, driver, host, database, user, password, port);
	}

	public DbDisconnect dbDisconnect(RVariable connection) {
		return new DbDisconnect(this, connection);
	}

	public DbSendQuery dbSendQuery(RVariable connection, Object query) {
		return new DbSendQuery(this, connection, query);
	}

	public DbGetQuery dbGetQuery(RVariable connection, Object query) {
		return new DbGetQuery(this, connection, query);
	}

	public DbWriteTable dbWriteTable(RVariable connection, String name, RVariable variable) {
		return new DbWriteTable(this, connection, name, variable);
	}

	public DbRemoveTable dbRemoveTable(RVariable connection, String name) {
		return new DbRemoveTable(this, connection, name);
	}
	
	public DbExistsTable dbExistsTable(RVariable connection, String name) {
		return new DbExistsTable(this, connection, name);
	}
	
	public DbDriver dbDriver(String name) {
		return new DbDriver(this, name);
	}
	
	public DbQuoteString dbQuoteString(RVariable conn, RVariable x){
		return new DbQuoteString( this , conn, x );
	}
//	calc.getQuotedFileContent <- function( filename ){
	
	public CalcGetQuotedFileContent calcGetQuotedFileContent( RVariable fileName ){
		return new CalcGetQuotedFileContent( this, fileName );
	}
	
	public If rIf(RScript condition, RScript script) {
		return new If(this, condition, script);
	}
	
	public IfElse ifElse( RScript condition, RScript leftValue, RScript rightValue ) {
		return new IfElse( this, condition, leftValue, rightValue );
	}
	
	public Not not(RScript script) {
		return new Not(this, script);
	}
	

	public Library library(String name) {
		return new Library(this, name);
	}

	public Div div(RScript numerator, RScript denumenator) {
		return new Div(this, numerator, denumenator);
	}

	public<T extends Object> RVector c(@SuppressWarnings("unchecked") T... values) {
		return new RVector(this, values);
	}
	
	public RDataFrame dataFrame() {
		return dataFrame((String[]) null, (RVector[]) null);
	}
	
	public RDataFrame dataFrame(String[] columnNames, RVector[] columns) {
		return new RDataFrame(columnNames, columns);
	}

	public Try rTry(RScript... scripts) {
		return new Try(this, scripts);
	}

	public Sqldf sqldf(String script) {
		return new Sqldf(this, script);
	}

	public Source source( String fileName ){
		return new Source( this, fileName);
	}

	public Setwd setWd( RScript script) {
		return new Setwd(this, script);
	}

	public CheckError checkError(RVariable variable) {
		return checkError(variable, null);
	}
	
	public CheckError checkError(RVariable variable, RVariable connection) {
		return new CheckError(this, variable, connection);
	}

	public Paste paste( RVariable variable1 , RVariable variable2 , String sep ){
		return new Paste(this, variable1, variable2, sep);
	}
	
	public FileInfo fileInfo( RVariable rVariable ){
		return new FileInfo( this , rVariable );
	}
	
	public ReadChar readChar( RVariable con, RVariable nchars ){
		return new ReadChar( this, con, nchars );
	}
	
	public CalculateQuantityError calculateQuantityError( RVariable data, RVariable plots , RVariable strata ) {
		return new CalculateQuantityError( this, data , plots , strata );
	}
	
	public CalculateAreaError calculateAreaError(  RVariable plots , RVariable strata ) {
		return new CalculateAreaError( this, plots , strata );
	}

	// simple text passed as script. no parsing done here. it's assumed that the
	// script is correct
	public RScript rScript(String script) {
		RScript rScript = new RScript(this);
		rScript.append(script);
		return rScript;
	}

	public RScript rScript(String script, Collection<Variable<?>> variables) {
		RScript rScript = new RScript(this);
		rScript.append(script);
		rScript.parseVariables(variables);
		return rScript;
	}
	
	public RScript addNewLine() {
		this.append( NEW_LINE );
		return this;
	}
	
	public RScript addScript(RScript script) {
		if( script != null && !script.isEmpty() ){
			this.append( script.toString() );
			this.append( NEW_LINE );
		}
		return this;
	}
	
	public boolean isEmpty(){
		return sb.length() <= 0;
	}
	
	// =====================================
	// methods to convert the instance into an R script
	// =====================================
	protected void append(Object s) {
		sb.append(s);
	}

	protected String toScript() {
		return sb.toString();
	}

	@Override
	public String toString() {
		// append previous script
		StringBuilder sb = new StringBuilder();
		if (previous != null) {
			sb.append(previous.toString());
		}

		// append script
		String script = this.toScript();
		if (!StringUtils.isBlank(script)) {
			script = script.trim();
//			script = script.replaceAll("[\r\n]+", "\n");
			sb.append(script);
			if (!script.endsWith(";")) {
				sb.append(";");
			}
			sb.append("\n");
		}

		return sb.toString();
	}

	// =====================================
	// getters, hash code and equals methods
	// =====================================
	public Set<String> getVariables() {
		return variables;
	}

	public int hashCode() {
		return toString().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		} else if (obj instanceof RScript) {
			return toString().equals(obj.toString());
		}
		return false;
	}

	// =====================================
	// utility methods
	// =====================================
	protected static String escape(String name) {
		return "\"" + name + "\"";
	}

	protected void reset() {
		this.sb = new StringBuilder();
	}

	private void parseVariables(Collection<Variable<?>> variables) {
		String script = this.toScript();
		for (Variable<?> variable : variables) {
			String variableName = variable.getName();

			Pattern pattern = Pattern.compile("\\b" + variableName + "\\b");
			Matcher matcher = pattern.matcher(script);
			if (matcher.find()) {
				this.variables.add(variableName);
			}

		}
	}

	public static RScript getCalcCommonScript() {
		if( CALC_COMMON_SCRIPT == null ) {
			CALC_COMMON_SCRIPT = loadScript( "org/openforis/calc/r/functions.R" );
		}
		return CALC_COMMON_SCRIPT;
	}
	
	@Deprecated
	public static RScript getErrorEstimationScript() {
		if( ERROR_ESTIMATION_SCRIPT == null ) {
			ERROR_ESTIMATION_SCRIPT = loadScript( "org/openforis/calc/r/error-point-estimators.R" );
		}
		return ERROR_ESTIMATION_SCRIPT;
	}

	private static RScript loadScript( String filePath ) {
		InputStream stream = RScript.class.getClassLoader().getResourceAsStream( filePath );
		try {
			String string = IOUtils.toString(stream);
			RScript rScript = new RScript().rScript(string);
			return rScript;
		} catch (IOException e) {
			throw new IllegalStateException( "unable to find " + filePath, e );
		} finally {
			try {
				stream.close();
			} catch (IOException e) {
			}
		}
	}

}
