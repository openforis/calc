/**
 * 
 */
package org.openforis.calc.engine;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Vector;

import org.openforis.calc.chain.CalculationStep;
import org.openforis.calc.metadata.Variable;
import org.openforis.calc.r.RDataFrame;
import org.openforis.calc.r.REnvironment;
import org.openforis.calc.r.RException;
import org.openforis.calc.r.RVariable;
import org.openforis.calc.r.RVector;
import org.openforis.calc.r.SetValue;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * @author S. Ricci
 * @author Mino Togna
 * 
 */
public class CalcTestJob extends CalcJob {

	private static final int DEFAULT_LIMIT = 1000;

	@JsonIgnore
	private CalculationStep calculationStep;
	
	@JsonIgnore
	private List<DataRecord> results;
	
	// parameters
	@JsonIgnore
	private int limit;

	@JsonIgnore
	private ParameterMap variableSettings;
	
	protected CalcTestJob(Workspace workspace, BeanFactory beanFactory, ParameterMap variableSettings) {
		super(workspace, null, beanFactory);
		this.variableSettings = variableSettings;
		this.limit = DEFAULT_LIMIT;
	}
	
	public void setCalculationStep(CalculationStep calculationStep) {
		this.calculationStep = calculationStep;
	}
	
	public CalculationStep getCalculationStep() {
		return calculationStep;
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
		long total = new CombinationsGenerator().calculateTotalCombinations(variableSettings);
		return Math.min(total, limit);
	}
	
	private ArrayList<String> getVariableNames() {
		Set<String> variableNamesSet = variableSettings.names();
		ArrayList<String> variableNames = new ArrayList<String>(variableNamesSet);
		return variableNames;
	}
	
	public class TestTask extends CalcRTask {

		protected TestTask() {
			super( getrEnvironment(), calculationStep.getCaption() );
		}
		
		@Override
		@Transactional
		synchronized 
		protected void execute() throws InterruptedException, RException {
			Variable<?> outputVariable = getCalculationStep().getOutputVariable();
			String outputVariableName = outputVariable.getName();

			REnvironment rEnvironment = getrEnvironment();
			
			//generate all possible combinations with provided variable settings
			List<DataRecord> allCombinations = generateCombinations();

			//create data frame
			RDataFrame dataFrame = createTestDataFrame(allCombinations);
			RVariable dataFrameVariable = r().variable( outputVariable.getEntity().getName() );
			SetValue setDataFrame = r().setValue( dataFrameVariable , dataFrame);
			rEnvironment.eval(setDataFrame.toString());
			
			//evaluate calculation step script
			rEnvironment.eval(getCalculationStep().getScript());
			
			//set values into result data records
			String[] resultValues = rEnvironment.evalStrings(r().variable(dataFrameVariable, outputVariableName).toString());
			
			generateResults(outputVariableName, allCombinations, resultValues);
		}

		private void generateResults(String outputVariableName, List<DataRecord> rows, String[] resultValues) {
			results = new Vector<DataRecord>();

			for ( int rowIdx=0; rowIdx<rows.size(); rowIdx++ ) {
				DataRecord row = rows.get(rowIdx);
				row.add(outputVariableName, resultValues[rowIdx]);
				results.add(row);
			}
		}

		/**
		 * Converts a list of {@link DataRecord} objects into a {@link RDataFrame} object.
		 *	
		 */
		private RDataFrame createTestDataFrame(List<DataRecord> records) throws RException {
			List<RVector> columns = new ArrayList<RVector>();
			for (String variableName : getVariableNames()) {
				//create a column of values per each variable
				Object[] values = new Object[records.size()];
				for (int i = 0; i < records.size(); i++) {
					DataRecord record = records.get(i);
					Object value = record.getValue(variableName);
					values[i] = value;
				}
				columns.add( r().c(values) );
			}
			RDataFrame result = new RDataFrame(getVariableNames(), columns);
			return result;
		}

		/**
		 * Generates all the possible combinations given the specified settings
		 */
		private List<DataRecord> generateCombinations() {
			Map<String, List<?>> seriesByVariables = new HashMap<String, List<?>>();
			for (String varName : getVariableNames()) {
				ParameterMap varSettings = variableSettings.getMap(varName);
				List<Double> series = new CombinationsGenerator().generateSeries(varSettings);
				seriesByVariables.put(varName, series);
			}
			List<DataRecord> result = new CombinationsGenerator().generateAllCombinations(seriesByVariables, limit);
			return result;
		}

	}
	
	private static class CombinationsGenerator {
		
		/**
		 * Generates all the possible combinations of the specified lists of values
		 */
		public List<DataRecord> generateAllCombinations(Map<String, List<?>> seriesByName, int limit) {
			List<DataRecord> combinations = new ArrayList<DataRecord>();

			long total = calculateTotalCombinations(seriesByName.values());
			
			for(int count=1; count<=total && count<=limit; count++) {
				DataRecord row = new DataRecord(seriesByName.size());
				int currentSeriesWeight = 1;
				for (Entry<String, List<?>> seriesEntry : seriesByName.entrySet()) {
					String seriesName = seriesEntry.getKey();
					List<?> series = seriesEntry.getValue();
					
					int currentSeriesSize = series.size();
					
					int calculatedValIndex = new Double(Math.ceil((double) ((count - 1) / currentSeriesWeight))).intValue();
					
					int valueIndex = calculatedValIndex % currentSeriesSize;
					
					Object value = series.get(valueIndex);
					row.add(seriesName, value);
					currentSeriesWeight *= currentSeriesSize;
				}
				combinations.add(row);
			}
			return combinations;
		}

		/**
		 * Generates a list of values according to min, max and increment specified in the settings 
		 */
		public List<Double> generateSeries(ParameterMap settings) {
			double min = settings.getNumber("min").doubleValue();
			double max = settings.getNumber("max").doubleValue();
			double increment = settings.getNumber("increment").doubleValue();
			return generateSeries(min, max, increment);
		}

		/**
		 * Generates a list of values from min to max with the increment specified 
		 */
		public List<Double> generateSeries(double min, double max, double increment) {
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
		 * Calculates the total number of possible combinations of the series specified
		 */
		public long calculateTotalCombinations(Collection<List<?>> seriesList) {
			long total = 1;
			for (List<?> list : seriesList) {
				total = total *= list.size();
			}
			return total;
		}
		
		/**
		 * Calculates the total number of possible combinations of the series that can be obtained by the specified settings 
		 */
		public long calculateTotalCombinations(ParameterMap seriesParams) {
			long total = 1;
			Set<String> names = seriesParams.names();
			for (String name : names) {
				ParameterMap seriesParam = seriesParams.getMap(name);
				double min = seriesParam.getNumber("min").doubleValue();
				double max = seriesParam.getNumber("max").doubleValue();
				double increment = seriesParam.getNumber("increment").doubleValue();
				List<Double> series = generateSeries(min, max, increment);
				total *= series.size();
			}
			return total;
		}
		
	}
}
