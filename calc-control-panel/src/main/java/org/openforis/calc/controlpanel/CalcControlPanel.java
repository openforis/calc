package org.openforis.calc.controlpanel;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

/**
 * @author Mino Togna
 *
 */
public class CalcControlPanel extends Application {

	private CalcControlPanelController controller;
	
	public static void main( String[] args ) {
		launch(args);
	}

	@Override
	public void start( Stage primaryStage ) throws Exception {
		primaryStage.setTitle( "OpenForis Calc - Control Panel" );
		
		FXMLLoader fxmlLoader = new FXMLLoader();
		Pane pane = (Pane) fxmlLoader.load( getClass().getResource( "calc_control_panel.fxml" ).openStream() );

		controller = fxmlLoader.getController();
		controller.startServer( null );
			
		Scene myScene = new Scene( pane );
		primaryStage.setScene( myScene );
		primaryStage.show();
		
		controller.openBrowser( this , 3000 );
	}

	@Override
	public void stop() throws Exception {
		controller.shutdown();
		
		super.stop();
	}
	
	
}
