/**
 * 
 */
package org.openforis.calc.r;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.openforis.calc.metadata.Variable;

/**
 * @author Mino Togna
 * 
 * R scripts wrapper 
 */
public class RScript {
	// =====================================
	// R symbols
	// =====================================
	protected static final String DOLLAR = "$";
	protected static final String SPACE = " ";
	protected static final String ASSIGN = "<-";
	protected static final String COMMA = ",";
	protected static final String NULL = "NULL";
	
	// previous r script
	private RScript previous;
	// stringbuilder that contains the script
	private StringBuilder sb;
	
	private Set<String> variables;

	public RScript() {
		this((Collection<Variable<?>>)null);
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
		return new RVariable(this, dataframe, name) ;
	}
	
	public RVariable variable(RVariable dataframe, String name) {
		return new RVariable(this, dataframe.toScript(), name) ;
	}
	
	public RVariable variable(String name) {
		return this.variable( (String)null, name ) ;
	}
	
	public AsCharacter asCharacter(RScript script) {
		return new AsCharacter(this, script) ;
	}
	
	public SetValue setValue(RVariable variable, RScript script) {
		return new SetValue(this, variable, script) ;
	}
	
	public DbConnect dbConnect(RVariable driver, String host, String database, String user, String password, int port) {
		return new DbConnect(this, driver, host, database, user, password, port);
	}
	
	public DbDisconnect dbDisconnect(RVariable connection) {
		return new DbDisconnect(this, connection);
	}
	
	public DbSendQuery dbSendQuery(RVariable connection, Object query) {
		return new DbSendQuery(this, connection, query) ;
	}

	public DbGetQuery dbGetQuery(RVariable connection, Object query) {
		return new DbGetQuery(this, connection, query) ;
	}
	
	public DbWriteTable dbWriteTable(RVariable connection, String name, RVariable variable) {
		return new DbWriteTable(this, connection, name, variable) ;
	}
	
	public DbRemoveTable dbRemoveTable(RVariable connection, String name) {
		return new DbRemoveTable(this, connection, name) ;
	}
	
	public DbDriver dbDriver(String name) {
		return new DbDriver(this, name) ;
	}
	
	public Library library(String name) {
		return new Library(this, name);
	}
	
	public Div div(RScript numerator, RScript denumenator) {
		return new Div(this, numerator, denumenator);
	}

	public RVector c(Object... values) {
		return new RVector(this, values);
	}
	
	public RNamedVector c(String name, Object... values) {
		return new RNamedVector(this, name, values);
	}
	
	public RDataFrame dataFrame(RNamedVector... columns) {
		return new RDataFrame(columns);
	}
	
	public Try rTry(RScript... scripts) {
		return new Try(this, scripts);
	}
	
	public CheckError checkError(RVariable variable, RVariable connection) {
		return new CheckError(this, variable, connection);
	}
	
	// simple text passed as script. no parsing done here. it's assumed that the script is correct
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
		if(! StringUtils.isBlank(script) ){
			script = script.trim();
			script = script.replaceAll("[\r\n]+","\n");
			sb.append(script);
			if( ! script.endsWith(";") ) {
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
		if( obj == null ){
			return false;
		} else if(obj instanceof RScript) {
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
			if(script.contains(variableName)){
				this.variables.add(variableName);
			}
			
		}
		//TODO find a pattern 
//		Pattern p = Pattern.compile(CalculationStep.VARIABLE_PATTERN);
//		Matcher m = p.matcher(script);
//		while (m.find()) {
//			String variable = m.group(1);
//			variables.add(variable);
//		}
	}
	
	public static RScript getCalcRScript(){
		InputStream stream = RScript.class.getClassLoader().getResourceAsStream("org/openforis/calc/r/functions.R");
		try {
			String string = IOUtils.toString(stream);
			RScript rScript = new RScript().rScript(string);
			return rScript;
		} catch (IOException e) {
			throw new IllegalStateException("unable to find org/openforis/calc/r/functions.R", e);
		}
	}
	
}
