import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

public class ServerController implements Initializable {
    Server serverConnection;

    @FXML
    private TextField portField;
    @FXML
    private ListView<String> serverDialogueView;
    @FXML
    private VBox serverIntroRoot;
    @FXML
    private Label connectedPlayersLabel;
    @FXML
    private Label connectedGuestsLabel;
    //TODO: ^Figure out how to use these to update when someone joins
    //Manually trigger event?


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        //No need to initialize anything :]
    }

    //setOnAction methods go here
    public void openServerMethod(ActionEvent e) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/FXML/ServerGame.fxml"));
        Parent serverGameRoot = loader.load();
        ServerController newController = loader.getController();
        
        serverConnection = new Server(data -> {
            Platform.runLater(()-> {
                newController.serverDialogueView.getItems().add(data.toString());
            });
        }, Integer.parseInt(portField.getText()));

        serverGameRoot.getStylesheets().add("/styles/ServerGameStyle.css");
        serverIntroRoot.getScene().setRoot(serverGameRoot);
    }
}
