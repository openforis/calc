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
import org.openforis.calc.r.CheckError;
import org.openforis.calc.r.RDataFrame;
import org.openforis.calc.r.REnvironment;
import org.openforis.calc.r.RNamedVector;
import org.openforis.calc.r.RVariable;
import org.openforis.calc.r.SetValue;
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
			
			//generate the data frame using all possible combinations according to the provided variable settings
			RDataFrame dataFrame = new CombinationsGenerator().generateCombinations();

			//assing the data frame to a variable called as the entity name
			Variable<?> outputVariable = calculationStep.getOutputVariable();
			
			RVariable dataFrameVariable = r().variable( outputVariable.getEntity().getName() );
			SetValue setDataFrame = r().setValue( dataFrameVariable , dataFrame);
			addScript(setDataFrame);
			
			//add try calculation step script
			Try rTry = r().rTry(calculationStep.getRScript());
			addScript(rTry);
			
			RVariable outputRVariable = r().variable(dataFrameVariable, outputVariable.getName());
			
			//check errors
			CheckError checkError = r().checkError(outputRVariable, null);
			addScript(checkError);
			
			//evaluate script
			super.execute();
			
			//get output variable values
			double[] resultValues = rEnvironment.evalDoubles(outputRVariable.toString());
			
			//generate results
			dataFrame.addColumn(r().c(outputVariable.getName(), (Object[]) ArrayUtils.toObject(resultValues)));
			
			generateResultRecords(dataFrame);
		}

		private void generateResultRecords(RDataFrame dataFrame) {
			results = new ArrayList<DataRecord>();
			for(int count = 0;  count < dataFrame.getSize(); count++) {
				DataRecord record = new DataRecord();
				List<RNamedVector> columns = dataFrame.getColumns();
				for ( int columnIndex = 0;  columnIndex < columns.size(); columnIndex++ ) {
					RNamedVector column = columns.get(columnIndex);
					String colName = column.getName();
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
				RNamedVector column = r().c(varName);
				
				int currentSeriesSize = series.size();
			
				// generate column combinations
				for(int count=1; count<=total && count<=limit; count++) {
					//calculate series value position
					int calculatedValIndex = new Double(Math.ceil((double) ((count - 1) / currentSeriesWeight))).intValue();
					int valueIndex = calculatedValIndex % currentSeriesSize;
					
					Object value = series.get(valueIndex);
					
					column.addValue(value);
				}
				result.addColumn(column);
				
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
