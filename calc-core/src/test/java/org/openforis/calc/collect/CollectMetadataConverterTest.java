package org.openforis.calc.collect;

import static org.junit.Assert.assertFalse;

import java.io.InputStream;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.openforis.calc.engine.Workspace;
import org.openforis.calc.metadata.Entity;
import org.openforis.collect.model.CollectSurveyContext;
import org.openforis.collect.model.validation.CollectValidator;
import org.openforis.collect.persistence.xml.CollectSurveyIdmlBinder;
import org.openforis.collect.relational.CollectRdbException;
import org.openforis.collect.relational.model.RelationalSchema;
import org.openforis.collect.relational.model.RelationalSchemaGenerator;
import org.openforis.idm.metamodel.Survey;
import org.openforis.idm.metamodel.xml.IdmlParseException;
import org.openforis.idm.model.expression.ExpressionFactory;

/**
 * 
 * @author S. Ricci
 *
 */
public class CollectMetadataConverterTest {

	private Survey survey;
	private RelationalSchema rdbSchema;

	@Before
	public void setup() throws IdmlParseException, CollectRdbException {
		InputStream idmIs = ClassLoader.getSystemResourceAsStream("test.idm.xml");
		CollectSurveyContext context = new CollectSurveyContext(new ExpressionFactory(), new CollectValidator());
		CollectSurveyIdmlBinder binder = new CollectSurveyIdmlBinder(context);
		survey = binder.unmarshal(idmIs);
		RelationalSchemaGenerator rdbGenerator = new RelationalSchemaGenerator();
		rdbSchema = rdbGenerator.generateSchema(survey, "test");
	}
	
	@Test
	public void test() throws CollectRdbException {
		Workspace workspace = new Workspace();
		workspace.setId(1);
		CollectMetadataSynchronizer converter = new CollectMetadataSynchronizer(workspace, rdbSchema);
		List<Entity> entities = converter.sync();
		assertFalse(entities.isEmpty());
	}
	
}
