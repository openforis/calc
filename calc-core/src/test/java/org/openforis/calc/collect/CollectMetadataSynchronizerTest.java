package org.openforis.calc.collect;

import java.io.InputStream;

import org.junit.Before;
import org.junit.Test;
import org.openforis.calc.engine.Workspace;
import org.openforis.collect.model.CollectSurveyContext;
import org.openforis.collect.model.validation.CollectValidator;
import org.openforis.collect.persistence.xml.CollectSurveyIdmlBinder;
import org.openforis.collect.relational.CollectRdbException;
import org.openforis.idm.metamodel.Survey;
import org.openforis.idm.metamodel.xml.IdmlParseException;
import org.openforis.idm.model.expression.ExpressionFactory;

/**
 * 
 * @author S. Ricci
 *
 */
public class CollectMetadataSynchronizerTest {

	private Survey survey;

	@Before
	public void setup() throws IdmlParseException {
		InputStream idmIs = ClassLoader.getSystemResourceAsStream("test.idm.xml");
		CollectSurveyContext context = new CollectSurveyContext(new ExpressionFactory(), new CollectValidator());
		CollectSurveyIdmlBinder binder = new CollectSurveyIdmlBinder(context);
		survey = binder.unmarshal(idmIs);
	}
	
	@Test
	public void test() throws CollectRdbException {
		CollectMetadataSynchronizer synchronizer = new CollectMetadataSynchronizer();
		Workspace workspace = new Workspace();
		workspace.setId(1);
		synchronizer.importMetadata(workspace, survey, "test");
	}
	
}
