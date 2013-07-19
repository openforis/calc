package org.openforis.calc.collect;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.openforis.calc.engine.Workspace;
import org.openforis.calc.engine.WorkspaceLockedException;
import org.openforis.calc.engine.WorkspaceManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

/**
 * 
 * @author S. Ricci
 *
 */
@RunWith( SpringJUnit4ClassRunner.class )
@ContextConfiguration( locations = {"classpath:applicationContext.xml"} )
@TransactionConfiguration(defaultRollback=true)
@Transactional
public class CollectMetadataServiceIntegrationTest {

	@Autowired
	private CollectMetadataService collectMetadataService;
	@Autowired
	private WorkspaceManager workspaceManager;
	
	@Test
	public void testCollectMetadataImport() throws WorkspaceLockedException {
		Workspace ws = new Workspace();
		ws.setName("test");
		ws.setInputSchema("collect_schema");
		ws.setOutputSchema("calc_schema");
		workspaceManager.save(ws);
		collectMetadataService.importMetadata();
	}
	
}
