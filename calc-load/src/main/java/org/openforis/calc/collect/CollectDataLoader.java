package org.openforis.calc.collect;

import java.io.IOException;
import java.util.List;

import org.openforis.calc.model.ModelObject;
import org.openforis.calc.model.ObservationUnit;
import org.openforis.calc.model.SurveySourceMap;
import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.persistence.xml.DataHandler;
import org.openforis.collect.persistence.xml.DataHandler.NodeUnmarshallingError;
import org.openforis.collect.persistence.xml.DataUnmarshaller;
import org.openforis.collect.persistence.xml.DataUnmarshaller.ParseRecordResult;
import org.openforis.collect.persistence.xml.DataUnmarshallerException;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.xml.IdmlParseException;
import org.openforis.idm.model.Entity;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * 
 * @author G. Miceli
 *
 */
public class CollectDataLoader extends CollectLoaderBase {
	
	// TODO to partially refactor into DataService

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
	public void importData(String path, int step) throws IOException, DataImportException {
		try {
			collectSurvey = metadataService.loadIdml(path+IDML_FILENAME);
			survey = surveyDao.fetchByUri(collectSurvey.getUri());
			sourceMap = metadataService.loadSourceMap(survey.getId());
			DataHandler handler = new DataHandler(collectSurvey, null);
			dataUnmarshaller = new DataUnmarshaller(handler);
			importXml(path+"3/699.xml");
		} catch (IdmlParseException e) {
			throw new DataImportException(e);
		} catch (DataUnmarshallerException e) {
			throw new DataImportException(e);
		}
	}

	private void importXml(String filename) throws DataUnmarshallerException, DataImportException {
		ParseRecordResult result = dataUnmarshaller.parse(filename);
		if ( !result.isSuccess() ) {
			logFailures(filename, result);
			throw new DataUnmarshallerException("Failed to load data");
		}
		logWarnings(filename, result);
		CollectRecord record = result.getRecord();
		importRecord(record);
		
	}

	private void importRecord(CollectRecord record) throws DataImportException {
		Entity root = record.getRootEntity();
		traverse(root, null);
	}

	private void traverse(Entity entity, ObservationUnit parentUnit) throws DataImportException {
		EntityDefinition defn = entity.getDefinition();
		int defnId = defn.getId();
		ModelObject obj = sourceMap.getModelObject(defnId);
		if ( obj instanceof ObservationUnit ) {
			ObservationUnit level = (ObservationUnit) obj;
			
			parentUnit = level;
		} else {
			throw new DataImportException("Entity "+defn.getPath()+" mapped to invalid "+obj.getClass());
		}
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
