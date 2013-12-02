/**
 * 
 */
package org.openforis.calc.module.r;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.sql.DataSource;

import org.apache.commons.lang3.StringUtils;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.TableField;
import org.jooq.UniqueKey;
import org.jooq.UpdateQuery;
import org.jooq.UpdateSetFirstStep;
import org.jooq.UpdateSetMoreStep;
import org.jooq.impl.DSL;
import org.openforis.calc.chain.CalculationStep;
import org.openforis.calc.engine.Workspace;
import org.openforis.calc.metadata.Entity;
import org.openforis.calc.metadata.QuantitativeVariable;
import org.openforis.calc.metadata.Variable;
import org.openforis.calc.psql.Psql;
import org.openforis.calc.schema.EntityDataView;
import org.openforis.calc.schema.InputTable;
import org.openforis.calc.schema.Schemas;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * This class is responsible for converting one or more calculation step to an R
 * script
 * 
 * @author Mino Togna
 * 
 */
// @Component
public class CalculationStepRScriptConverter {

	@Autowired
	private DataSource dataSource;

	private StringBuilder sb;

	private Workspace workspace;
	private Schemas schemas;
	
	public CalculationStepRScriptConverter(Workspace workspace) {
		this.sb = new StringBuilder();
		this.workspace = workspace;
		this.schemas = new Schemas(workspace);
	}

	public String toRScript(Entity entity, CalculationStep... steps) {
		appendLibrary();
		appendOpenConnection();
		appendSteps(entity, steps);

		return sb.toString();
	}

	/**
	 * Append library declarations
	 */
	private void appendLibrary() {
		sb.append("library('lmfor');");
		appendNewLine();
		sb.append("library('RPostgreSQL');");
		appendNewLine();
		appendNewLine();
	}

	// driver <- dbDriver('PostgreSQL');
	// con <- dbConnect(driver, host="localhost", dbname="calc", user="calc",
	// password="calc", port=5432)
	// dbSendQuery(conn=con, statement= "SET search_path TO naforma1, public");

	//TODO get the database connection properties from file
	private void appendOpenConnection() {
		sb.append("driver <- dbDriver('PostgreSQL');");
		appendNewLine();
		sb.append("con <- dbConnect(driver, host='localhost', dbname='calc', user='calc', password='calc', port=5432);");
		appendNewLine();
		sb.append("dbSendQuery(conn=con, statement='SET search_path TO "
				+ schemas.getInputSchema().getName() + ", public');");
		appendNewLine();
	}

	//TODO refactor code below. create RScript object?
	private void appendSteps(Entity entity, CalculationStep[] steps) {
		EntityDataView view = schemas.getInputSchema().getDataView(entity);
		InputTable table = schemas.getInputSchema().getDataTable(entity);
		Field<?> primaryKeyField = view.getPrimaryKey().getFields().get(0);
		String primaryKey = primaryKeyField.getName();
		
		//fields to select
		Set<String> selectFields = new HashSet<String>();
		selectFields.add(primaryKey);
		//fields to save 
		Set<String> outputFields = new HashSet<String>();
		outputFields.add(primaryKey);
		//scripts to be executed
		List<String> scripts = new ArrayList<String>();
		
		//calculate plot area if script is set
		String plotAreaScript = entity.getPlotAreaScript();
		
		// prepare script for each step
		for (CalculationStep step : steps) {
			
			//append input vars to the select
			Set<String> inputVariables = step.getInputVariables();
			selectFields.addAll(inputVariables);
			//selects also variable used to calculate plota rea
			if(StringUtils.isNotBlank(plotAreaScript)){
				Set<String> plotAreaVars = extractVariables(plotAreaScript);
				selectFields.addAll(plotAreaVars);
			}
			
			//prepare script
			String script = replaceVariables(step.getScript());
			script = "data$"+step.getOutputVariable().getName() + " <- " + script +";";
			scripts.add(script);
			
			// also calculate the per_ha value if output variable has per_ha variable and if plot_area_script is set
			QuantitativeVariable outputVariable = (QuantitativeVariable) step.getOutputVariable();
			//output fields for result table
			String outputVariableName = outputVariable.getName();
			outputFields.add(outputVariableName);
			
			QuantitativeVariable variablePerHa = outputVariable.getVariablePerHa();
			
			if( variablePerHa != null && StringUtils.isNotBlank(plotAreaScript) ) {
				StringBuilder perHaScript = new StringBuilder();
				perHaScript.append("data$");
				String variablePerHaName = variablePerHa.getName();
				perHaScript.append(variablePerHaName);
				perHaScript.append(" <- data$");
				perHaScript.append(outputVariableName);
				perHaScript.append(" / ( ");
				//TODO calculate plot area only once
				perHaScript.append( replaceVariables(plotAreaScript) );
				perHaScript.append(" );");
				
				scripts.add(perHaScript.toString());
				outputFields.add(variablePerHaName);
			}

		}
		// 1. update output variables to null
		UpdateQuery<Record> upd = new Psql().updateQuery(table);//.set(null, null).
		for (String field : outputFields) {
			//skip primary key
			if( !field.equals(primaryKey) ){
				//TODO what if there are other types of field to update other than double?
				Field<Double> f = (Field<Double>) table.field(field);
				upd.addValue( f, DSL.val(null, Double.class) );
			}
		}
		sb.append("dbSendQuery(conn=con, statement='");
		sb.append(upd.toString());
		sb.append("');");
		appendNewLine();
		
		// 2. append select
		sb.append("data <- dbGetQuery( conn=con , statement=\"select ");
		sb.append(StringUtils.join(selectFields,","));
		sb.append(" from ");
		sb.append(this.schemas.getInputSchema().getDataView(entity).getName() );
		sb.append("\");");
		appendNewLine();
		
		
		// 3. append scripts
		for (String script : scripts) {
			sb.append(script);
			appendNewLine();
			appendNewLine();
		}
		
		// 4. convert primary key field to string otherwise integers are stored as real. (R doesnt manage int type. all numbers are real)
		String pkeyConversion = "data$\"" +primaryKey+ "\" <- as.character(data$\""+primaryKey+"\");";
		sb.append(pkeyConversion);
		appendNewLine();
		
		// 5. keep results (only pkey and output variables) 
		sb.append( "results <-  data[ , c('" );
		sb.append(StringUtils.join(outputFields , "','"));
		sb.append("') ];");
		appendNewLine();
		// remove Inf numbers from results
		sb.append( "is.na(results[ , unlist(lapply(results, is.numeric))] ) <-  results[ , unlist(lapply(results, is.numeric))] == Inf;" );
		
		// 6. remove results table
		String resultsTable = entity.getResultsTable();
		
		sb.append("dbRemoveTable(con, \"");
		sb.append(resultsTable);
		sb.append("\");");
		appendNewLine();
		
		// 7. write results to db
		sb.append("dbWriteTable(con, \"");
		sb.append(resultsTable);
		sb.append("\", results, row.names=F);");
		appendNewLine();

		// 8. for each output field, update table with results joining with results table
		//TODO implement updateFrom with Psql
		StringBuilder update = new StringBuilder();
		update.append("update ");
		update.append(table.getName());
		update.append(" d ");
		update.append("set ");
		int i = 0;
		for (String field : outputFields) {
			//skip primary key
			if( !field.equals(primaryKey) ){
				if((i++)!=0){
					update.append(", ");
				}
				update.append(field);
				update.append(" = r.");
				update.append(field);
				update.append(" ");
			}
		}
		update.append("from ");
		update.append(resultsTable);
		update.append(" r ");
		update.append("where r.");
		update.append(primaryKey);
		update.append("::integer = d.");
		update.append(primaryKey);
		sb.append("dbSendQuery(conn=con, statement=\"");
		sb.append(update.toString());
		sb.append("\");");
		appendNewLine();
		
		// 9. close connection
		sb.append("dbDisconnect(con);");
		appendNewLine();
	}

	private String replaceVariables(String script) {
		String newScript = script;
		
		Pattern pattern = Pattern.compile(CalculationStep.VARIABLE_PATTERN);
		Matcher m = pattern.matcher(script);
		while (m.find()) {
			String variable = m.group(1);			
			newScript = newScript.replaceFirst("\\$"+variable+"\\$", "data\\$"+variable);
		}
		
		return newScript;
	}
	
	public Set<String> extractVariables(String script) {
		Set<String> variables = new HashSet<String>();
		Pattern p = Pattern.compile(CalculationStep.VARIABLE_PATTERN);
		Matcher m = p.matcher(script);
		while (m.find()) {
			String variable = m.group(1);
			variables.add(variable);
		}
		return variables;
	}
	private void appendNewLine(){
		sb.append("\n");
	}
	
	public static void main(String[] args) {
		String script = "($dbh$ * 0.54353 ) ^ 2 + $dbh$";
		String newScript = script;
		
		Pattern pattern = Pattern.compile("(\\$)(.+?)(\\$)");
		Matcher m = pattern.matcher(script);
		while (m.find()) {
			System.out.println("==== found");
			int groupCount = m.groupCount();
			System.out.println(groupCount);
			String variable = m.group(2);
			System.out.println(variable);
			
			
			String group = m.group();
			System.out.println(group);
			newScript = newScript.replaceFirst("\\$"+variable+"\\$", "data\\$"+variable);
//			a+=m.replaceFirst("MINO");
//			System.out.println("...replaced");
//			System.out.println(a);
//			a+= script.replace(script, replacement)
		}
		System.out.println("=== =Script: ");
		System.out.println(newScript);
	}
	
}
