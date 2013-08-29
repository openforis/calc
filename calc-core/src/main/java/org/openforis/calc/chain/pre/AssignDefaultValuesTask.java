package org.openforis.calc.chain.pre;

import org.openforis.calc.engine.Task;
import org.openforis.calc.metadata.CategoricalVariable;
import org.openforis.calc.metadata.Category;
import org.openforis.calc.metadata.QuantitativeVariable;
import org.openforis.calc.metadata.Variable;
import org.openforis.calc.psql.PsqlBuilder;

public class AssignDefaultValuesTask extends Task {

	@Override
	protected void execute() throws Throwable {
		// TODO Auto-generated method stub
	}
	
	private void applyDefaultVariableValue(String outputFactTable, Variable variable) {
		if(  variable instanceof QuantitativeVariable &&  ( (QuantitativeVariable)variable).getDefaultValue() != null ){
			QuantitativeVariable quantitativeVariable = (QuantitativeVariable)variable;
			String valueColumn = PsqlBuilder.quote(variable.getOutputValueColumn());
			Double defaultValue = quantitativeVariable.getDefaultValue();
			
			createPsqlBuilder().update( outputFactTable )
				.set( valueColumn + " = ? ")
				.where( valueColumn ).isNull()
				.execute( defaultValue);
			
		}else if ( variable instanceof CategoricalVariable &&  ( (CategoricalVariable)variable).getDefaultValue() != null  ){
			
			CategoricalVariable categoricalVariable = (CategoricalVariable)variable;
			String valueColumn = PsqlBuilder.quote(variable.getOutputValueColumn());
			String idColumn =  PsqlBuilder.quote( categoricalVariable.getOutputCategoryIdColumn());
			Category defaultValue = categoricalVariable.getDefaultValue();
			
			createPsqlBuilder().update( outputFactTable )
				.set( valueColumn + " = ?, " + idColumn + " = " + defaultValue.getId())
				.where( valueColumn ).isNull()
				.or( idColumn ).isNull()
				.execute( defaultValue.getCode()  );
		}
	}
}
