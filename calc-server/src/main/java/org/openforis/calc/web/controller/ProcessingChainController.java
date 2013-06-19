/**
 * 
 */
package org.openforis.calc.web.controller;

import java.util.List;

import org.openforis.calc.engine.ProcessingChainService;
import org.openforis.calc.engine.Workspace;
import org.openforis.calc.engine.WorkspaceManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author M. Togna
 *
 */
@Controller
@RequestMapping(value="/rest")
public class ProcessingChainController {

	@Autowired
	private WorkspaceManager workspaceManager;
	
	@Autowired
	private ProcessingChainService processingChainService;
	
	@RequestMapping(value="/workspaces.json", method=RequestMethod.GET, produces="application/json")
	public @ResponseBody List<Workspace> getWorkspaces() {
		List<Workspace> workspaces = workspaceManager.loadAll();
		return workspaces;
	}
	
	public void getProcessingChainJob(int chainId) {
		
	}
	
	public void executeTasks() {
		
	}
	
	
//	@RequestMapping(value="/chain.json", method=RequestMethod.GET, produces="application/json")
//	public @ResponseBody Map<String,Integer> getViewChains(){
//		Map<String, Integer> map = new HashMap<String, Integer>();
//		map.put("1", 1);
//		System.out.println(map);
////		return "chain";
//		return map;
//	} 
//
//	
//	@RequestMapping(value="/rest/chain", method=RequestMethod.GET, produces="application/json")
//	public @ResponseBody Map<String,Integer> getViewChains2(){
//		Map<String, Integer> map = new HashMap<String, Integer>();
//		map.put("2", 1);
//		System.out.println(map);
////		return "chain";
//		return map;
//	} 
//	
//	@RequestMapping(value="/", method=RequestMethod.GET, produces="application/json")
//	public @ResponseBody Map<String,Integer> getViewChains3(){
//		Map<String, Integer> map = new HashMap<String, Integer>();
//		map.put("3", 1);
//		System.out.println(map);
////		return "chain";
//		return map;
//	} 
}
