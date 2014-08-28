package org.openforis.calc.chain;

import java.math.BigDecimal;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.jooq.Condition;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.SQLDialect;
import org.jooq.SelectQuery;
import org.jooq.impl.DSL;
import org.jooq.impl.DynamicTable;
import org.openforis.calc.engine.CalculationException;
import org.openforis.calc.engine.ParameterMap;
import org.openforis.calc.engine.Workspace;
import org.openforis.calc.metadata.CategoricalVariable;
import org.openforis.calc.metadata.Entity;
import org.openforis.calc.metadata.Equation;
import org.openforis.calc.metadata.EquationList;
import org.openforis.calc.metadata.QuantitativeVariable;
import org.openforis.calc.metadata.Variable;
import org.openforis.calc.psql.CaseStep;
import org.openforis.calc.psql.CaseStep.EndStep;
import org.openforis.calc.psql.Psql;
import org.openforis.calc.r.RScript;
import org.openforis.calc.r.RVariable;
import org.openforis.calc.r.SetValue;
import org.openforis.calc.r.Sqldf;
import org.springframework.stereotype.Component;

/**
 * Utility class that generates the R script based on the calculation step type
 * @author Mino Togna
 *
 */
@Component
public class CalculationStepRScriptGenerator {

	public String generateRScript( CalculationStep calculationStep ){
		String script = null;
		
		switch( calculationStep.getType() ){
		case CATEGORY:
			script = createCategoryTypeScript( calculationStep );
			break;
		case EQUATION:
			script = createEquationTypeScript( calculationStep );
			break;
		case SCRIPT:
			script = calculationStep.getScript();
			break;
		
		}
		return script;
	}

	private String createEquationTypeScript( CalculationStep calculationStep ) {
		Workspace workspace = calculationStep.getWorkspace();
		
		Variable<?> outputVariable = calculationStep.getOutputVariable();
		Entity entity = outputVariable.getEntity();
		RVariable rOutputVariable = r().variable( entity.getName(), outputVariable.getName() );
		
		EquationList equationList = workspace.getEquationListById( calculationStep.getEquationListId() );
		Iterator<Equation> iterator = equationList.getEquations().iterator();
		RScript rScript = getNextREquation( iterator , calculationStep );
		
		SetValue setValue = r().setValue( rOutputVariable, rScript );
		
		String script = setValue.toString();
		return script;
	}

	private RScript getNextREquation( Iterator<Equation> iterator , CalculationStep calculationStep ) {
		Equation eq = iterator.next();
		
		String code = eq.getCode();
		String condition = eq.getCondition();
		
		StringBuffer sbCondition = new StringBuffer();
		RScript rCondition = null;
		if( StringUtils.isNotBlank( code ) || StringUtils.isNotBlank( condition)  ) {
			if(StringUtils.isNotBlank( code )) {
				
				Entity entity = calculationStep.getOutputVariable().getEntity();
				Integer codeVariableId = calculationStep.getParameters().getInteger( "codeVariable" );
				Variable<?> codeVariable = calculationStep.getWorkspace().getVariableById(codeVariableId );
				RVariable rCodeVariable	= r().variable( entity.getName() , codeVariable.getName() );
				
				sbCondition.append( rCodeVariable.toString() );
				sbCondition.append( " == " );
				if( codeVariable instanceof CategoricalVariable ){
					sbCondition.append( "'" );
				}
				sbCondition.append( code );
				if( codeVariable instanceof CategoricalVariable ){
					sbCondition.append( "'" );
				}
			}
			
			if(StringUtils.isNotBlank( condition )) {
				if( sbCondition.length() > 0 ) {
					sbCondition.append( " & " );
				}
				
				String expr = replaceVariables(condition , calculationStep);
				sbCondition.append( expr );
			}
			
			rCondition = r().rScript( sbCondition.toString() );
		}
		
		String equation = eq.getEquation();
		equation = replaceVariables(equation , calculationStep);
		RScript rEquation = r().rScript(equation);
		
		RScript rScript = null;
		if( iterator.hasNext() ) {
			if( rCondition == null ){
				throw new CalculationException( "Equation " + equation + " has neither code nor condition set" );
			}
			rScript = r().ifElse( rCondition, rEquation, getNextREquation(iterator , calculationStep) );
		} else {
			rScript = rEquation;
		}
		return rScript;
	}
	
	String replaceVariables( String string ,  CalculationStep calculationStep ) {
		Entity entity = calculationStep.getOutputVariable().getEntity();
		RVariable entityDf = r().variable( entity.getName() );

		String script = string;
		
		List<ParameterMap> varParams = calculationStep.getParameters().getList("variables");
		for (ParameterMap varParam : varParams) {
			Integer variableId = varParam.getInteger("variableId");
			Variable<?> variable = entity.getWorkspace().getVariableById(variableId);
			
			RVariable rVariable = r().variable( entityDf , variable.getName() );

			String equationVariable = varParam.getString( "equationVariable" );
			String rVar = rVariable.toString();
			rVar = rVar.replaceAll("\\$", "\\\\\\$");
			script = script.replaceAll( "\\b" + equationVariable + "\\b", rVar );
		}
		
		return script;
	}
	
	private String createCategoryTypeScript( CalculationStep calculationStep ) {
		
		Variable<?> outputVariable = calculationStep.getOutputVariable();
		Entity entity = outputVariable.getEntity();
		Workspace workspace = entity.getWorkspace();
		DynamicTable<?> table = new DynamicTable<Record>(entity.getName());
		
		Psql dsl = new Psql(SQLDialect.SQLITE);
		
//		select.addSelect( DSL.field( entity.getName() + ".*") );
		CaseStep caseIdStep = dsl.decode();
		CaseStep caseCodeStep = dsl.decode();
		
//		select.addSelect( dsl.decode().when(condition, expr) );
		List<CalculationStepCategoryClassParameters> parameters = calculationStep.getCategoryClassParameters();
		for (CalculationStepCategoryClassParameters param : parameters) {
			Integer classId 	= param.getClassId();
			String classCode 	= param.getClassCode();
			Integer variableId 	= param.getVariableId();
			String condition 	= param.getCondition();
			String left 		= param.getLeft();
			String right 		= param.getRight();
			
			Variable<?> variable = workspace.getVariableById(variableId);
			String variableName = variable.getName();
			
			Condition sqlCondition = null;
			if( variable instanceof CategoricalVariable ){
				sqlCondition = getCondition( table.getVarcharField(variableName)  ,condition, left, right, String.class ) ;
			} else if( variable instanceof QuantitativeVariable ){
				sqlCondition = getCondition( table.getBigDecimalField(variableName)  ,condition, left, right, BigDecimal.class ) ;
			}
			
			caseIdStep = caseIdStep.when(sqlCondition, classId);
			caseCodeStep = caseCodeStep.when(sqlCondition, "'"+classCode+"'");
		}
		StringBuilder sb = new StringBuilder();
		
		EndStep endId = caseIdStep.otherwise( -1 ).end();
		SelectQuery<Record> select = dsl.selectQuery();
		select.addFrom(table);
		select.addSelect( DSL.field(endId.toString()).as( outputVariable.getInputCategoryIdColumn()) );
		
		Sqldf sqldf = r().sqldf( selectToString(select) );
		RVariable tmp = r().variable("tmp");
		SetValue setValue = r().setValue(tmp , sqldf);
		sb.append(setValue.toString());
		
		RVariable outputVar = r().variable(entity.getName() , outputVariable.getInputCategoryIdColumn() );
		setValue = r().setValue( outputVar, r().variable(tmp,outputVariable.getInputCategoryIdColumn()) );
		sb.append(setValue.toString());
		
		EndStep endCode = caseCodeStep.otherwise( "'NA'" ).end();
		select.addSelect( DSL.field(endCode.toString()).as( outputVariable.getOutputValueColumn()) );
		select = dsl.selectQuery();
		select.addFrom(table);
		select.addSelect( DSL.field(endCode.toString()).as( outputVariable.getOutputValueColumn()) );
		
		sqldf = r().sqldf( selectToString(select) );
		tmp = r().variable("tmp");
		setValue = r().setValue(tmp , sqldf);
		sb.append(setValue.toString());
		
		outputVar = r().variable(entity.getName() , outputVariable.getOutputValueColumn() );
		setValue = r().setValue( outputVar, r().variable(tmp,outputVariable.getOutputValueColumn()) );
		sb.append(setValue.toString());
		
		return sb.toString();
	}

	private String selectToString(SelectQuery<Record> select) {
		String string = select.toString();
		string = string.replaceAll( "'", "\\\\'" );
		return string;
	}

	public static void main(String[] args) {
		String string = "select case  when \"tree\".\"forest_status\" < '100' then 1  when \"tree\".\"forest_status\" < '200' then 2  when \"tree\".\"forest_status\" < '300' then 3  when \"tree\".\"forest_status\" < '400' then 4  when \"tree\".\"forest_status\" < '500' then 5  when \"tree\".\"forest_status\" = '630' then 6  when \"tree\".\"forest_status\" <> '630' then 7 else -1  end tree_major_forest_status_id from tree";
		string = string.replaceAll( "'", "\\\\'" );
		System.out.println( string );
	}
	
	@SuppressWarnings("unchecked")
	private <T> Condition getCondition(Field<T> field, String condition, String left, String right, Class<T> fieldType) {
		T value1 = null; 
		T value2 = null;
		if( String.class.isAssignableFrom( fieldType ) ) {
			value1 = left ==  null ? null : (T) left;
			value2 = right ==  null ? null : (T) right;
		} else if( Number.class.isAssignableFrom( fieldType ) ) {
			value1 = left ==  null ? null : (T) new BigDecimal(left);
			value2 = right ==  null ? null : (T) new BigDecimal(right);
		} else {
			throw new IllegalArgumentException( "Data type " + fieldType.getName() + " not yet supported" );
		}
		
		Condition c = null;
		if ("=".equals(condition)) {
			c = field.eq( value1 );
		} else if ("!=".equals(condition)) {
			c = field.notEqual( value1 );
		} else if ( "<".equals(condition) ) {
			c = field.lessThan(value1);
		} else if ( "<=".equals(condition)) {
			c = field.lessOrEqual( value1 );
		} else if ( ">".equals(condition)) {
			c = field.greaterThan( value1 );
		} else if ( ">=".equals(condition)) {
			c = field.greaterOrEqual( value1 );
		} else if ( "LIKE".equals(condition)) {
			c = field.like( value1.toString() );
		} else if ( "NOT LIKE".equals(condition)) {
			c = field.notLike( value1.toString() );
		} else if ("BETWEEN".equals(condition)) {
			c = field.between( value1 , value2 );
		} else if ("NOT BETWEEN".equals(condition)) {
			c = field.notBetween( value1 , value2 );
		} else if ("IS NULL".equals(condition)) {
			c = field.isNull();
		} else if ("IS NOT NULL".equals(condition)) {
			c = field.isNotNull();
		} else if ("IN".equals(condition)) {
			// right now only string with IN clause is working
//			JSONArray valuesList = (JSONArray) conditionParam.get("values");
//			c = field.in( valuesList );
		}
	
		return c;
	}
	

	private RScript r() {
		return new RScript();
	}
}
