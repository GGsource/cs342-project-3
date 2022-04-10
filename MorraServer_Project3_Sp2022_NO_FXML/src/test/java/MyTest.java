import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javafx.application.Platform;

class MyTest {
	Server testServer;
	Client testClient;

	@BeforeEach
	void start() {
		testServer = new Server(data -> {
			Platform.runLater(()-> {
				String incomingMessage = data.toString();
				if (incomingMessage.startsWith("*")) {
					//Starts with * so its an update on the user counts!
					String[] users = incomingMessage.split("\\*");
					// connectedPlayersLabel.setText("Players connected: " + users[1]);
					// connectedGuestsLabel.setText("Guests connected: " + users[2]);
					// if (Integer.parseInt(users[1]) > 1)
					// 	connectedBox.setStyle("-fx-background-color: forestgreen;");
					// else
					// 	connectedBox.setStyle("-fx-background-color: darkred;");
				} else if(incomingMessage.startsWith("?")) {
					String[] scores = incomingMessage.split("\\?");
					// redPointsLabel.setText("Red's Score: " + scores[1]);
					// bluePointsLabel.setText("Blue's Score: " + scores[2]);
				} else {
					//Just a normal message being displayed in server
					// serverDialogueView.getItems().add(data.toString());
				}
			});
		}, 5555);

		testClient = new Client(data->{
			Platform.runLater(()->{
				String incomingMessage = data.toString();
				if (incomingMessage.startsWith("%")) {
					//Starts with % so we're being told to reenable buttons
					// playButtonsBox.setDisable(false);
					// confirmButton.setDisable(false);
					// guessField.setDisable(false);
				} else if (incomingMessage.startsWith("#")) {
					//Starts with # so we're being told to reenable play btns
					// playButtonsBox.setDisable(false);
				} else if (incomingMessage.startsWith("@")) {
					//Starts with @ so we're being told to reenable newgamebutton
					// newGameButton.setDisable(false);
				} else if (incomingMessage.startsWith("^")) {
					//Starts with ^ so we're being told what the opponent picked
					// String [] inString = incomingMessage.split("\\^");
					// for (String s : inString) {
					// 	System.out.println("inString had: " + s);
					// }
					// //Now set the graphic to instring[1]
					// opponentView.setImage(new Image("/images/" + inString[1]+".png"));
				} else {
					// clientDialogueView.getItems().add(incomingMessage);
				}
			});
		}, "127.0.0.1", 5555);
	}

	@Test
	void successfulConnectionTest() {
		fail("Not yet implemented");
	}
	

}
