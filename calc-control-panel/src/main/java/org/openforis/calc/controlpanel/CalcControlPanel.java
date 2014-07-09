package org.openforis.calc.controlpanel;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.stage.Window;

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
	public void start( Stage stage ) throws Exception {
		stage.setTitle( "OpenForis Calc - Control Panel" );
		stage.setResizable( false );
		
		FXMLLoader fxmlLoader = new FXMLLoader();
		Pane pane = (Pane) fxmlLoader.load( getClass().getResource( "calc_control_panel.fxml" ).openStream() );
		
		controller = fxmlLoader.getController();
		controller.startServer( null );
			
		Scene scene = new Scene( pane );
		stage.setScene( scene );
		Window window = scene.getWindow();
		window.setHeight( 150 );
		stage.show();
		
		controller.openBrowser( this , 3000 );
	}

	@Override
	public void stop() throws Exception {
		controller.shutdown();
		
		super.stop();
	}
	
	
}
