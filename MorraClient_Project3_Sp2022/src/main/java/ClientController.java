import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class ClientController implements Initializable{
    Client clientConnection;

    @FXML
    private TextField portField;
    @FXML
    private TextField addressField;
    @FXML
    private ListView<String> clientDialogueView;
    @FXML
    private VBox clientIntroRoot;
    @FXML
    private VBox clientGameRoot;
    @FXML
    private HBox buttonBox;
    @FXML
    private TextField guessField;
    @FXML
    private Button confirmButton;


    @Override
    public void initialize(URL location, ResourceBundle resources) {
    }

    public void connectMethod(ActionEvent e) throws IOException{
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/FXML/ClientGameScene.fxml"));
        Parent clientGameRoot = loader.load();
        ClientController newController = loader.getController();
        
        clientConnection = new Client(data->{Platform.runLater(()->{newController.clientDialogueView.getItems().add(data.toString());});}, addressField.getText(), Integer.parseInt(portField.getText()));
        
        newController.buttonBox.setDisable(true);
        newController.guessField.setDisable(true);
        newController.confirmButton.setDisable(true);

        clientIntroRoot.getScene().setRoot(clientGameRoot);
        System.out.println("Successfully changed to client game scene!");
        clientConnection.start();
    }
    
}
