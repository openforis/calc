package org.openforis.calc.controlpanel;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.file.Path;
import java.util.ResourceBundle;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.stage.Window;

import com.sun.deploy.uitoolkit.impl.fx.HostServicesFactory;
import com.sun.javafx.application.HostServicesDelegate;
/**
 * Controller class for the control panel
 * 
 * @author Mino Togna
 *
 */
public class CalcControlPanelController implements Initializable {
	
	private ScheduledExecutorService executorService;
	private Server server;
	private Integer linesRead;
	
	// ui elements
	@FXML
	private Button startBtn;
	@FXML
	private Button stopBtn;
	@FXML
	private Button logBtn;
	@FXML
	public TextArea console;
	@FXML
	private Pane applicationPane;
	
	private boolean logOpened = false;
	private double windowHeight;
	
	@Override
	public void initialize( URL url , ResourceBundle resourceBundle ) {
		this.windowHeight = 500;
		this.server = new Server();
//		this.server.clearLog();
		
		executorService = Executors.newScheduledThreadPool( 5 );
		
		// logger thread
		this.linesRead = 0;
		executorService.scheduleWithFixedDelay( new Logging( this ), 1, 1, TimeUnit.SECONDS);
	}
	
	@FXML
	public void startServer( MouseEvent event ) throws IOException {
		startBtn.setDisable( true );
		server.start( console );
		
		// enable stop button after 5 seconds
		executorService.schedule( new Runnable() {
			@Override
			public void run() {
						try {
							Thread.sleep( 5000 );
						} catch ( InterruptedException e ) {
						}
						Platform.runLater( new Runnable() {
							@Override
							public void run() {
								stopBtn.setDisable( false );
							}
						});
				
			}
		}, 0, TimeUnit.SECONDS );
		
	}
	
	@FXML
	public void stopServer( MouseEvent event ) throws IOException {
		stopBtn.setDisable( true );				
		server.stop();
		// enable start button once server is down
		
		executorService.schedule( new Runnable() {
			@Override
			public void run() {
				
						while( server.isRunning() ){
							try {
								Thread.sleep( 1000 );
							} catch ( InterruptedException e ) {
							}
						}
						Platform.runLater( new Runnable() {
							@Override
							public void run() {
								startBtn.setDisable( false );
							}
						});
				
			}
		}, 0, TimeUnit.SECONDS );
		
	}
	
	@FXML
	public void toggleLog( MouseEvent event ) {
		Window window = applicationPane.getScene().getWindow();
		
		if( this.logOpened ){
			window.setHeight( 150 );
			this.logOpened = false;
		} else {
			window.setHeight( windowHeight );
			this.logOpened = true;
		}
	}

	void shutdown() throws IOException {
		this.stopServer(null);
		this.executorService.shutdownNow();
	}
	
	void openBrowser( Application application , final long delay ) {
		
		final HostServicesDelegate hostServices = HostServicesFactory.getInstance( application );
		executorService.submit( new Runnable() {
			@Override
			public void run() {
					try {
						Thread.sleep( delay );
						
						String url = "http://127.0.0.1:8081/calc";
						hostServices.showDocument( url);
		
					} catch ( Exception e ) {
						e.printStackTrace();
					}
				}
			});
	}
	
	private class Logging implements Runnable {

		private File logFile;
		private TextArea console;

		public Logging( final CalcControlPanelController controller ){
			this.console = controller.console;
			
			Path log = controller.server.getLog();
			this.logFile = log.toFile();
		}

		@Override
		public void run() {
			
			Platform.runLater(new Runnable() {
				public void run() {
					try {
						FileInputStream inputStream = new FileInputStream(logFile);
						@SuppressWarnings( "resource" )
						BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
						for ( int i = 0 ; i < linesRead ; i++ ) {
							br.readLine();
						}

						String line = null;

						while ( (line = br.readLine()) != null ) {
							console.appendText(line);
							console.appendText("\n");

							linesRead++;
						}
					} catch ( Exception e ) {
						e.printStackTrace();
					}

				}
			});

		}
	}
	
}
