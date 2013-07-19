package org.openforis.calc.collect;

import java.io.InputStream;
import java.util.List;

import org.openforis.calc.engine.Task;
import org.openforis.calc.engine.Workspace;
import org.openforis.calc.engine.WorkspaceManager;
import org.openforis.calc.metadata.Entity;
import org.openforis.collect.persistence.xml.CollectSurveyIdmlBinder;
import org.openforis.collect.relational.CollectRdbException;
import org.openforis.collect.relational.model.RelationalSchema;
import org.openforis.collect.relational.model.RelationalSchemaGenerator;
import org.openforis.idm.metamodel.Survey;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * @author S. Ricci
 *
 */
public class CollectMetadataImportTask extends Task {

	@Autowired
	private WorkspaceManager workspaceManager;
	
	@Autowired
	private CollectSurveyIdmlBinder collectSurveyIdmlBinder;
	
	@Override
	protected void execute() throws Throwable {
		// TODO get survey from input schema
		// Workspace ws = getContext().getWorkspace();
		InputStream surveyIs = getClass().getClassLoader().getResourceAsStream(
				"test.idm.xml");
		importMetadata(surveyIs);
	}

	protected void importMetadata(InputStream surveyIs) {
		try {
			Survey survey = collectSurveyIdmlBinder.unmarshal(surveyIs);
			importMetadata(survey);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected void importMetadata(Survey survey) {
		Workspace ws = getContext().getWorkspace();
		// generate rdb schema
		RelationalSchema schema = generateSchema(ws, survey);
		// convert into entities
		CollectMetadataConverter metadataConverter = new CollectMetadataConverter();
		List<Entity> entities = metadataConverter.convert(ws, schema);

		ws.setEntities(entities);
		workspaceManager.save(ws);
	}

	protected RelationalSchema generateSchema(Workspace ws, Survey survey) {
		RelationalSchemaGenerator rdbGenerator = new RelationalSchemaGenerator();
		RelationalSchema schema;
		try {
			schema = rdbGenerator.generateSchema(survey, ws.getInputSchema());
		} catch (CollectRdbException e) {
			throw new RuntimeException(e);
		}
		return schema;
	}
}