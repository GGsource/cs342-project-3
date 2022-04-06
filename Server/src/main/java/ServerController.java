import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
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
    private VBox sceneTwoRoot;
    @FXML
    private VBox introSceneRoot;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        serverDialogueView = new ListView<>();
    }
    //set on action methods here
    public void openServerMethod(ActionEvent e) throws IOException {
        serverConnection = new Server(data -> {
            Platform.runLater(()-> {
                serverDialogueView.getItems().add(data.toString());
            });
        }, Integer.parseInt(portField.getText()));
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/FXML/ServerSceneTwo.fxml"));
        Parent sceneTwoRoot = loader.load();
        sceneTwoRoot.getStylesheets().add("/styles/ServerGameStyle.css");
        introSceneRoot.getScene().setRoot(sceneTwoRoot);
        System.out.println("Successfully changed to scene two!");

    }
}
