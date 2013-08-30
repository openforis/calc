package org.openforis.calc.chain.pre;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;

import org.jooq.Field;
import org.openforis.calc.engine.Task;
import org.openforis.calc.metadata.BinaryVariable;
import org.openforis.calc.metadata.Entity;
import org.openforis.calc.metadata.MultiwayVariable;
import org.openforis.calc.metadata.QuantitativeVariable;
import org.openforis.calc.metadata.Variable;
import org.openforis.calc.schema.OutputSchema;
import org.openforis.calc.schema.OutputTable;

public class ApplyDefaultsTask extends Task {

	@Override
	protected void execute() throws Throwable {
		OutputSchema outputSchema = getOutputSchema();
		Collection<OutputTable> outputTables = outputSchema.getOutputTables();
		for (OutputTable outputTable : outputTables) {
			Entity entity = outputTable.getEntity();
			List<Variable<?>> variables = entity.getVariables();
			for (Variable<?> variable : variables) {
				if ( variable instanceof QuantitativeVariable ) {
					applyDefaultValue(outputTable, (QuantitativeVariable) variable);
				} else if ( variable instanceof BinaryVariable ){
					applyDefaultValue(outputTable, (BinaryVariable) variable);					
				} else if ( variable instanceof MultiwayVariable ){
					applyDefaultValue(outputTable, (MultiwayVariable) variable);					
				}
			}
		}
	}
	
	private void applyDefaultValue(OutputTable outputTable, QuantitativeVariable variable) {
		BigDecimal defaultValue = variable.getDefaultValue();
		if(  defaultValue != null ){
			Field<BigDecimal> valueField = outputTable.getQuantityField(variable);
			psql().update( outputTable )
				.set( valueField, defaultValue )
				.where( valueField.isNull() )
				.execute();
		}
	}
	
	@SuppressWarnings("unchecked")
	private void applyDefaultValue(OutputTable outputTable, BinaryVariable variable) {
		Boolean defaultValue = variable.getDefaultValue();
		if(  defaultValue != null ) {
			Field<Boolean> valueField = (Field<Boolean>) outputTable.getCategoryValueField(variable);
			psql().update( outputTable )
				.set( valueField, defaultValue )
				.where( valueField.isNull() )
				.execute();
		}
	}
	
	@SuppressWarnings("unchecked")
	private void applyDefaultValue(OutputTable outputTable, MultiwayVariable variable) {
		String defaultValue = variable.getDefaultValue();
		if(  defaultValue != null ) {
			Field<String> valueField = (Field<String>) outputTable.getCategoryValueField(variable);
			psql().update( outputTable )
				.set( valueField, defaultValue )
				.where( valueField.isNull() )
				.execute();
		}
	}
}
