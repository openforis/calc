package org.openforis.calc.dataimport.collect;

import java.io.IOException;
import java.util.List;

import org.openforis.calc.dataimport.ImportException;
import org.openforis.calc.model.ModelObject;
import org.openforis.calc.model.ObservationUnit;
import org.openforis.calc.model.SurveySourceMap;
import org.openforis.calc.model.SurveyedCluster;
import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.persistence.xml.DataHandler;
import org.openforis.collect.persistence.xml.DataHandler.NodeUnmarshallingError;
import org.openforis.collect.persistence.xml.DataUnmarshaller;
import org.openforis.collect.persistence.xml.DataUnmarshaller.ParseRecordResult;
import org.openforis.collect.persistence.xml.DataUnmarshallerException;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.metamodel.xml.IdmlParseException;
import org.openforis.idm.model.Attribute;
import org.openforis.idm.model.Entity;
import org.openforis.idm.model.Node;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * 
 * @author G. Miceli
 *
 */
public class CollectDataLoader extends CollectLoaderBase {
	
	// TODO partially refactor into DataService?

	private DataUnmarshaller dataUnmarshaller;
	private SurveySourceMap sourceMap;
	
	public static void main(String[] args)  {
		try {
			ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("applicationContext.xml");
			CollectDataLoader loader = ctx.getBean(CollectDataLoader.class);
			loader.importData(TEST_PATH, 3);

		} catch ( Throwable ex ) {
			ex.printStackTrace();
		}
	}

	synchronized
	public void importData(String path, int step) throws IOException, ImportException, InvalidMetadataException {
		try {
			collectSurvey = metadataService.loadIdml(path+IDML_FILENAME);
			survey = surveyDao.fetchByUri(collectSurvey.getUri());
			sourceMap = metadataService.loadSourceMap(survey.getId());
			DataHandler handler = new DataHandler(collectSurvey, null);
			dataUnmarshaller = new DataUnmarshaller(handler);
			importXml(path+"3/699.xml");
		} catch (IdmlParseException e) {
			throw new ImportException(e);
		}
	}

	private void importXml(String filename) throws ImportException, InvalidMetadataException {
		ParseRecordResult result;
		try {
			result = dataUnmarshaller.parse(filename);
			if ( !result.isSuccess() ) {
				logFailures(filename, result);
				throw new ImportException("Failed to load data");
			}
			logWarnings(filename, result);
			CollectRecord record = result.getRecord();
			importRecord(record);
		} catch (DataUnmarshallerException e) {
			throw new ImportException(e);
		}
	}

	private void importRecord(CollectRecord record) throws ImportException, InvalidMetadataException {
		Entity root = record.getRootEntity();
		traverse(root, null, null, null);
	}

	private void traverse(Entity entity, ObservationUnit parentUnit, SurveyedCluster cluster, Integer parentId) 
				throws ImportException, InvalidMetadataException {
		EntityDefinition defn = entity.getDefinition();
		EntityType type = getEntityType(defn);
		if ( type != null ) {
			switch (type) {
			case CLUSTER:
				if ( parentUnit != null ) {
					throw new InvalidMetadataException("'cluster' must be above all observation units");
				}
				cluster = importCluster(entity);
//				log.info("Cluster entity found: "+entity.getPath());
				break;
			case PLOT:
//				parentUnit = importPlot(parentUnit, entity, parentId);
				break;
			case SPECIMEN:
//				parentUnit = importSpecimen(parentUnit, entity, parentId);
				break;
			default:
				throw new RuntimeException("Unimplemented entity type '"+type+"'");
			}
			
			int defnId = defn.getId();
			ModelObject obj = sourceMap.getModelObject(defnId);
			if ( obj instanceof ObservationUnit ) {
				ObservationUnit level = (ObservationUnit) obj;
				
				parentUnit = level;
			} else {
				throw new ImportException("Entity "+defn.getPath()+" mapped to invalid "+obj.getClass());
			}

		}
	}

	private SurveyedCluster importCluster(Entity entity) {
		SurveyedCluster cluster = new SurveyedCluster();
		List<Node<?>> children = entity.getChildren();
		for (Node<?> child : children) {
			NodeDefinition defn = child.getDefinition();
			// TODO Preload cluster codes --> id
			// TODO Assign cluster id
			// TODO Assign survey date
		}
		return cluster;
	}

	private void logWarnings(String filename, ParseRecordResult result) {
		List<NodeUnmarshallingError> warns = result.getWarnings();
		if ( warns!=null && !warns.isEmpty() ) {
			log.warn(filename+" contains warnings:");
			logUnmarshallingErrors(warns);
		}
	}

	private void logFailures(String filename, ParseRecordResult result) {
		List<NodeUnmarshallingError> fails = result.getFailures();
		if ( fails!=null && !fails.isEmpty() ) {
			log.warn(filename+" contains errors:");
			logUnmarshallingErrors(fails);
		}
	}

	private void logUnmarshallingErrors(List<NodeUnmarshallingError> errors) {
		for (NodeUnmarshallingError error : errors) {
			log.warn(error.getPath()+": "+error.getMessage());
		}
	}
}
