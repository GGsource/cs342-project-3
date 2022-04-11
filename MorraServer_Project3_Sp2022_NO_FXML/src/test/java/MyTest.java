import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javafx.application.Platform;

class MyTest {
	ArrayList<String> messageList;

	@BeforeEach
	void start() {
		messageList = new ArrayList<>();
	}
	private Server createServer() {
		return new Server(data -> {
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
	}
	private Client createClient () {
		return new Client(data->{
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
	void clientWithNoServerTest() {
		//An exception is thrown if we attempt to connect to a non-existant server
		Client testClient = createClient();
		assertThrows(Exception.class, ()->testClient.out.writeObject(""), "Exception wasn't thrown, despite there being no server to connect to!");
	}
	@Test
	void serverWithNoClientsTest() {
		Server testServer = createServer();
		assertNull(testServer.clientRed, "Uh oh, Red Client isn't null despite no one connecting...");
		assertNull(testServer.clientBlue, "Uh oh, Blue Client isn't null despite no one connecting...");
	}
	@Test
	void serverWithOneClientTest() {
		Server testServer = createServer();
		Client testClient = createClient();
		assertNotNull(testServer.clientRed, "Uh oh, Red Client is null despite a client being connected...");
	}
	

}
