package org.openforis.calc.collect;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openforis.calc.engine.Job;
import org.openforis.calc.engine.WorkspaceLockedException;
import org.openforis.calc.engine.WorkspaceService;
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
	private WorkspaceService workspaceManager;
	
	@Test
	public void testCollectMetadataSync() throws WorkspaceLockedException {
//		Workspace ws = new Workspace();
//		ws.setName("test");
//		ws.setInputSchema("naforma1");
//		ws.setOutputSchema("calc");
//		workspaceManager.save(ws);
//		Integer workspaceId = ws.getId();
//		Job job = collectMetadataService.startSync(workspaceId);
		Job job = collectMetadataService.startSync(174);
		boolean ok = job.waitFor(30000);
		Assert.assertTrue(ok);
	}
}
