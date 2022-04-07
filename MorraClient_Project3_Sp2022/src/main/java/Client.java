import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.Socket;
import java.util.function.Consumer;

import javafx.application.Platform;

public class Client extends Thread{
    Socket clientSocket;
    ObjectOutputStream out;
    ObjectInputStream in;
    private Consumer<Serializable> callback;
    String chosenAddress;
    int chosenPort;

    Client(Consumer<Serializable> call,String givenAddress, int givenPort) {
        callback = call;
        chosenAddress = givenAddress;
        chosenPort = givenPort;
    }

    public void run() {
        try {
            clientSocket = new Socket(chosenAddress, chosenPort);
            out = new ObjectOutputStream(clientSocket.getOutputStream());
            in = new ObjectInputStream(clientSocket.getInputStream());
            clientSocket.setTcpNoDelay(true);
        }
        catch (Exception e) {
            System.out.println("Failed to create new socket for client 😔");
        }
        while (true) {
            try {
                String message = in.readObject().toString();
                callback.accept(message);
            }
            catch (Exception e) {
                System.out.println("I (client) have disconnected.");
                Platform.exit();
                System.exit(0);
                break;
            }
        }
    }
}
