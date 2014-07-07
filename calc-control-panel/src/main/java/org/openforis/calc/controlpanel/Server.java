package org.openforis.calc.controlpanel;
import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;

import javafx.scene.control.TextArea;

/**
 * @author Mino Togna
 *
 */
public class Server {
	private static final String OS_NAME_WINDOWS_PREFIX = "Windows";
	
	private Path applicationPath;
	private Path startup;
	private Path shutdown;
	private Path log;

	public Server() {
		
		String calcHome = System.getenv( "CALC_HOME" );
		
		if( calcHome == null ){
			throw new IllegalStateException( "Cannot find calc_home environment variable" );
		}
		applicationPath = Paths.get( calcHome ).toAbsolutePath();
		
		String fileExtension = isOsWindows() ? "cmd" : "sh";

		startup = applicationPath.resolve( "calc-server/startup." + fileExtension );
		shutdown = applicationPath.resolve( "calc-server/shutdown." + fileExtension );
//		/catalina.2014-06-20.log
		String dateFormat = new SimpleDateFormat( "yyyy-MM-dd" ).format( new Date() );
		log = applicationPath.resolve( "calc-server/tomcat/logs/catalina."+dateFormat+".log" );
	}

	public Path getLog() {
		return log;
	}
	
	public void start( final TextArea textArea  ) throws IOException {
		if( !isRunning() ) {
			exec( startup.toString() );			
		}
	}
	
	public void stop() throws IOException {
		if( isRunning() ){
			exec( shutdown.toString() );
		}
	}
	
	private void exec( String command ) throws IOException {
		Runtime.getRuntime().exec( command );
	}
	
	boolean isOsWindows() {
		String osName = System.getProperty( "os.name" );
		return osName.startsWith( OS_NAME_WINDOWS_PREFIX );
	}
	
	boolean isRunning() {
		boolean running = false;
		
		try {
			@SuppressWarnings( { "unused", "resource" } )
			Socket socket = new Socket( "127.0.0.1" , 8081 );
			running = true;
		} catch ( Exception e ) {
			running = false;
		}
		
		return running;
	}
	
	void clearLog() {
		File file = this.log.toFile();
		file.delete();
	}
	
}
