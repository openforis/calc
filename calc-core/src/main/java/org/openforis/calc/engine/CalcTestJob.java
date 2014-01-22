/**
 * 
 */
package org.openforis.calc.engine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.ArrayUtils;
import org.openforis.calc.chain.CalculationStep;
import org.openforis.calc.metadata.Variable;
import org.openforis.calc.r.RDataFrame;
import org.openforis.calc.r.REnvironment;
import org.openforis.calc.r.RException;
import org.openforis.calc.r.RScript;
import org.openforis.calc.r.RVariable;
import org.openforis.calc.r.RVector;
import org.openforis.calc.r.Try;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * @author S. Ricci
 * @author Mino Togna
 * 
 */
public class CalcTestJob extends CalcJob {

	private static final int DEFAULT_LIMIT = 10000;

	// input
	@JsonIgnore
	private CalculationStep calculationStep;
	
	@JsonIgnore
	private ParameterMap variableSettings;
	
	@JsonIgnore
	private int limit;

	// output
	@JsonIgnore
	private List<DataRecord> results;

	protected CalcTestJob(Workspace workspace, BeanFactory beanFactory, ParameterMap variableSettings) {
		super(workspace, null, beanFactory);
		this.variableSettings = variableSettings;
		this.limit = DEFAULT_LIMIT;
	}
	
	public void setCalculationStep(CalculationStep calculationStep) {
		this.calculationStep = calculationStep;
	}
	
	@Override
	protected void initTasks() {
		addTask( new TestTask() );
	}
	
	@JsonIgnore
	public List<DataRecord> getResults(int from, int to) {
		if (results == null) {
			return Collections.emptyList();
		} else {
			synchronized (results) {
				if ( to > results.size() ) {
					to = results.size();
				}
				List<DataRecord> subList = results.subList(from, to);
				return subList;
			}
		}
	}
	
	public long getResultsCount() {
		long total = new CombinationsGenerator().calculateTotalCombinations();
		return Math.min(total, limit);
	}
	
	public class TestTask extends CalcRTask {

		protected TestTask() {
			super( getrEnvironment(), calculationStep.getCaption() );
		}
		
		@Override
		@Transactional
		synchronized 
		protected void execute() throws Throwable {
			REnvironment rEnvironment = getrEnvironment();
			
			addScript(RScript.getCalcRScript());
			
			//generate the data frame using all possible combinations according to the provided variable settings
			RDataFrame dataFrame = new CombinationsGenerator().generateCombinations();

			//assing the data frame to a variable called as the entity name
			Variable<?> outputVariable = calculationStep.getOutputVariable();
			RVariable dataFrameVariable = r().variable( outputVariable.getEntity().getName() );
			
			addScript(r().setValue( dataFrameVariable , dataFrame));
			
			//create try calculation step script
			Try rTry = r().rTry(calculationStep.getRScript());
			
			//set try result into temp variable
			RVariable tryResultVariable = r().variable("try_result");
			addScript(r().setValue( tryResultVariable, rTry));
			
			//check errors
			addScript(r().checkError(tryResultVariable));
			
			//evaluate script
			try {
				super.execute();

				//get output variable values
				RVariable outputRVariable = r().variable(dataFrameVariable, outputVariable.getName());
				double[] resultValues = rEnvironment.evalDoubles(outputRVariable.toString());
				
				//generate results
				dataFrame.addColumn(outputVariable.getName(), r().c((Object[]) ArrayUtils.toObject(resultValues)));
				
				generateResultRecords(dataFrame);
			} catch ( RException e) {
				getRLogger().appendError("Unable to parse script. Please make sure it's syntactically correct.");
				throw e;
			}
		}

		private void generateResultRecords(RDataFrame dataFrame) {
			results = new ArrayList<DataRecord>();
			for(int count = 0;  count < dataFrame.getSize(); count++) {
				DataRecord record = new DataRecord();
				List<String> columnNames = dataFrame.getColumnNames();
				List<RVector> columns = dataFrame.getColumns();
				for ( int columnIndex = 0;  columnIndex < columns.size(); columnIndex++ ) {
					String colName = columnNames.get(columnIndex);
					RVector column = columns.get(columnIndex);
					if ( count >= column.size() ) {
						System.out.println(String.format("Trying to access invalid position %d for column %s", count, colName));
					}
					Object value = column.getValue(count);
					record.add(colName, value);
				}
				results.add(record);
			}
		}

	}
	
	/**
	 * It generates data frames with test data using all the possible combinations of the specified series of values.
	 * 
	 * @author S. Ricci
	 *
	 */
	private class CombinationsGenerator {
		
		/**
		 * Generates all the possible combinations given the specified settings
		 */
		public RDataFrame generateCombinations() {
			RDataFrame result = r().dataFrame();
			
			long total = calculateTotalCombinations();
			
			//give a weight to each column (weight = previous_column_weight * series_size)
			int currentSeriesWeight = 1;
			
			for ( String varName : variableSettings.names() ) {
				ParameterMap varSettings = variableSettings.getMap(varName);
				List<Double> series = generateSeries(varSettings);
				RVector column = r().c();
				
				int currentSeriesSize = series.size();
			
				// generate column combinations
				for(int count=1; count<=total && count<=limit; count++) {
					//calculate series value position
					int calculatedValIndex = new Double(Math.ceil((double) ((count - 1) / currentSeriesWeight))).intValue();
					int valueIndex = calculatedValIndex % currentSeriesSize;
					
					Object value = series.get(valueIndex);
					
					column.addValue(value);
				}
				result.addColumn(varName, column);
				
				currentSeriesWeight *= currentSeriesSize;
			}
			return result;
		}
		
		/**
		 * Generates a list of values according to min, max and increment specified in the settings 
		 */
		public List<Double> generateSeries(ParameterMap settings) {
			double min = settings.getNumber("min").doubleValue();
			double max = settings.getNumber("max").doubleValue();
			double increment = settings.getNumber("increment").doubleValue();
			List<Double> result = new ArrayList<Double>();
			if ( increment > 0 ) {
				double current = min;
				while(current <= max) {
					result.add(current);
					current += increment;
				}
			}
			return result;
		}

		/**
		 * Calculates the total number of possible combinations of the series that can be obtained by the specified settings 
		 */
		public long calculateTotalCombinations() {
			long total = 1;
			Set<String> names = variableSettings.names();
			for (String name : names) {
				ParameterMap seriesParam = variableSettings.getMap(name);
				List<Double> series = generateSeries(seriesParam);
				total *= series.size();
			}
			return total;
		}
		
	}
}
