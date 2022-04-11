import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class ServerGUI extends Application {
	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		Parent root = FXMLLoader.load(getClass().getResource("/FXML/ServerIntro.fxml")); //Load in proper FXML file
		primaryStage.setTitle("Welcome to the Morra Server!"); //Window title
		primaryStage.getIcons().add(new Image("/images/icon-server.png")); //Custom Icon!
		
		//This ensures closing the window will close down the server
		primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
			@Override
            public void handle(WindowEvent t) {
                Platform.exit();
                System.exit(0);
            }
		});

		Scene serverIntroScene = new Scene(root, 400, 400);
		primaryStage.setScene(serverIntroScene);
		primaryStage.show();
	}
}