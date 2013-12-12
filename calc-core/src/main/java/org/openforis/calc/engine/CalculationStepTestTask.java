package org.openforis.calc.engine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
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
	protected REnvironment rEnvironment;
	@JsonIgnore
	private List<DataRecord> records;
	
	public CalculationStepTestTask() {
		limit = 5000;
		maxItems = -1;
	}

	@Override
	public synchronized void init() {
		records = generateAllVariablesCombinations();
		super.init();
	}
	
	@Override
	@Transactional
	synchronized 
	protected void execute() throws InterruptedException, RException {
		Variable<?> outputVariable = getOutputVariable();
		String outputVariableName = outputVariable.getName();

		rEnvironment = r.newEnvironment();
		
		// for output
		results = new Vector<DataRecord>();

		RScript rScript = new RScript();
		
		RDataFrame dataFrame = createTestDataFrame();
		
		RVariable dfVar = new RScript().variable(getEntity().getName());
		
		rScript = rScript.setValue(dfVar, dataFrame);
		
		RVariable outputRVar = new RScript().variable(dfVar, outputVariableName);
		
		//assign script result to output variable column in data frame
		rScript = rScript.setValue(outputRVar, new RScript().rScript(getScript()));
		
		rEnvironment.eval(rScript.toString());
		
		//set values into result data records
		String[] resultValues = rEnvironment.evalStrings(outputRVar.toString());
		
		updateResults(outputVariableName, resultValues);
	}

	private void updateResults(String outputVariableName, String[] resultValues) {
		//for each record set output variable value
		for ( int i=0; i < records.size(); i++ ) {
			DataRecord record = records.get(i);
			String value = resultValues[i];
			record.add(outputVariableName, value);
			results.add(record);
			incrementItemsProcessed();
		}
	}
	

	@Override
	protected long countTotalItems() {
		long count = records.size();
		if (maxItems <= 0 || maxItems > count) {
			maxItems = count;
		}

		return maxItems;
	}

	@JsonIgnore
	List<DataRecord> bufferResults = null;

	@JsonIgnore
	public List<DataRecord> getResults(int from, int to) {
		if (results != null) {
			synchronized (results) {
				if (this.getItemsProcessed() < to) {
					to = (int) this.getItemsProcessed();
				} else if (from + (to - from) > results.size()) {
					to = results.size();
				}
				List<DataRecord> subList = results.subList(from, to);
				// bufferResults =C ollectionUtils.unmodifiableList(subList);
				bufferResults = new ArrayList<DataRecord>();
				bufferResults.addAll(subList);
				return bufferResults;
			}
		}
		return null;
	}

	private RDataFrame createTestDataFrame() throws RException {
		List<RVector> columns = new ArrayList<RVector>();
		List<String> variableNames = getVariableNames();
		for (String colName : variableNames) {
			Object[] values = new Object[records.size()];
			for(int i=0; i<records.size(); i++) {
				DataRecord record = records.get(i);
				Object value = record.getValue(colName);
				values[i] = value;
			}
			columns.add(new RVector(null, false, values));
		}
		RDataFrame result = new RDataFrame(variableNames, columns);
		return result;
	}

	private List<Double> generateVariableSeries(ParameterMap parameterMap) {
		List<Double> result = new ArrayList<Double>();
		
		double min = parameterMap.getNumber("min").doubleValue();
		double max = parameterMap.getNumber("max").doubleValue();
		double increment = parameterMap.getNumber("increment").doubleValue();
		
		double current = min;
		while(current <= max) {
			result.add(current);
			current += increment;
		}
		return result;
	}
	
	private List<DataRecord> generateAllVariablesCombinations() {
		Map<String, List<Double>> seriesByVariable = new HashMap<String, List<Double>>();
		ParameterMap variablesSettings = settings.getMap("variables");
		Set<String> variableNames = variablesSettings.names();
		for (String varName : variableNames) {
			ParameterMap varSettings = variablesSettings.getMap(varName);
			List<Double> series = generateVariableSeries(varSettings);
			seriesByVariable.put(varName, series);
		}
		List<DataRecord> result = generateAllCombinations(new DataRecord(), seriesByVariable);
		return result;
	}
	
	private List<DataRecord> generateAllCombinations(DataRecord initialData, Map<String, List<Double>> seriesByVariable) {
		List<DataRecord> result = new ArrayList<DataRecord>();
		
		Entry<String, List<Double>> firstEntry = seriesByVariable.entrySet().iterator().next();
		String variableName = firstEntry.getKey();
		List<Double> series = firstEntry.getValue();
		Map<String, List<Double>> remainingSeries = new HashMap<String, List<Double>>(seriesByVariable);
		remainingSeries.remove(variableName);
		for (Double val : series) {
			try {
				DataRecord dataRecord = initialData.clone();
				dataRecord.add(variableName, val);
				if ( remainingSeries.isEmpty() ) {
					result.add(dataRecord);
				} else {
					List<DataRecord> leafRecords = generateAllCombinations(dataRecord, remainingSeries);
					result.addAll(leafRecords);
				}
			} catch (CloneNotSupportedException e1) {
			}
		}
		return result;
	}
	
	private List<String> getVariableNames() {
		ParameterMap variablesSettings = settings.getMap("variables");
		Set<String> variableNames = variablesSettings.names();
		return new ArrayList<String>(variableNames);
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
	
}
