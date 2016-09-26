/**
 * 
 */
package org.openforis.calc.chain;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.input.Tailer;
import org.apache.commons.io.input.TailerListener;
import org.apache.commons.io.input.TailerListenerAdapter;
import org.openforis.calc.Calc;
import org.openforis.calc.chain.export.ROutputScript;
import org.openforis.calc.engine.CalculationException;
import org.openforis.calc.engine.Job;
import org.openforis.calc.engine.Task;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author M. Togna
 *
 */
public class ProcessingChainTask extends Task {

	@Autowired
	private ProcessingChainService processingChainService;
	
	@Autowired
	private Calc calc;

	private List<ROutputScript> scripts;
	
	private File processingChainDir;

	private StringBuffer logBuffer;
	
	private int currentTask;

	private String errorStackTrace;

	private Tailer tailer;

	private Thread thread;

	public ProcessingChainTask() throws IOException {
		super();

		logBuffer = new StringBuffer();
		currentTask = -99;
	}

//	@Override
//	protected void setJob(Job job) {
//		super.setJob(job);
//		
//		
//	}

	@Override
	public synchronized void init() {		
//		this.scripts = processingChainService.createROutputScripts(getWorkspace(), true);
		try {
			this.initChain();
		} catch (IOException e) {
			throw new RuntimeException("Unable to create Processing Chain Task", e);
		}
		super.init();
	}
	
	private void initChain() throws IOException {
		this.scripts = processingChainService.createROutputScripts(getWorkspace(), true);

		File calcUserHome = calc.getCalcUserHomeDirectory();

		this.processingChainDir = processingChainService.exportToDir(getWorkspace(), scripts, calcUserHome, true);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openforis.calc.engine.Worker#execute()
	 */
	@Override
	protected void execute() throws Throwable {
						
		executeChain();

		while (true) {		
			
			if (currentTask > 0) {
				setItemsProcessed(currentTask);
			} else if (currentTask == -1) {
				this.setErrorStackTrace(this.logBuffer.toString());

				throw new CalculationException("Error while executing the R processing chain");
			}

			if (currentTask == getTotalItems()) {
				this.tailer.stop();
				this.thread.interrupt();

				this.processingChainDir.deleteOnExit();

				break;
			}

			Thread.sleep(500);
		}

	}

	private void executeChain() throws IOException {
		File chain = new File(this.processingChainDir, "chain.R");
		
		String rscriptCommand = calc.getRscriptCommandPath();
		String chainPath = chain.getAbsolutePath();
		if(calc.isWindows()){
			chainPath = "\""+chainPath + "\"";
		}
		Runtime.getRuntime().exec(rscriptCommand + " " + chainPath);

		File logFile = new File(this.processingChainDir, "output.txt");
		TailerListener listener = this.new TailerListner();
		long delay = 500;

		this.tailer = new Tailer(logFile, listener, delay);
		this.thread = new Thread(tailer);
		this.thread.start();
	}

	@Override
	protected long countTotalItems() {
		return scripts.size() - 1;
	}

	@Override
	public String getName() {
		return "R Scripts - Processing chain";
	}

	@Override
	public String getErrorStackTrace() {
		return this.errorStackTrace;
	}

	public void setErrorStackTrace(String errorStackTrace) {
		this.errorStackTrace = errorStackTrace;
	}
	
	
	private class TailerListner extends TailerListenerAdapter {
		private String regex = "(={10})(-?\\d+)";
		private Pattern pattern = Pattern.compile(regex);

		@Override
		public void handle(String line) {
			super.handle(line);

			Matcher matcher = pattern.matcher(line);

			if (matcher.matches()) {
				currentTask = Integer.parseInt(matcher.group(2));
			} else {
				logBuffer.append(line);
				logBuffer.append("\n");
			}
		}

	}
}
