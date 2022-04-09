import java.io.IOException;
import java.util.HashMap;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class ClientGUI extends Application {
	ListView<String> listItems;
	Client clientConnection;
	int selectedGuess;
	int selectedPlay;
	boolean redWantsReplay = false;
	boolean blueWantsReplay = false;


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
		//topbar
		MenuItem statsItem = new MenuItem("Bug Facts");
		Menu statsMenu = new Menu("Cool Stats");
		statsMenu.getItems().add(statsItem);
		MenuBar topBar = new MenuBar(statsMenu);
		VBox barBox = new VBox(topBar);
		statsItem.setOnAction(e->{givenStage.setScene(sceneMap.get("clientStatsScene"));});
		//The rest
		Label portLabel = new Label("Please enter the server's port:");
		TextField portField = new TextField("5555");
		Label addressLabel = new Label("Please enter the server's address");
		TextField addressField = new TextField("127.0.0.1");
		VBox serverDetailsBox = new VBox(portLabel, portField, addressLabel, addressField);
		serverDetailsBox.setAlignment(Pos.CENTER);
		Button connectButton = new Button("Connect");
		VBox connectBox = new VBox(connectButton);
		connectBox.setAlignment(Pos.CENTER);
		VBox clientIntroBox = new VBox(barBox, serverDetailsBox, connectBox);
		clientIntroBox.setSpacing(30);
		sceneMap.put("clientIntroScene", new Scene(clientIntroBox, 300, 300));

		//Client Game Scene
		Label welcomeLabel = new Label("Welcome to the Morra Client!");
		ListView<String> clientDialogueView = new ListView<>();
		VBox topBox = new VBox(welcomeLabel, clientDialogueView);
		topBox.setAlignment(Pos.CENTER);
		Label opponentLabel = new Label("Opponent's Play:");
		ImageView opponentView = new ImageView(new Image("/images/q.png"));
		VBox opponentBox = new VBox(opponentLabel, opponentView);
		opponentBox.setAlignment(Pos.CENTER);
		Label playLabel = new Label("Select your Play:");
		HBox playButtonsBox = new HBox();
		playButtonsBox.setAlignment(Pos.CENTER);
		Button newGameButton = new Button("New Game");
		Button quitGameButton = new Button("Quit Game");
		HBox quitOrNewBox = new HBox(newGameButton, quitGameButton);
		Label guessLabel = new Label("Insert your guess: ");
		TextField guessField = new TextField("4");
		guessField.setMaxWidth(40);
		Button confirmButton = new Button("Confirm Selections");
		for (int i = 0; i < 6; i++) {
			Button playButton = new Button(""+i, new ImageView(new Image("/images/"+i+".png")));
			playButton.setOnAction(e->{
				selectedPlay = Integer.parseInt(playButton.getText());
				guessField.setDisable(false);
				confirmButton.setDisable(false);
				//DEBUGGING:
				System.out.println("Your play is: " + selectedPlay);
			});
			playButtonsBox.getChildren().add(playButton);
		}
		VBox bottomBox = new VBox(playLabel, playButtonsBox, guessLabel, guessField, confirmButton);
		bottomBox.setAlignment(Pos.CENTER);
		quitOrNewBox.setAlignment(Pos.CENTER);
		VBox clientGameBox = new VBox(topBox, opponentBox, bottomBox, quitOrNewBox);
		clientGameBox.setAlignment(Pos.CENTER);
		sceneMap.put("clientGameScene", new Scene(clientGameBox, 525, 500));

		//Bonus Stats Scene
		ImageView coolBugView = new ImageView(new Image("/images/coolbugfacts.png"));
		coolBugView.setPreserveRatio(true);
		coolBugView.setFitWidth(300);
		Label coolLabel = new Label("Cool Bug Fact:");
		Label coolOptimalLabel = new Label("In terms of the optimal guesses, you should only ever make a guess between 0 and 10! Anything else is functionally impossible in this game.");
		Label coolKnowledgeLabel = new Label("With that in mind, You will always know your own choice, so your choices can be cut down further. The sum can be anywhere from 0 to 5 integers above your own.");
		Label coolStatsLabel = new Label("Thus we arrive at a correct guess chance of 1 in 6. So for every guess, you have a 16.67% chance of being correct. Not bad!");
		coolOptimalLabel.setWrapText(true);
		coolOptimalLabel.setTextAlignment(TextAlignment.CENTER);
		coolKnowledgeLabel.setWrapText(true);
		coolKnowledgeLabel.setTextAlignment(TextAlignment.CENTER);
		coolStatsLabel.setWrapText(true);
		coolStatsLabel.setTextAlignment(TextAlignment.CENTER);
		Button returnButton = new Button("Return");
		VBox clientStatsBox = new VBox(coolBugView, coolLabel, coolOptimalLabel, coolKnowledgeLabel, coolStatsLabel, returnButton);
		clientStatsBox.setAlignment(Pos.CENTER);
		clientStatsBox.setSpacing(20);
		sceneMap.put("clientStatsScene", new Scene(clientStatsBox, 300, 600));
		returnButton.setOnAction(e->{givenStage.setScene(sceneMap.get("clientIntroScene"));});

		//Connect Button On Action Method
		connectButton.setOnAction(e->{
			redWantsReplay = false;
			blueWantsReplay = false;
			clientConnection = new Client(data->{
				Platform.runLater(()->{
					String incomingMessage = data.toString();
					if (incomingMessage.startsWith("%")) {
						//Starts with % so we're being told to reenable buttons
						playButtonsBox.setDisable(false);
						confirmButton.setDisable(false);
						guessField.setDisable(false);
					} else if (incomingMessage.startsWith("#")) {
						//Starts with # so we're being told to reenable play btns
						playButtonsBox.setDisable(false);
					} else if (incomingMessage.startsWith("@")) {
						//Starts with @ so we're being told to reenable newgamebutton
						newGameButton.setDisable(false);
					} else if (incomingMessage.startsWith("^")) {
						//Starts with ^ so we're being told what the opponent picked
						String [] inString = incomingMessage.split("\\^");
						for (String s : inString) {
							System.out.println("inString had: " + s);
						}
						//Now set the graphic to instring[1]
						opponentView.setImage(new Image("/images/" + inString[1]+".png"));
					} else {
						clientDialogueView.getItems().add(data.toString());
					}
				});
			}, addressField.getText(), Integer.parseInt(portField.getText()));
			playButtonsBox.setDisable(true);
			guessField.setDisable(true);
			confirmButton.setDisable(true);
			newGameButton.setDisable(true);
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
			confirmButton.setDisable(true);
			guessField.setDisable(true);
			playButtonsBox.setDisable(true);
			newGameButton.setDisable(true);
		});

		newGameButton.setOnAction(e->{
			// playButtonsBox.setDisable(false);
			// confirmButton.setDisable(false);
			// guessField.setDisable(false);
			//We need to clear the scores
			try {
				if (clientConnection.localInfo.isPlayerRed) {
					MorraInfo replayMsg = new MorraInfo("&");
					replayMsg.isPlayerRed = true;
					clientConnection.out.writeObject(replayMsg);
				}
				else {
					MorraInfo replayMsg = new MorraInfo("&");
					replayMsg.isPlayerRed = false;
					clientConnection.out.writeObject(replayMsg);
				}
			} catch (IOException exc) {
				System.out.println("Failed to notify others about desire to replay :/");
				exc.printStackTrace();
			}
		});
		quitGameButton.setOnAction(e->{
			Platform.exit();
			System.exit(0);
		});

		return sceneMap;
	}

}
