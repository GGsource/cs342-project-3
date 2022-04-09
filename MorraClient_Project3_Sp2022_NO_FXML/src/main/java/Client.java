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
                if (incomingInfo == null)
                {
                    System.out.println("incoming info is null D:");
                }
                else {
                    System.out.println("incoming info is not null.");
                    if (incomingInfo.isResetPigeon) {
                        System.out.println("The received info is indeed a reset pigeon!");
                    }
                }
                if (incomingInfo.isMessagePigeon) { //A msg was sent!
                    System.out.println("Incoming info is a message pigeon!!");
                    callback.accept(incomingInfo.msg);
                }
                else if(incomingInfo.isResetPigeon) { //We're being told about the game's state!
                    //We've been told to reset for a new game!
                    System.out.println("Incoming info is a reset pigeon!!");
                    localInfo.reset();
                }
                else { //It wasn't a msg, it's the game's state
                System.out.println("Incoming info is a normal game state info");
                    localInfo = incomingInfo;
                }
            }
            catch (Exception e) {
                System.out.println("I (client) have disconnected.");
                e.printStackTrace();
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
