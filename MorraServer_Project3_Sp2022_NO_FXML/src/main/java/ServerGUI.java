import java.util.HashMap;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class ServerGUI extends Application {
	Server serverConnection;
	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		HashMap<String, Scene> serverSceneMap = createServerScenes(primaryStage);
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

		primaryStage.setScene(serverSceneMap.get("serverIntroScene"));
		primaryStage.show();
	}

	private HashMap<String, Scene> createServerScenes(Stage primaryStage) {
		HashMap<String, Scene> sceneMap = new HashMap<>();

		//Server Intro Scene
		Label portLabel = new Label("Please enter your server port:");
		portLabel.getStyleClass().add("label");
		TextField portField = new TextField("5555");
		VBox portBox = new VBox(portLabel, portField);
		portBox.setAlignment(Pos.CENTER);
		Button openServerButton = new Button("Open Server");
		VBox openServerBox = new VBox(openServerButton);
		openServerBox.setAlignment(Pos.CENTER);
		VBox serverIntroBox = new VBox(portBox, openServerBox);
		serverIntroBox.setAlignment(Pos.CENTER);
		sceneMap.put("serverIntroScene", new Scene(serverIntroBox, 500, 600));
		
		//Server Game Scene
		Label serverGameLabel = new Label("Welcome to the Morra Server");
		serverGameLabel.getStyleClass().add("label");
		Label connectedPlayersLabel = new Label("Players connected: 0");
		connectedPlayersLabel.getStyleClass().add("label");
		Label connectedGuestsLabel = new Label("Guests connected: 0");
		connectedGuestsLabel.getStyleClass().add("label");
		HBox connectedBox = new HBox(connectedPlayersLabel, connectedGuestsLabel);
		connectedBox.getStyleClass().add("HBox");
		connectedBox.setAlignment(Pos.CENTER);
		ListView<String> serverDialogueView = new ListView<>();
		Label redPointsLabel = new Label("Red's Score: 0");
		redPointsLabel.getStyleClass().add("points");
		redPointsLabel.setStyle("-fx-background-color: red;");
		Label bluePointsLabel = new Label("Blue's Score: 0");
		bluePointsLabel.getStyleClass().add("points");
		bluePointsLabel.setStyle("-fx-background-color: blue;");
		HBox pointsBox = new HBox(redPointsLabel, bluePointsLabel);
		pointsBox.getStyleClass().add("HBox");
		pointsBox.setAlignment(Pos.CENTER);
		VBox serverGameBox = new VBox(serverGameLabel, connectedBox, serverDialogueView, pointsBox);
		serverGameBox.getStyleClass().add("VBox");
		serverGameBox.setAlignment(Pos.CENTER);
		Scene serverGameScene = new Scene(serverGameBox, 500, 600);
		serverGameScene.getStylesheets().add("/styles/ServerGameStyle.css");
		sceneMap.put("serverGameScene", serverGameScene);

		openServerButton.setOnAction(e->{
			serverConnection = new Server(data -> {
				Platform.runLater(()-> {
					String incomingMessage = data.toString();
					if (incomingMessage.startsWith("*")) {
						//Starts with * so its an update on the user counts!
						String[] users = incomingMessage.split("\\*");
						connectedPlayersLabel.setText("Players connected: " + users[1]);
						connectedGuestsLabel.setText("Guests connected: " + users[2]);
						if (Integer.parseInt(users[1]) > 1)
							connectedBox.setStyle("-fx-background-color: forestgreen;");
						else
							connectedBox.setStyle("-fx-background-color: darkred;");
					} else if(incomingMessage.startsWith("?")) {
						String[] scores = incomingMessage.split("\\?");
						redPointsLabel.setText("Red's Score: " + scores[1]);
						bluePointsLabel.setText("Blue's Score: " + scores[2]);
					} else {
						serverDialogueView.getItems().add(data.toString());
					}
				});
			}, Integer.parseInt(portField.getText()));

			primaryStage.setScene(sceneMap.get("serverGameScene"));
		});

		return sceneMap;
	}
}