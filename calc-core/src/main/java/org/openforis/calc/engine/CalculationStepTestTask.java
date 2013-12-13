package org.openforis.calc.engine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import org.openforis.calc.metadata.Entity;
import org.openforis.calc.metadata.Variable;
import org.openforis.calc.r.R;
import org.openforis.calc.r.RDataFrame;
import org.openforis.calc.r.REnvironment;
import org.openforis.calc.r.RException;
import org.openforis.calc.r.RScript;
import org.openforis.calc.r.RVariable;
import org.openforis.calc.r.RVector;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Tests a user-defined R statement or script.
 *
 * @author S. Ricci
 */
public class CalculationStepTestTask extends CalculationStepTask {

	private static final int DEFAULT_LIMIT = 5000;

	@JsonIgnore
	private List<DataRecord> results;
	
	@JsonIgnore
	@Autowired
	protected R r;

	// parameters
	@JsonIgnore
	private ParameterMap settings;
	@JsonIgnore
	private long maxItems;
	@JsonIgnore
	private int limit;

	// used during execution
	@JsonIgnore
	private ArrayList<String> variableNames;
	
	public CalculationStepTestTask() {
		limit = DEFAULT_LIMIT;
		maxItems = -1;
	}

	@Override
	public synchronized void init() {
		ParameterMap variablesSettings = settings.getMap("variables");
		Set<String> variableNames = variablesSettings.names();
		this.variableNames = new ArrayList<String>(variableNames);
		super.init();
	}
	
	@Override
	@Transactional
	synchronized 
	protected void execute() throws InterruptedException, RException {
		Variable<?> outputVariable = getOutputVariable();
		String outputVariableName = outputVariable.getName();

		REnvironment rEnvironment = r.newEnvironment();
		
		List<List<?>> allCombinations = generateAllVariablesCombinations();

		RScript rScript = r();
		
		RDataFrame dataFrame = createTestDataFrame(allCombinations);
		
		RVariable dfVar = r().variable(getEntity().getName());
		
		rScript = rScript.setValue(dfVar, dataFrame);
		
		RVariable outputRVar = r().variable(dfVar, outputVariableName);
		
		//assign script result to output variable column in data frame
		rScript = rScript.setValue(outputRVar, r().rScript(getScript()));
		
		//evaluate the script
		rEnvironment.eval(rScript.toString());
		
		//set values into result data records
		String[] resultValues = rEnvironment.evalStrings(outputRVar.toString());
		
		updateResults(outputVariableName, allCombinations, resultValues);
	}

	private void updateResults(String outputVariableName, List<List<?>> rows, String[] resultValues) {
		results = new Vector<DataRecord>();

		for ( int rowIdx=0; rowIdx<rows.size(); rowIdx++ ) {
			List<?> row = rows.get(rowIdx);
			DataRecord record = new DataRecord();
			for ( int varIdx=0; varIdx < variableNames.size(); varIdx++ ) {
				String varName = variableNames.get(varIdx);
				record.add(varName, row.get(varIdx));
			}
			record.add(outputVariableName, resultValues[rowIdx]);
			results.add(record);
			incrementItemsProcessed();
		}
	}

	@Override
	protected long countTotalItems() {
		long total = 1;
		for (String variable : variableNames) {
			long seriesSize = getVariableSeriesSize(variable);
			total *= seriesSize;
		}
		maxItems = Math.min(total, limit);
		return maxItems;
	}

	@JsonIgnore
	List<DataRecord> bufferResults = null;

	@JsonIgnore
	public List<DataRecord> getResults(int from, int to) {
		if (results == null) {
			return Collections.emptyList();
		} else {
			synchronized (results) {
				if (this.getItemsProcessed() < to) {
					to = (int) this.getItemsProcessed();
				} else if (from + (to - from) > results.size()) {
					to = results.size();
				}
				List<DataRecord> subList = results.subList(from, to);
				bufferResults = new ArrayList<DataRecord>();
				bufferResults.addAll(subList);
				return bufferResults;
			}
		}
	}

	/**
	 * Converts a list of {@link DataRecord} objects into a {@link RDataFrame} object.
	 *	
	 */
	private RDataFrame createTestDataFrame(List<List<?>> records) throws RException {
		List<RVector> columns = new ArrayList<RVector>();
		for (int variableIndex = 0; variableIndex < variableNames.size(); variableIndex++) {
			Object[] values = new Object[records.size()];
			for(int i=0; i<records.size(); i++) {
				List<?> record = records.get(i);
				Object value = record.get(variableIndex);
				values[i] = value;
			}
			columns.add(new RVector(null, false, values));
		}
		RDataFrame result = new RDataFrame(variableNames, columns);
		return result;
	}

	/**
	 * Generates all the possible combinations given the specified settings
	 */
	private List<List<?>> generateAllVariablesCombinations() {
		List<List<?>> seriesByVariables = generateSeriesByVariables();
		List<List<?>> result = new CombinationsGenerator().generateAllCombinations(seriesByVariables, limit);
		return result;
	}

	/**
	 * Creates a map of series of values per each variable
	 */
	private List<List<?>> generateSeriesByVariables() {
		List<List<?>> result = new ArrayList<List<?>>();
		for (String varName : variableNames) {
			ParameterMap variablesSettings = settings.getMap("variables");
			ParameterMap varSettings = variablesSettings.getMap(varName);
			List<Double> series = generateSeries(varSettings);
			result.add(series);
		}
		return result;
	}
	
	/**
	 * Generates a sequence of possible values given the specified settings
	 */
	private List<Double> generateSeries(ParameterMap parameterMap) {
		double min = parameterMap.getNumber("min").doubleValue();
		double max = parameterMap.getNumber("max").doubleValue();
		double increment = parameterMap.getNumber("increment").doubleValue();
		return new CombinationsGenerator().generateSeries(min, max, increment);
	}

	/**
	 * Calculates the size of the series that will be generated for the specified variable 
	 */
	private long getVariableSeriesSize(String variableName) {
		ParameterMap variablesSettings = settings.getMap("variables");
		ParameterMap parameterMap = variablesSettings.getMap(variableName);
		double min = parameterMap.getNumber("min").doubleValue();
		double max = parameterMap.getNumber("max").doubleValue();
		double increment = parameterMap.getNumber("increment").doubleValue();
		double result = Math.floor((max - min) / increment) + 1;
		return new Double(result).longValue();
	}
	
	private Variable<?> getOutputVariable() {
		return getCalculationStep().getOutputVariable();
	}
	
	private Entity getEntity() {
		return getOutputVariable().getEntity();
	}

	private String getScript() {
		return getCalculationStep().getScript();
	}
	
	public long getMaxItems() {
		return maxItems;
	}

	public void setMaxItems(long max) {
		this.maxItems = max;
	}

	public ParameterMap getSettings() {
		return settings;
	}
	
	public void setSettings(ParameterMap settings) {
		this.settings = settings;
	}
	
	private static class CombinationsGenerator {
		
		/**
		 * Generates all the possible combinations of the specified lists of values
		 */
		public List<List<?>> generateAllCombinations(List<List<?>> seriesList, int limit) {
			List<List<?>> combinations = new ArrayList<List<?>>();

			long total = calculateTotalCombinations(seriesList);
			
			for(int i=1; i<=total && i<=limit; i++) {
				ArrayList<Object> row = new ArrayList<Object>(seriesList.size());
				int currentSeriesWeight = 1;
				for (int seriesIndex = 0; seriesIndex < seriesList.size(); seriesIndex++) {
					List<?> series = seriesList.get(seriesIndex);
					int currentSeriesSize = series.size();
					
					int calculatedValIndex = new Double(Math.ceil((double) ((i - 1) / currentSeriesWeight))).intValue();
					
					int valueIndex = calculatedValIndex % currentSeriesSize;
					
					Object value = series.get(valueIndex);
					row.add(value);
					currentSeriesWeight *= currentSeriesSize;
				}
				combinations.add(row);
			}
			return combinations;
		}

		public long calculateTotalCombinations(List<List<?>> seriesList) {
			long total = 1;
			for (List<?> list : seriesList) {
				total = total *= list.size();
			}
			return total;
		}
		
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
		
	}
	
}
