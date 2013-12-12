package org.openforis.calc.engine;

import java.util.ArrayList;
import java.util.Collection;
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
import org.rosuda.REngine.REXP;
import org.rosuda.REngine.REXPInteger;
import org.rosuda.REngine.REXPList;
import org.rosuda.REngine.REXPMismatchException;
import org.rosuda.REngine.REXPString;
import org.rosuda.REngine.RList;
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
		rEnvironment = r.newEnvironment();
		
		// for output
		results = new Vector<DataRecord>();

		Variable<?> outputVariable = getOutputVariable();
		String outputVariableName = outputVariable.getName();
		
		RDataFrame df = createTestDataFrame();
		rEnvironment.assign(getEntity().getName(), df);
		
		
		
		String script = getScript();
		rEnvironment.eval(script);
		
		//for each record set output variable value
		/*
		for (DataRecord record : records) {
			Map<String, Object> valuesByVariable = record.getFields();
			String script = getScript();
			for (Entry<String, Object> variableEntry : valuesByVariable.entrySet()) {
				String name = variableEntry.getKey();
				Object value = variableEntry.getValue();
				script = script.replaceAll("\\$" + name + "\\$", value.toString());
			}
			double result = evaluate(script);
			record.add(outputVariableName, result);
			results.add(record);
			incrementItemsProcessed();
		}
		*/
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

	private double evaluate(String script) {
		double result = 0;
		try {
			result = rEnvironment.evalDouble(script);
		} catch (RException e) {
			throw new IllegalStateException("Error while evaluating script: " + script);
		}
		return result;
	}
	
	private RDataFrame createTestDataFrame() throws RException {
		List<DataRecord> records = generateAllVariablesCombinations();
		RDataFrame result = new RDataFrame(getVariableNames(), records);
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
	
	private List<String> getVariableNames() {
		ParameterMap variablesSettings = settings.getMap("variables");
		Set<String> variableNames = variablesSettings.names();
		return new ArrayList<String>(variableNames);
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
	
	public static void main(String[] args) throws REXPMismatchException {
		Collection<REXP> rows = new ArrayList<REXP>();
		RList rowList = new RList(new REXP[]{ new REXPString("TEST"), new REXPInteger(123)});
		REXPList row = new REXPList(rowList);
		rows.add(row);
		RList rList = new RList(rows, new String[]{"col1", "col2"});
		REXP.createDataFrame(rList);
	}
	
}
