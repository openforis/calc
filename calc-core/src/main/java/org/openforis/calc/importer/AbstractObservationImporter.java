package org.openforis.calc.importer;

import java.util.ArrayList;
import java.util.List;

import org.openforis.calc.io.flat.FlatDataStream;
import org.openforis.calc.io.flat.FlatRecord;
import org.openforis.calc.model.CategoricalValue;
import org.openforis.calc.model.Category;
import org.openforis.calc.model.NumericValue;
import org.openforis.calc.model.Observation;
import org.openforis.calc.model.ObservationUnitMetadata;
import org.openforis.calc.model.SurveyMetadata;
import org.openforis.calc.model.VariableMetadata;
import org.openforis.calc.service.MetadataService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author G. Miceli
 */
public abstract class AbstractObservationImporter
		<O extends Observation, N extends NumericValue, C extends CategoricalValue> 
		extends AbstractFlatFileImporter {

	private String surveyName;
	private String observationUnitName;
	
	@Autowired
	private MetadataService metadataService;
	
	private ObservationUnitMetadata observationUnitMetadata;

	private List<VariableMetadata> variables;

	private Class<N> numValueClass;
	private Class<C> catValueClass;
	private List<O> observations;
	private List<N> numVals;
	private List<C> catVals;

	AbstractObservationImporter(Class<N> numValueClass, Class<C> catValueClass) {
		this.numValueClass = numValueClass;
		this.catValueClass = catValueClass;
	}

	protected abstract O processObservation(FlatRecord record);

	protected abstract void doInserts(List<O> obs, List<N> numVals, List<C> catVals);

	@Override
	protected final void onStart(FlatDataStream stream) {
		List<String> fieldNames = stream.getFieldNames();
		loadMetadata(fieldNames);
		observations = new ArrayList<O>();
		numVals = new ArrayList<N>();
		catVals = new ArrayList<C>();
	}
	
	@Override
	protected final void cleanup() {
		observationUnitMetadata = null;
		observations = null;
		numVals = null;
		catVals = null;
		observationUnitMetadata = null;
	}

	private void loadMetadata(List<String> fieldNames) {
		if ( surveyName == null ) {
			throw new NullPointerException("Survey not set");
		}
		SurveyMetadata metadata = metadataService.getSurveyMetadata(surveyName);
		if ( metadata == null ) {
			throw new IllegalArgumentException("Unknown survey: "+surveyName);
		}
		if ( observationUnitName == null ) {
			throw new NullPointerException("Observation unit not set");
		}
		observationUnitMetadata = metadata.getObservationUnitMetadataByName(observationUnitName); 
		if ( observationUnitMetadata == null ) {
			throw new IllegalArgumentException("Unknown observation unit: "+observationUnitName);
		}
		List<String> skipped = new ArrayList<String>();
		List<String> varFields = new ArrayList<String>();
		variables = new ArrayList<VariableMetadata>();
		for (String col : fieldNames) {
			VariableMetadata var = observationUnitMetadata.getVariableMetadataByName(col);
			if ( var == null ) {
				skipped.add(col);
			} else {
				variables.add(var);
				varFields.add(var.getVariableName());
			}
		}
//		log.info("Recognized system attributes: TBD");
		log.debug("Recognized: "+varFields);
		log.debug("Skipped: "+skipped);
	}
	
	@Override
	protected boolean processRecord(FlatRecord record) {
		O obs = processObservation(record);
		if ( obs == null ) {
			return false;
		}
		observations.add(obs);
		processValues(record, obs);
		return true;
	}

	private void processValues(FlatRecord record, O o) {
		for (VariableMetadata var : variables) {
			String name = var.getVariableName();
			if ( var.isNumeric() ) {
				processNumericValue(record, o, var, name);
			} else if ( var.isCategorical() ) {
				processCategoricalValue(record, o, var, name);
			}
		}
	}

	private void processCategoricalValue(FlatRecord record, Observation o, VariableMetadata var, String name) {
		try {
			String codeStr = record.getValue(name, String.class);
			if ( codeStr != null ) {
				if ( var.isBinary() ) {
					codeStr = translateBinaryCategoryCode(codeStr);
				} 
				String[] codes = codeStr.split(",");
				if ( codes.length > 1 && !var.isMultipleResponse() ) {
					log.warn("Single-response variable '"+var.getVariableName()+"' contains multiple codes in line "+getReadRows()+1);
					return;
				}
				for ( String code : codes ) {
					code = code.trim();
					Category cat = var.getCategoryByCode(code);
					if ( cat == null ) {
						log.warn("Skipping unknown code: "+codeStr+" for variable '"+var.getVariableName()+"' line "+getReadRows()+1);
					} else {
						C val = catValueClass.newInstance();
						val.setObservationId(o.getId());
						val.setCategoryId(cat.getId());
						val.setOriginal(true);
						val.setCurrent(true);
						catVals.add(val);
					}
				}
			}
		} catch (InstantiationException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	private String translateBinaryCategoryCode(String code) {
		if ( "true".equalsIgnoreCase(code) ) {
			code = "T";
		} else if ("false".equalsIgnoreCase(code) ){
			code = "F";
		}
		return code;
	}

	private void processNumericValue(FlatRecord record, Observation o, VariableMetadata var, String name) {
		try {
			Double value = record.getValue(name, Double.class);
			if ( value != null ) {
				N val = numValueClass.newInstance();
				val.setObservationId(o.getId());
				val.setVariableId(var.getId());
				val.setValue(value);
				val.setOriginal(true);
				val.setCurrent(true);
				numVals.add(val);
			}
		} catch ( NumberFormatException e ) {
			String str = record.getValue(name, String.class);
			log.warn("Invalid number '"+str+"' in for variable '"
					+var.getVariableName()+"' line "+getReadRows());
		} catch (InstantiationException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	protected final void performInserts() {
		doInserts(observations, numVals, catVals);
		observations.clear();
		numVals.clear();
		catVals.clear();
	}

	public String getSurvey() {
		return surveyName;
	}

	public void setSurveyName(String survey) {
		this.surveyName = survey;
	}

	public String getObservationUnitName() {
		return observationUnitName;
	}

	public void setObservationUnitName(String observationUnit) {
		this.observationUnitName = observationUnit;
	}
	
	protected ObservationUnitMetadata getObservationUnitMetadata() {
		return observationUnitMetadata;
	}
}