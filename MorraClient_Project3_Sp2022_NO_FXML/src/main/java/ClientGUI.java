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

public class ClientGUI extends Application {
	ListView<String> listItems;
	Client clientConnection;
	int selectedGuess;
	int selectedPlay;

	public static void main(String[] args) {
		launch(args);
	}

	//feel free to remove the starter code from this method
	@Override
	public void start(Stage primaryStage) throws Exception {
		HashMap<String, Scene> sceneMap = createClientScenes(primaryStage);
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
		
		primaryStage.setScene(sceneMap.get("clientIntroScene"));
		primaryStage.show();
	}

	private HashMap<String, Scene> createClientScenes(Stage givenStage) {
		HashMap<String, Scene> sceneMap = new HashMap<>();

		//Client Intro Scene
		Label portLabel = new Label("Please enter the server's port:");
		TextField portField = new TextField("5555");
		Label addressLabel = new Label("Please enter the server's address");
		TextField addressField = new TextField("127.0.0.1");
		VBox serverDetailsBox = new VBox(portLabel, portField, addressLabel, addressField);
		serverDetailsBox.setAlignment(Pos.CENTER);
		Button connectButton = new Button("Connect");
		VBox connectBox = new VBox(connectButton);
		connectBox.setAlignment(Pos.CENTER);
		VBox clientIntroBox = new VBox(serverDetailsBox, connectBox);
		clientIntroBox.setAlignment(Pos.CENTER);
		sceneMap.put("clientIntroScene", new Scene(clientIntroBox, 300, 300));

		//Client Game Scene
		Label welcomeLabel = new Label("Welcome to the Morra Client!");
		ListView<String> clientDialogueView = new ListView<>();
		VBox topBox = new VBox(welcomeLabel, clientDialogueView);
		topBox.setAlignment(Pos.CENTER);
		Label playLabel = new Label("Select your Play:");
		HBox playButtonsBox = new HBox();
		playButtonsBox.setAlignment(Pos.CENTER);
		Button newGameButton = new Button("New Game");
		Button quitGameButton = new Button("Quit Game");
		HBox quitOrNewBox = new HBox(newGameButton, quitGameButton);
		Label guessLabel = new Label("Insert your guess: ");
		TextField guessField = new TextField("4");
		Button confirmButton = new Button("Confirm Selections");
		for (int i = 0; i <= 6; i++) {
			Button playButton = new Button(""+i);
			playButton.setOnAction(e->{
				selectedPlay = Integer.parseInt(playButton.getText());
				guessField.setDisable(false);
				confirmButton.setDisable(false);
				quitOrNewBox.setDisable(false);
				//DEBUGGING:
				System.out.println("Your play is: " + selectedPlay);
			});
			playButtonsBox.getChildren().add(playButton);
		}
		VBox bottomBox = new VBox(playLabel, playButtonsBox, guessLabel, guessField, confirmButton);
		bottomBox.setAlignment(Pos.CENTER);
		quitOrNewBox.setAlignment(Pos.CENTER);
		VBox clientGameBox = new VBox(topBox, bottomBox, quitOrNewBox);
		clientGameBox.setAlignment(Pos.CENTER);
		sceneMap.put("clientGameScene", new Scene(clientGameBox, 350, 750));

		//Connect Button On Action Method
		connectButton.setOnAction(e->{
			clientConnection = new Client(data->{
				Platform.runLater(()->{
					//TODO: If string begins with ! then reenable buttons
					clientDialogueView.getItems().add(data.toString());
				});
			}, addressField.getText(), Integer.parseInt(portField.getText()));
			
			//buttonBox.setDisable(true);
			guessField.setDisable(true);
			confirmButton.setDisable(true);
			quitOrNewBox.setDisable(true);
			givenStage.setScene(sceneMap.get("clientGameScene"));
			//DEBUGGING:
			System.out.println("Successfully changed to client game scene!");
			clientConnection.start();
		});

		//Confirm Button On Action Method
		confirmButton.setOnAction(e->{
			selectedGuess = Integer.parseInt(guessField.getText());
			clientConnection.localInfo.makeMove(selectedPlay, clientConnection.localInfo.isPlayerRed);
			clientConnection.localInfo.makeGuess(selectedGuess, clientConnection.localInfo.isPlayerRed);
			MorraInfo outgoingInfo = new MorraInfo(clientConnection.localInfo);
			System.out.println("You decided to play: "+ outgoingInfo.getLastPlay());
			System.out.println("You decided to guess:"+ outgoingInfo.getLastGuess());
			System.out.println(outgoingInfo.getPlaysList().size() + " plays so far:   " + outgoingInfo.getPlaysList());
			System.out.println(outgoingInfo.getGuessList().size() + " guesses so far: " + outgoingInfo.getGuessList());
			clientConnection.send(outgoingInfo);//Send its modified copy
			//Now server should have updated itself with the changes
			//TODO: Once you confirm, disable plays buttons and guessfield
			// confirmButton.setDisable(true);
			// guessField.setDisable(true);
			// playButtonsBox.setDisable(true);
		});

		newGameButton.setOnAction(e->{
			//We need to clear the scores
			//TODO: get to server's cleargamestate()
		});
		quitGameButton.setOnAction(e->{
			givenStage.close();
			//FIXME: DOESNT TRIGGER DISCONNECT DIALOGUE
		});

		return sceneMap;
	}

}
