package test;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.openforis.calc.engine.Workspace;
import org.openforis.calc.engine.WorkspaceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:applicationContext.xml" , "classpath:applicationContext-config.xml" , "classpath:applicationContext-persistence.xml"} )
public class QueryTest {

//	@Autowired
//	SamplingDesignDao dao;
	
	@Autowired
	WorkspaceService workspaceService;
	
    @Test
    public void testJoin() throws Exception {
    	
    	Workspace ws = workspaceService.getActiveWorkspace();
    	
    	System.out.println( "aaaaaaaaaaaaa"  + ws.getName());
    	
//    	
//    	SamplingDesign o = dao.findById(12);
//    	
//    	ParameterMap s = o.getPhase1JoinSettings();
//    	System.out.println( s.toJsonString() );
//    	
    }
}