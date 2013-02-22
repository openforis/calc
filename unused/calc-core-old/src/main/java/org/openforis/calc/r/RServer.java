/**
 * 
 */
package org.openforis.calc.r;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.annotation.PostConstruct;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecuteResultHandler;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteWatchdog;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.rosuda.REngine.Rserve.RConnection;
import org.rosuda.REngine.Rserve.RserveException;

/**
 * @author M. Togna
 * 
 */
public class RServer {

	private static final Log LOG = LogFactory.getLog(RServer.class);

	private boolean running;
	private ExecutorService executorService;
	private String cmd = "R CMD Rserve --no-save --RS-conf";
	private DefaultExecuteResultHandler resultHandler = new DefaultExecuteResultHandler();
	private RConnectionProperies serverProperies;

	public RServer() {
		executorService = Executors.newSingleThreadExecutor();
		serverProperies = new RConnectionProperies();
		running = false;
	}

	private String getServerConfigFilePath() {
		try {
			InputStream is = getClass().getClassLoader().getResourceAsStream("org/openforis/calc/r/Rserve.conf");
			String tmpDir = System.getProperty("java.io.tmpdir");
			File tempFile = new File(tmpDir, "Rserve.conf");
			BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(tempFile));
			IOUtils.copy(is, out);
			is.close();
			out.close();
			return tempFile.getAbsolutePath();
		} catch ( Exception e ) {
			throw new RuntimeException("Error while creating server config file", e);
		}
	}

	synchronized public void shutdown() {
		try {
			RConnection connection = new RConnection(serverProperies.getUrl(), serverProperies.getPort());
			if ( connection.isConnected() ) {
				connection.serverShutdown();
			}
			connection.close();
		} catch ( RserveException e ) {
			if ( LOG.isInfoEnabled() ) {
				LOG.info("RServer cannot shutdown");
			}
		}
	}

	@PostConstruct
	synchronized public void start() {
		if ( !running ) {
			shutdown();

			StartRServerTask start = new StartRServerTask();
			try {
				executorService.submit(start).get();
//				Thread.sleep(1000);
			} catch ( InterruptedException e ) {
				throw new RuntimeException("Error in Thread.sleep", e);
			} catch ( ExecutionException e ) {
				throw new RuntimeException("Error while starting R Server", e);
			}
		}
	}

	public boolean isRunning() {
		return running;
	}

	@Override
	protected void finalize() throws Throwable {
		executorService.shutdownNow();
		shutdown();
	}

	private class StartRServerTask implements Callable<Void> {

		public Void call() throws Exception {
			CommandLine cmdLine = CommandLine.parse(cmd);
			String configFilePath = getServerConfigFilePath();
			cmdLine.addArgument(configFilePath);

			DefaultExecutor executor = new DefaultExecutor();
			ExecuteWatchdog watchDog = new ExecuteWatchdog(ExecuteWatchdog.INFINITE_TIMEOUT);
			executor.setWatchdog(watchDog);
			executor.execute(cmdLine, resultHandler);

			return null;
		}

	}
	
}
