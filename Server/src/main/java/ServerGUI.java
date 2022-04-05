import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;

import javafx.stage.Stage;

public class ServerGUI extends Application {

	public static void main(String[] args) {
		launch(args);
	}

	//feel free to remove the starter code from this method
	@Override
	public void start(Stage primaryStage) throws Exception {
		Parent root = FXMLLoader.load(getClass()
					.getResource("/FXML/ServerFXML.fxml"));
		primaryStage.setTitle("Welcome to Server App!");
		
		
				
		Scene serverIntroScene = new Scene(root, 300, 300);
		primaryStage.setScene(serverIntroScene);
		primaryStage.show();
	}

}
