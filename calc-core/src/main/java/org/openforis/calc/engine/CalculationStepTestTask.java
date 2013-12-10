package org.openforis.calc.engine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Vector;

import org.json.simple.JSONObject;
import org.openforis.calc.metadata.Variable;
import org.openforis.calc.r.R;
import org.openforis.calc.r.REnvironment;
import org.openforis.calc.r.RException;
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
	private Parameters parameters;
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

	public Parameters getParameters() {
		return parameters;
	}
	
	public void setParameters(Parameters parameters) {
		this.parameters = parameters;
	}
	
	@Override
	public synchronized void init() {
		records = createAllCombinations();
		super.init();
	}
	
	@Override
	@Transactional
	synchronized 
	protected void execute() throws InterruptedException, RException {
		rEnvironment = r.newEnvironment();

		// for output
		results = new Vector<DataRecord>();

		Variable<?> outputVariable = getOutputVariable();
		String outputVariableName = outputVariable.getName();
		
		//for each record set output variable value
		for (DataRecord record : records) {
			Map<String, Object> valuesByVariable = record.getFields();
			String script = getScript();
			for (Entry<String, Object> variableEntry : valuesByVariable.entrySet()) {
				String name = variableEntry.getKey();
				Object value = variableEntry.getValue();
				script = script.replaceAll("\\$" + name + "\\$", value.toString());
			}
			double result = evaluate(script, outputVariable);
			record.add(outputVariableName, result);
			results.add(record);
			incrementItemsProcessed();
		}
		System.out.println(String.format("Generated %d records", results.size()));
	}
	
	protected List<DataRecord> createAllCombinations() {
		Map<String, List<Double>> seriesByVariable = new HashMap<String, List<Double>>();
		for (Entry<String, VariableParameters> entry : parameters.getVariableParametersByName().entrySet()) {
			String varName = entry.getKey();
			VariableParameters variableParameters = entry.getValue();
			List<Double> series = variableParameters.generateSeries();
			seriesByVariable.put(varName, series);
		}
		List<DataRecord> result = createAllCombinations(new DataRecord(), seriesByVariable);
		return result;
	}
	
	protected List<DataRecord> createAllCombinations(DataRecord initialData, Map<String, List<Double>> seriesByVariable) {
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
					List<DataRecord> leafRecords = createAllCombinations(dataRecord, remainingSeries);
					result.addAll(leafRecords);
				}
			} catch (CloneNotSupportedException e1) {
			}
		}
		return result;
	}

	@Override
	protected long countTotalItems() {
		long count = records.size();
		if (maxItems <= 0 || maxItems > count) {
			maxItems = count;
		}

		return maxItems;
	}

	private Variable<?> getOutputVariable() {
		return getCalculationStep().getOutputVariable();
	}

	private String getScript() {
		return getCalculationStep().getScript();
	}

	// not used for now
	synchronized protected void executeExternalScript() throws RException {
		REnvironment env = r.newEnvironment();
		String script = getCalculationStep().getScript();
		log().debug("Custom R: " + script);
		env.eval(script);
	}

	public long getMaxItems() {
		return maxItems;
	}

	public void setMaxItems(long max) {
		this.maxItems = max;
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

	@JsonIgnore
	public DataRecord getNextResult() {
		synchronized (results) {
			if (results.isEmpty()) {
				return null;
			}
			DataRecord record = results.get(0);
			results.remove(0);
			return record;
		}
	}

	private double evaluate(String script, Variable<?> outputVariable) {
		double result = 0;
		try {
			result = rEnvironment.evalDouble(script);
		} catch (RException e) {
			throw new IllegalStateException("Error while evaluating script: " + script);
		}
		return result;
	}
	
	public static class Parameters {
		
		private Map<String, VariableParameters> variableParametersByName;
		
		public Map<String, VariableParameters> getVariableParametersByName() {
			return variableParametersByName;
		}
		
		public void setVariableParametersByName(
				Map<String, VariableParameters> variableParametersByName) {
			this.variableParametersByName = variableParametersByName;
		}
		
		public String[] getVariableNames() {
			String[] result = variableParametersByName.keySet().toArray(new String[]{});
			return result;
		}

		public static Parameters parse(JSONObject parameters) {
			Parameters result = new Parameters();
			Map<String, VariableParameters> variablesParameters = new HashMap<String, VariableParameters>();
			JSONObject varsParamsJson = (JSONObject) parameters.get("variables");
			for (Object varParamsObj : varsParamsJson.values()) {
				JSONObject varParams = (JSONObject) varParamsObj;
				String variableName = (String) varParams.get("variableName");
				VariableParameters params = new VariableParameters();
				params.max = Double.parseDouble((String) varParams.get("max"));
				params.increment = Double.parseDouble((String) varParams.get("increment"));
				params.min = Double.parseDouble((String) varParams.get("min"));
				variablesParameters.put(variableName, params);
			}
			result.variableParametersByName = variablesParameters;
			return result;
		}
		
	}
	
	public static class VariableParameters {
		private double min;
		private double max;
		private double increment;
		
		public List<Double> generateSeries() {
			List<Double> result = new ArrayList<Double>();
			double current = min;
			while(current <= max) {
				result.add(current);
				current += increment;
			}
			return result;
		}

		public double getMin() {
			return min;
		}
		
		public void setMin(double min) {
			this.min = min;
		}
		
		public double getMax() {
			return max;
		}
		
		public void setMax(double max) {
			this.max = max;
		}
		
		public double getIncrement() {
			return increment;
		}
		
		public void setIncrement(double increment) {
			this.increment = increment;
		}
		
	}
	
}
