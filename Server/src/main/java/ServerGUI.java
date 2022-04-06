import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class ServerGUI extends Application {
	Server serverConnection;
	ListView<String> listItems;

	public static void main(String[] args) {
		launch(args);
	}

	//feel free to remove the starter code from this method
	@Override
	public void start(Stage primaryStage) throws Exception {
		Parent root = FXMLLoader.load(getClass()
					.getResource("/FXML/ServerIntroFXML.fxml"));
		primaryStage.setTitle("Welcome to the Morra Server!");
		
		//This ensures closing the window will close down the server
		primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
			@Override
            public void handle(WindowEvent t) {
                Platform.exit();
                System.exit(0);
            }
		});

		Scene serverIntroScene = new Scene(root, 300, 300);
		primaryStage.setScene(serverIntroScene);
		primaryStage.show();
	}

}
