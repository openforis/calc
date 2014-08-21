/**
 * 
 */
package org.openforis.calc.web.controller;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.parser.ParseException;
import org.openforis.calc.chain.CalculationStep;
import org.openforis.calc.chain.InvalidProcessingChainException;
import org.openforis.calc.chain.ProcessingChain;
import org.openforis.calc.engine.CalcJob;
import org.openforis.calc.engine.CalcTestJob;
import org.openforis.calc.engine.DataRecord;
import org.openforis.calc.engine.Job;
import org.openforis.calc.engine.ParameterMap;
import org.openforis.calc.engine.TaskManager;
import org.openforis.calc.engine.Workspace;
import org.openforis.calc.engine.WorkspaceLockedException;
import org.openforis.calc.engine.WorkspaceService;
import org.openforis.calc.persistence.jooq.ParameterMapConverter;
import org.openforis.commons.io.OpenForisIOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author Mino Togna
 * 
 */
@Controller
@RequestMapping(value = "/rest/job")
public class JobController {
	@Autowired
	private WorkspaceService workspaceService;

	@Autowired
	private TaskManager taskManager;

	@RequestMapping(value = "/processing-chain.R", method = RequestMethod.POST )
	public void downloadRCode(HttpServletResponse response) throws ParseException {
		
		StringBuilder sb = new StringBuilder();
		
		Workspace activeWorkspace = workspaceService.getActiveWorkspace();
		if (activeWorkspace != null) {
			Job job = taskManager.getJob( activeWorkspace.getId() );
			if( job instanceof CalcJob ){
				String jobString = job.toString();
				sb.append( jobString );
			} 
		}
		
		if( sb.length() <= 0 ){
			sb.append( "print('No job foud');" );
		}
		
		try {
			//prepare response header
			SimpleDateFormat dateFormat = new SimpleDateFormat( "yyyy-MM-dd-HH-mm" );
			String formattedDate = dateFormat.format(new Date());
			String fileName = String.format("%s-%s-%s.%s", "processing-chain" , activeWorkspace.getName() , formattedDate , "R" );
			
			response.setContentType( "text/plain" ); 
			response.setHeader( "Content-Disposition", "attachment; filename=" + fileName );
			
			//create csv writer
			ServletOutputStream outputStream = response.getOutputStream();
			BufferedWriter writer = new BufferedWriter( new OutputStreamWriter(outputStream, OpenForisIOUtils.UTF_8) );
			writer.write( sb.toString() );
			writer.close();
		} catch (IOException e) {
			throw new RuntimeException( "Error exporting to R code" , e );
		}
	}
	/**
	 * Execute a job for the given calculation step id
	 * 
	 * @param stepId
	 * @return
	 * @throws InvalidProcessingChainException
	 * @throws WorkspaceLockedException
	 */
	@RequestMapping(value = "/step/{stepId}/execute.json", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody
	synchronized Job executeCalculationStep(@PathVariable int stepId) throws InvalidProcessingChainException, WorkspaceLockedException {
		Workspace workspace = workspaceService.getActiveWorkspace();
		
		ProcessingChain defaultProcessingChain = workspace.getDefaultProcessingChain();
		
		CalculationStep step = defaultProcessingChain.getCalculationStepById( stepId );

//		workspaceService.updateResultTable( step );
		
		CalcJob job = taskManager.createCalcJob( workspace );
		job.addCalculationStep(step);
		job.setTempResults(true);
		
		taskManager.startJob(job);

		return job;
	}

	/**
	 * Execute default processing chain
	 * @return
	 * @throws InvalidProcessingChainException
	 * @throws WorkspaceLockedException
	 */
	@RequestMapping(value = "/execute.json", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody
	synchronized Job execute() throws InvalidProcessingChainException, WorkspaceLockedException {
		Workspace workspace = workspaceService.getActiveWorkspace();
		
		CalcJob job = taskManager.createDefaultCalcJob( workspace , true );
		workspaceService.resetResults( workspace );
		
		taskManager.startJob( job );

		return job;
	}
	
	/**
	 * Creates a job that tests the calculation step with the given id
	 * 
	 * @param stepId
	 * @param variables Variable parameters in JSON format
	 * @return
	 * @throws InvalidProcessingChainException
	 * @throws WorkspaceLockedException
	 */
	@RequestMapping(value = "/test/execute.json", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody
	synchronized Job testCalculationStep(@RequestParam int stepId, @RequestParam String variables) throws InvalidProcessingChainException, WorkspaceLockedException {
		Workspace workspace = workspaceService.getActiveWorkspace();
		
		ProcessingChain defaultProcessingChain = workspace.getDefaultProcessingChain();
		
		CalculationStep step = defaultProcessingChain.getCalculationStepById(stepId);

		ParameterMap parameterMap = new ParameterMapConverter().from( variables );

		CalcTestJob job = taskManager.createCalcTestJob(workspace, step, parameterMap);
		job.setCalculationStep(step);
		
		taskManager.startJob(job);

		return job;
	}

	@RequestMapping(value = "/test/results.json", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody
	List<DataRecord> getTestResults(@RequestParam String jobId, @RequestParam int offset, @RequestParam(value = "numberOfRows", required=false) Integer numberOfRows) {
		CalcTestJob job = getTestJob(jobId);
		if ( numberOfRows == null ) {
			numberOfRows = 50;
		}
		List<DataRecord> results = job.getResults(offset, offset + numberOfRows);
		return results;
	}
	
	@RequestMapping(value = "/test/results/count.json", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody 
	Response getTestResultsCount(@RequestParam String jobId) {
		Response response = new Response();
		CalcTestJob job = getTestJob(jobId);
		long count = job.getResultsCount();
		response.addField("count", count);
		return response;
	}
	
	private CalcTestJob getTestJob(String jobId) {
		Workspace workspace = workspaceService.getActiveWorkspace();
		Job job = taskManager.getJob(workspace.getId());
		
		if( job == null || ! job.getId().toString().equals(jobId) ){
			throw new IllegalArgumentException("Job with id " + jobId + " not found for the active workspace");
		}
		return (CalcTestJob) job;
	}
	
}
