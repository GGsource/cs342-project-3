import java.io.IOException;
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
    MorraInfo localInfo;

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
                //Something has been sent, receive it
                MorraInfo incomingInfo = (MorraInfo)in.readObject();
                if (incomingInfo.isCarrierPigeon) { //A msg was sent!
                    callback.accept(incomingInfo.msg);
                }
                else { //It wasn't a msg, it's the game's state
                    localInfo = incomingInfo;
                }
            }
            catch (Exception e) {
                System.out.println("I (client) have disconnected.");
                Platform.exit();
                System.exit(0);
                break;
            }
        }
    }

    public void send(MorraInfo outgoingInfo) {
		
		try {
			out.writeObject(outgoingInfo);
		} catch (IOException e) {
            System.out.println("Uh oh. Couldn't send info as client.");
			e.printStackTrace();
		}
	}
}
