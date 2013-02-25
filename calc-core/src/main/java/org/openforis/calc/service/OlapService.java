package org.openforis.calc.service;

import java.io.OutputStream;
import java.util.Collection;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.openforis.calc.model.ObservationUnit;
import org.openforis.calc.model.ObservationUnit.Type;
import org.openforis.calc.model.ObservationUnitMetadata;
import org.openforis.calc.model.SurveyMetadata;
import org.openforis.calc.model.VariableMetadata;
import org.openforis.calc.olap.schema.Schema;
import org.openforis.calc.olap.schema.SchemaGenerator;
import org.openforis.calc.persistence.AoiDao;
import org.openforis.calc.persistence.GroundPlotViewDao;
import org.openforis.calc.persistence.OlapDimensionDao;
import org.openforis.calc.persistence.OlapSchemaDao;
import org.openforis.calc.persistence.PlotFactTableDao;
import org.openforis.calc.persistence.PlotSectionViewDao;
import org.openforis.calc.persistence.SpecimenDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 
 * @author M. Togna
 * 
 */
@Service
public class OlapService extends CalcService {

	@Autowired
	private AoiDao aoiDao;
	@Autowired
	private OlapDimensionDao olapDimensionDao;
	@Autowired
	private PlotSectionViewDao plotSectionViewDao;
	@Autowired
	private SpecimenDao specimenDao;
	@Autowired
	private PlotFactTableDao plotFactTableDao;
	@Autowired	
	private GroundPlotViewDao groundPlotViewDao;
	@Autowired	
	private OlapSchemaDao olapSchemaDao;
	
	@Transactional
	public void publishData(String surveyName) {
		SurveyMetadata surveyMetadata = getSurveyMetadata(surveyName);
		
		dropSchema(surveyName);
		createSchema(surveyName);
		
		createTables(surveyMetadata);
		
		populateTables(surveyMetadata);
	}

	@Transactional
	private void populateTables(SurveyMetadata surveyMetadata) {
		int surveyId = surveyMetadata.getSurveyId();
//		String surveyName = surveyMetadata.getSurveyName();
		
		olapDimensionDao.populateAoiDimensionTables(surveyId);
		
		Collection<ObservationUnitMetadata> observationMetadata = surveyMetadata.getObservationMetadata();
		for ( ObservationUnitMetadata obsUnitMetadata : observationMetadata ) {
			Collection<VariableMetadata> variables = obsUnitMetadata.getVariableMetadata();
//			olapDimensionDao.createOlapDimensionTables(surveyMetadata.getSurveyId(), variables);
//			String obsUnitName = obsUnitMetadata.getObsUnitName();
			Type unitType = obsUnitMetadata.getObsUnitTypeEnum();
			switch (unitType) {
			case PLOT:
//				olapDimensionDao.populateOlapDimensionTables(surveyId, variables);
//				// plot fact table
//				plotFactTableDao.populatePlotFactTable(obsUnitMetadata);
//				
////				FlatDataStream areaFactData = getAreaFactData(surveyName, obsUnitName, PlotDistributionCalculationMethod.PRIMARY_SECTION_ONLY);
////				updateAreaFacts(surveyName, areaFactData);
				break;
			case SPECIMEN:
//				FlatDataStream specimenFactData = getSpecimenFactData(surveyName, obsUnitName);
//				updateSpecimenFacts(surveyName, obsUnitName, specimenFactData);
				break;
			case INTERVIEW:
				olapDimensionDao.populateDimensionTables(surveyId, variables);
				break;
			}
		}
	}
	
	private void createTables(SurveyMetadata surveyMetadata) {
		int surveyId = surveyMetadata.getSurveyId();
//		String surveyName = surveyMetadata.getSurveyName();
		
		olapDimensionDao.createAoiOlapDimensionTables(surveyId);
		
		Collection<ObservationUnitMetadata> observationMetadata = surveyMetadata.getObservationMetadata();
		for ( ObservationUnitMetadata obsUnitMetadata : observationMetadata ) {
			Collection<VariableMetadata> variables = obsUnitMetadata.getVariableMetadata();
			
			Type unitType = obsUnitMetadata.getObsUnitTypeEnum();
			switch (unitType) {
			case PLOT:
//				olapDimensionDao.createVariableDimensionTables(variables);
//				// plot fact table				
//				plotFactTableDao.createPlotFactTable(obsUnitMetadata);
//				
////				FlatDataStream areaFactData = getAreaFactData(surveyName, obsUnitName, PlotDistributionCalculationMethod.PRIMARY_SECTION_ONLY);
////				updateAreaFacts(surveyName, areaFactData);

				break;
			case SPECIMEN:
//				olapDimensionDao.createVariableDimensionTables(variables);
////				FlatDataStream specimenFactData = getSpecimenFactData(surveyName, obsUnitName);
////				updateSpecimenFacts(surveyName, obsUnitName, specimenFactData);
				break;
			case INTERVIEW:
				olapDimensionDao.createVariableDimensionTables(variables);
				plotFactTableDao.createPlotFactTable(obsUnitMetadata);
				break;
			}
		}
	}

	private SurveyMetadata getSurveyMetadata(String surveyName) {
		SurveyMetadata surveyMetadata = metadataService.getSurveyMetadata(surveyName);
		if( surveyMetadata == null){
			throw new IllegalArgumentException("Survey " + surveyName + " not found");
		}
		return surveyMetadata;
	}

	private void createSchema(String schema) {
		olapSchemaDao.createSchema(schema);
	}

	private void dropSchema(String schema) {
		olapSchemaDao.dropSchema(schema);
		
	}

//	public void saveSchema(Schema schema, String path) throws IOException, JAXBException {
//		OutputStream out = new BufferedOutputStream(new FileOutputStream(new File(path)));
//		marshalOlapSchema(schema, out);
//		out.flush();
//		out.close();
//	}

	public void marshalOlapSchema(Schema schema, OutputStream out)  {
		try {
			JAXBContext context = JAXBContext.newInstance(Schema.class);
			Marshaller marshaller = context.createMarshaller();
			marshaller.marshal(schema, out);
		} catch (JAXBException e) {
			throw new RuntimeException(e);
		}
	}

	public Schema generateSchema(String surveyName) {
		SurveyMetadata surveyMetadata = getSurveyMetadata(surveyName);
		SchemaGenerator schemagen = new SchemaGenerator(surveyMetadata);
		return schemagen.generateSchema();
	}

//	@Deprecated
//	@Transactional
//	private void updateAreaFacts(String surveyName, FlatDataStream data) {
//		ObservationUnitMetadata plotMetadata = getPlotMetadata(surveyName);
//		areaFactDao.createOrUpdateAreaFactTable(data, plotMetadata.getVariableMetadata(), surveyName, AREA_FACT_TABLE_NAME);
//	}

//	private ObservationUnitMetadata getPlotMetadata(String surveyName) {
//		SurveyMetadata surveyMetadata = getSurveyMetadata(surveyName);
//		Collection<ObservationUnitMetadata> observationMetadata = surveyMetadata.getObservationMetadata();
//		for ( ObservationUnitMetadata obsUnit : observationMetadata ) {
//			if ( ObservationUnit.Type.PLOT.equals(obsUnit.getObsUnitType()) ) {
//				return obsUnit;
//			}
//		}
//		throw new IllegalArgumentException("Unable to find " + ObservationUnit.Type.PLOT.toString() + " observation unit for survey " + surveyName);
//	}

//	@Transactional
//	@Deprecated
//	private void updateSpecimenFacts(String surveyName, String obsUnitName, FlatDataStream data) throws IOException {
//		String tableName = obsUnitName + SPECIMEN_FACT_TABLE_NAME_SUFFIX;
//		ObservationUnitMetadata obs = getObservationUnitMetadata(surveyName, obsUnitName);
//		ObservationUnitMetadata obsParent = obs.getObsUnitParent();
//
//		List<VariableMetadata> vars = new ArrayList<VariableMetadata>();
//		vars.addAll(obs.getVariableMetadata());
//		vars.addAll(obsParent.getVariableMetadata());
//
//		factTableDao.createOrUpdateFactTable(data, vars, surveyName, tableName);
//	}

//	@Deprecated
//	private FlatDataStream getAreaFactData(String surveyName, String observationUnitName, PlotDistributionCalculationMethod plotDistributionCalculationMethod) {
//		ObservationUnitMetadata unitMetadata = getObservationUnitMetadata(surveyName, observationUnitName);
//		Collection<VariableMetadata> variables = unitMetadata.getVariableMetadata();
//		boolean useShares = plotDistributionCalculationMethod == PlotDistributionCalculationMethod.SHARED_PLOT;
//		return plotSectionViewDao.streamAreaFactData(variables, unitMetadata.getObsUnitId(), useShares);
//	}
//
//	@Deprecated
//	private FlatDataStream getSpecimenFactData(String surveyName, String observationUnitName) {
//		ObservationUnitMetadata unitMetadata = getObservationUnitMetadata(surveyName, observationUnitName);
//		Collection<VariableMetadata> variables = unitMetadata.getVariableMetadata();
//		Collection<VariableMetadata> parentVariables = unitMetadata.getObsUnitParent().getVariableMetadata();
//		return specimenDao.streamSpecimenFactData(unitMetadata.getObsUnitId(), variables, parentVariables);
//	}

//	@Deprecated
//	private Collection<VariableMetadata> getVariableMetadataForAnalysis(Collection<VariableMetadata> variables) {
//		List<VariableMetadata> varsForAnalysis = new ArrayList<VariableMetadata>();
//		for ( VariableMetadata variable : variables ) {
//			if ( variable.isCategorical() && variable.isForAnalysis() ) {
//				varsForAnalysis.add(variable);
//			}
//		}
//		return varsForAnalysis;
//	}

}
