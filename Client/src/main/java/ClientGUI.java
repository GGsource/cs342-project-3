import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class ClientGUI extends Application {
	ListView<String> listItems;

	public static void main(String[] args) {
		launch(args);
	}

	//feel free to remove the starter code from this method
	@Override
	public void start(Stage primaryStage) throws Exception {
		Parent clientIntroRoot = FXMLLoader.load(getClass()
					.getResource("/FXML/ClientIntroFXML.fxml"));
		primaryStage.setTitle("Welcome to Morra Client App!");
		primaryStage.getIcons().add(new Image("/images/icon-client.png"));
		
		//This ensures closing the window will close down the server
		primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
			@Override
            public void handle(WindowEvent t) {
                Platform.exit();
                System.exit(0);
            }
		});
		
				
		Scene clientIntroScene = new Scene(clientIntroRoot, 300,300);
		primaryStage.setScene(clientIntroScene);
		primaryStage.show();
	}

}
