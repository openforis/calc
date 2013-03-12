package org.openforis.calc.service;

import java.io.IOException;
import java.util.List;

import org.openforis.calc.model.SurveyMetadata;
import org.openforis.calc.persistence.InterviewFactDao;
import org.openforis.calc.persistence.PlotAggregateDao;
import org.openforis.calc.persistence.PlotFactDao;
import org.openforis.calc.persistence.RolapDimensionDao;
import org.openforis.calc.persistence.RolapSchemaDao;
import org.openforis.calc.persistence.SpecimenAggregateDao;
import org.openforis.calc.persistence.SpecimenFactDao;
import org.openforis.calc.persistence.jooq.rolap.AoiDimensionTable;
import org.openforis.calc.persistence.jooq.rolap.CategoryDimensionTable;
import org.openforis.calc.persistence.jooq.rolap.ClusterDimensionTable;
import org.openforis.calc.persistence.jooq.rolap.InterviewFactTable;
import org.openforis.calc.persistence.jooq.rolap.PlotAoiAggregateTable;
import org.openforis.calc.persistence.jooq.rolap.PlotAoiStratumAggregateTable;
import org.openforis.calc.persistence.jooq.rolap.PlotDimensionTable;
import org.openforis.calc.persistence.jooq.rolap.PlotFactTable;
import org.openforis.calc.persistence.jooq.rolap.RolapSchemaDefinition;
import org.openforis.calc.persistence.jooq.rolap.RolapSchemaGenerator;
import org.openforis.calc.persistence.jooq.rolap.RolapTable;
import org.openforis.calc.persistence.jooq.rolap.SpecimenDimensionTable;
import org.openforis.calc.persistence.jooq.rolap.SpecimenFactTable;
import org.openforis.calc.persistence.jooq.rolap.SpecimenPlotAggregateTable;
import org.openforis.calc.persistence.jooq.rolap.StratumDimensionTable;
import org.openforis.calc.persistence.jooq.rolap.TaxonDimensionTable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 
 * @author M. Togna
 * @author G. Miceli
 * 
 */
@Service
public class RolapService extends CalcService {

	@Autowired
	private RolapDimensionDao rolapDimensionDao;
	@Autowired
	private RolapSchemaDao rolapSchemaDao;
	@Autowired
	private PlotFactDao plotFactDao;
	@Autowired
	private PlotAggregateDao plotAggregateDao;
	@Autowired
	private SpecimenFactDao specimenFactDao;
	@Autowired
	private SpecimenAggregateDao specimenAggregateDao;
	@Autowired
	private InterviewFactDao interviewFactDao;
	
	@Transactional
	public void publishRolapSchema(String surveyName, String targetDatabaseSchema, String fileName) throws IOException {
		SurveyMetadata surveyMetadata = getSurveyMetadata(surveyName);
	
		RolapSchemaGenerator rsg = new RolapSchemaGenerator(surveyMetadata, targetDatabaseSchema);
		RolapSchemaDefinition defn = rsg.generateDefinition();

		// Create database
		List<RolapTable> tables = defn.getDatabaseTables();
		rolapSchemaDao.createSchema(tables);
		
		// Insert data
		populateTables(tables);
		
		// Save Mondrian schema
		// TODO create dirs, dynamically set path, etc.
		defn.saveMondrianSchemaXml(fileName);
	}
	
	private void populateTables(List<RolapTable> tables) {
		for ( RolapTable table : tables ) {
			if ( table instanceof AoiDimensionTable ) {
				rolapDimensionDao.populate((AoiDimensionTable) table);
			} else if ( table instanceof CategoryDimensionTable ) {
				rolapDimensionDao.populate((CategoryDimensionTable) table);
			} else if ( table instanceof ClusterDimensionTable ) {
				rolapDimensionDao.populate((ClusterDimensionTable) table);
			} else if ( table instanceof PlotDimensionTable ) {
				rolapDimensionDao.populate((PlotDimensionTable) table);
			} else if ( table instanceof StratumDimensionTable ) {
				rolapDimensionDao.populate((StratumDimensionTable) table);
			} else if ( table instanceof SpecimenDimensionTable ) {
				rolapDimensionDao.populate((SpecimenDimensionTable) table);
			} else if ( table instanceof TaxonDimensionTable ) {
				rolapDimensionDao.populate((TaxonDimensionTable) table);
			} else if ( table instanceof PlotFactTable ) {
				PlotFactTable factTable = (PlotFactTable) table;
				plotFactDao.populate(factTable);
			} else if ( table instanceof PlotAoiStratumAggregateTable ) {
				plotAggregateDao.populate((PlotAoiStratumAggregateTable) table);
			} else if ( table instanceof PlotAoiAggregateTable ) {
				plotAggregateDao.populate((PlotAoiAggregateTable) table);
			} else if ( table instanceof SpecimenFactTable ) {
				SpecimenFactTable factTable = (SpecimenFactTable) table;
				specimenFactDao.populate(factTable);
			} else if ( table instanceof SpecimenPlotAggregateTable ) {
				specimenAggregateDao.populate((SpecimenPlotAggregateTable) table);
				// TODO speciemn aggregates
			} else if ( table instanceof InterviewFactTable ) {
				InterviewFactTable factTable = (InterviewFactTable) table;
				interviewFactDao.populate(factTable);
			} else {
				// throw new UnsupportedOperationException("Unknown table "+table.getClass());
			}
		}

	}

//	@Transactional
//	private void populateTables(SurveyMetadata surveyMetadata) {
//		int surveyId = surveyMetadata.getSurveyId();
////		String surveyName = surveyMetadata.getSurveyName();
//		
//		olapDimensionDao.populateAoiDimensionTables(surveyId);
//		
//		Collection<ObservationUnitMetadata> observationMetadata = surveyMetadata.getObservationMetadata();
//		for ( ObservationUnitMetadata obsUnitMetadata : observationMetadata ) {
//			Collection<VariableMetadata> variables = obsUnitMetadata.getVariableMetadata();
////			olapDimensionDao.createOlapDimensionTables(surveyMetadata.getSurveyId(), variables);
////			String obsUnitName = obsUnitMetadata.getObsUnitName();
//			Type unitType = obsUnitMetadata.getObsUnitTypeEnum();
//			switch (unitType) {
//			case PLOT:
////				olapDimensionDao.populateOlapDimensionTables(surveyId, variables);
////				// plot fact table
////				plotFactTableDao.populatePlotFactTable(obsUnitMetadata);
////				
//////				FlatDataStream areaFactData = getAreaFactData(surveyName, obsUnitName, PlotDistributionCalculationMethod.PRIMARY_SECTION_ONLY);
//////				updateAreaFacts(surveyName, areaFactData);
//				break;
//			case SPECIMEN:
////				FlatDataStream specimenFactData = getSpecimenFactData(surveyName, obsUnitName);
////				updateSpecimenFacts(surveyName, obsUnitName, specimenFactData);
//				break;
//			case INTERVIEW:
//				olapDimensionDao.populateDimensionTables(surveyId, variables);
//				break;
//			}
//		}
//	}
	
//	private void createTables(SurveyMetadata surveyMetadata) {
//		int surveyId = surveyMetadata.getSurveyId();
////		String surveyName = surveyMetadata.getSurveyName();
//		
//		olapDimensionDao.createAoiOlapDimensionTables(surveyId);
//		
//		Collection<ObservationUnitMetadata> observationMetadata = surveyMetadata.getObservationMetadata();
//		for ( ObservationUnitMetadata obsUnitMetadata : observationMetadata ) {
//			Collection<VariableMetadata> variables = obsUnitMetadata.getVariableMetadata();
//			
//			Type unitType = obsUnitMetadata.getObsUnitTypeEnum();
//			switch (unitType) {
//			case PLOT:
////				olapDimensionDao.createVariableDimensionTables(variables);
////				// plot fact table				
////				plotFactTableDao.createPlotFactTable(obsUnitMetadata);
////				
//////				FlatDataStream areaFactData = getAreaFactData(surveyName, obsUnitName, PlotDistributionCalculationMethod.PRIMARY_SECTION_ONLY);
//////				updateAreaFacts(surveyName, areaFactData);
//
//				break;
//			case SPECIMEN:
////				olapDimensionDao.createVariableDimensionTables(variables);
//////				FlatDataStream specimenFactData = getSpecimenFactData(surveyName, obsUnitName);
//////				updateSpecimenFacts(surveyName, obsUnitName, specimenFactData);
//				break;
//			case INTERVIEW:
//				olapDimensionDao.createVariableDimensionTables(variables);
//				plotFactTableDao.createPlotFactTable(obsUnitMetadata);
//				break;
//			}
//		}
//	}


	private SurveyMetadata getSurveyMetadata(String surveyName) {
		SurveyMetadata surveyMetadata = metadataService.getSurveyMetadata(surveyName);
		if( surveyMetadata == null){
			throw new IllegalArgumentException("Survey " + surveyName + " not found");
		}
		return surveyMetadata;
	}

	

	

//	public void saveSchema(Schema schema, String path) throws IOException, JAXBException {
//		OutputStream out = new BufferedOutputStream(new FileOutputStream(new File(path)));
//		marshalOlapSchema(schema, out);
//		out.flush();
//		out.close();
//	}

//	public void marshalOlapSchema(Schema schema, OutputStream out)  {
//		try {
//			JAXBContext context = JAXBContext.newInstance(Schema.class);
//			Marshaller marshaller = context.createMarshaller();
//			marshaller.marshal(schema, out);
//		} catch (JAXBException e) {
//			throw new RuntimeException(e);
//		}
//	}

//	public Schema generateSchema(String surveyName) {
//		SurveyMetadata surveyMetadata = getSurveyMetadata(surveyName);
//		RolapSchemaGenerator schemagen = new RolapSchemaGenerator(surveyMetadata);
//		return schemagen.generateSchema();
//	}

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
