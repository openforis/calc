package org.openforis.calc.service;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.openforis.calc.io.flat.FlatDataStream;
import org.openforis.calc.model.ObservationUnit;
import org.openforis.calc.model.ObservationUnitMetadata;
import org.openforis.calc.model.SurveyMetadata;
import org.openforis.calc.model.VariableMetadata;
import org.openforis.calc.olap.Schema;
import org.openforis.calc.persistence.AreaFactDao;
import org.openforis.calc.persistence.OlapDimensionDao;
import org.openforis.calc.persistence.PlotSectionViewDao;
import org.openforis.calc.persistence.SpecimenDao;
import org.openforis.calc.persistence.SpecimenFactDao;
import org.openforis.calc.service.ObservationService.PlotDistributionCalculationMethod;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 
 * @author Mino Togna
 * 
 */
@Service
public class OlapService extends CalcService {
	private static final String AREA_FACT_TABLE_NAME = "area_fact";
	private static final String SPECIMEN_FACT_TABLE_NAME_SUFFIX = "_fact";

	@Autowired
	private OlapDimensionDao olapDimensionDao;
	@Autowired
	private PlotSectionViewDao plotSectionViewDao;
	@Autowired
	private SpecimenDao specimenDao;
	@Autowired
	private AreaFactDao areaFactDao;
	@Autowired
	private SpecimenFactDao specimenFactDao;

	@Transactional
	public void publishData(String surveyName) {
		SurveyMetadata surveyMetadata = metadataService.getSurveyMetadata(surveyName);
		Collection<ObservationUnitMetadata> observationMetadata = surveyMetadata.getObservationMetadata();
		for ( ObservationUnitMetadata obsUnitMetadata : observationMetadata ) {
			String obsUnitName = obsUnitMetadata.getObsUnitName();
			Collection<VariableMetadata> variables = obsUnitMetadata.getVariableMetadata();
			if ( ObservationUnit.Type.PLOT.equals(obsUnitMetadata.getObsUnitType()) ) {
				FlatDataStream areaFactData = getAreaFactData(surveyName, obsUnitName, PlotDistributionCalculationMethod.PRIMARY_SECTION_ONLY);
				updateAreaFacts(surveyName, areaFactData);
			} else if ( ObservationUnit.Type.SPECIMEN.equals(obsUnitMetadata.getObsUnitType()) ) {
				FlatDataStream specimenFactData = getSpecimenFactData(surveyName, obsUnitName);
				updateSpecimenFacts(surveyName, obsUnitName, specimenFactData);
			}

			olapDimensionDao.generateOlapDimensions(surveyName, variables);
		}
	}
	
	public void saveSchema(Schema schema, String path) throws IOException, JAXBException{
		JAXBContext context = JAXBContext.newInstance(Schema.class);
		Marshaller marshaller = context.createMarshaller();
		OutputStream out = new BufferedOutputStream( new FileOutputStream(new File(path)) );
		marshaller.marshal(schema, out);
		out.flush();
		out.close();
	}
	
	public Schema generateSchema(String surveyName){
		SurveyMetadata surveyMetadata = metadataService.getSurveyMetadata(surveyName);
		return generateSchema(surveyMetadata);
	}
	
	public Schema generateSchema(SurveyMetadata surveyMetadata){
		Schema schema = new Schema( surveyMetadata );
		return schema;
	}

	@Transactional
	private void updateAreaFacts(String surveyName, FlatDataStream data) {
		ObservationUnitMetadata plotMetadata = getPlotMetadata( surveyName );
		areaFactDao.createOrUpdateAreaFactTable(data,plotMetadata.getVariableMetadata(), surveyName, AREA_FACT_TABLE_NAME);
	}

	private ObservationUnitMetadata getPlotMetadata(String surveyName) {
		SurveyMetadata surveyMetadata = metadataService.getSurveyMetadata(surveyName);
		Collection<ObservationUnitMetadata> observationMetadata = surveyMetadata.getObservationMetadata();
		for ( ObservationUnitMetadata obsUnit : observationMetadata ) {
			if( ObservationUnit.Type.PLOT.equals( obsUnit.getObsUnitType() ) ){
				return obsUnit;
			}
		}
		throw new IllegalArgumentException("Unable to find " + ObservationUnit.Type.PLOT.toString() + " observation unit for survey " + surveyName);
	}

	@Transactional
	private void updateSpecimenFacts(String surveyName, String obsUnitName, FlatDataStream data) {
		String tableName = obsUnitName + SPECIMEN_FACT_TABLE_NAME_SUFFIX;
		ObservationUnitMetadata obs = getObservationUnitMetadata(surveyName, obsUnitName);
		ObservationUnitMetadata obsParent = obs.getObsUnitParent();
		
		List<VariableMetadata> vars = new ArrayList<VariableMetadata>();
		vars.addAll( obs.getVariableMetadata() );
		vars.addAll( obsParent.getVariableMetadata() );
		
		specimenFactDao.createOrUpdateFactTable(data, vars, surveyName, tableName);
	}

	private FlatDataStream getAreaFactData(String surveyName, String observationUnitName, PlotDistributionCalculationMethod plotDistributionCalculationMethod) {
		ObservationUnitMetadata unitMetadata = getObservationUnitMetadata(surveyName, observationUnitName);
		Collection<VariableMetadata> variables = unitMetadata.getVariableMetadata();
		boolean useShares = plotDistributionCalculationMethod == PlotDistributionCalculationMethod.SHARED_PLOT;
		return plotSectionViewDao.streamAreaFactData(variables, unitMetadata.getObsUnitId(), useShares);
	}

	private FlatDataStream getSpecimenFactData(String surveyName, String observationUnitName) {
		ObservationUnitMetadata unitMetadata = getObservationUnitMetadata(surveyName, observationUnitName);
		Collection<VariableMetadata> variables = unitMetadata.getVariableMetadata();
		Collection<VariableMetadata> parentVariables = unitMetadata.getObsUnitParent().getVariableMetadata();
		return specimenDao.streamSpecimenFactData(unitMetadata.getObsUnitId(), variables, parentVariables);
	}
}
