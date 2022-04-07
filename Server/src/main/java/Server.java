import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;
import java.util.function.Consumer;

public class Server {
    int clientCount = 0;
    LinkedList<ClientThread> clientGuestList = new LinkedList<>();
    TheServer ourServer;
    private Consumer<Serializable> callback;
    int chosenPort;
    ClientThread clientRed = null;
    ClientThread clientBlue = null;
    boolean playersAssigned = false;

    Server(Consumer<Serializable> call, int givenPort) {
        callback = call;
        chosenPort = givenPort;
        ourServer = new TheServer();
        ourServer.start();
    }

    public class TheServer extends Thread {
        public void run() {
            try (ServerSocket servingSocket = new ServerSocket(chosenPort);) {
                System.out.println("Server is ready and waiting for clients on port "+ chosenPort +"!");
                //DEBUGGING:
                callback.accept("Server is now Open!");

                while (true) {
                    //System.out.println("About to attempt getting new client...");
                    ClientThread c = new ClientThread(servingSocket.accept());
                    //System.out.println("Successfully got client!!");
                    c.start();

                    if (!playersAssigned && hasTwoPlayers()) {
                        beginGame();
                    }
                }
            }
            catch (Exception e) {
                callback.accept("Server failed to launch :(");
                e.printStackTrace();
            }
        }
    }

    public class ClientThread extends Thread {
        private Socket clientConnection;
        private ObjectInputStream in;
        private ObjectOutputStream out;
        public boolean isRed = false;
        public boolean isBlue = false;
        public boolean isGuest = false;

        ClientThread(Socket s) {
            clientConnection = s;
        }

        public void run() {
            try {
                in = new ObjectInputStream(clientConnection.getInputStream());
                out = new ObjectOutputStream(clientConnection.getOutputStream());
                clientConnection.setTcpNoDelay(true);
                assignRole(this);
            }
            catch (Exception e) {
                System.out.println("Stream failed to open on new client :(");
            }

            while (true) {
                try {
                    String data = in.readObject().toString();
                    callback.accept(this.getRole() + "sent: " + data);
                }
                catch (Exception e) {
                    callback.accept(this.getRole() + " has disconnected!");
                    if (this.isRed) {
                        //red just disconnected, remove it and send blue back into the waiting queue
                        clientRed = null;
                        reassignPlayers();
                    }
                    else if (this.isBlue) {
                        //Blue just disconnected, remove it and send red back to waiting
                        clientBlue = null;
                        reassignPlayers();
                    }
                    else {
                        clientGuestList.remove(this); //Unlist this guest since they left
                    }
                    break;
                }
            }
        }
        
        public String getRole() {
            if (isRed)
                return "Red Player";
            if (isBlue)
                return "Blue Player";
            return "a Guest Observer";
        }

        public void reassignPlayers() {
            callback.accept("One of the main players left, looking for a replacement");
            System.out.println("A player left before game could be finished");
            playersAssigned = false;
            //Should check if new pairing can be made immediately
            if (clientRed == null && !clientGuestList.isEmpty()) {
                clientRed = clientGuestList.pop();
                clientRed.isRed = true;
                callback.accept("Reselected Red Player");
            }
            if (clientBlue == null && !clientGuestList.isEmpty()) {
                clientBlue = clientGuestList.pop();
                clientBlue.isBlue = true;
                callback.accept("Reselected Blue Player");
            }
            //0, 1, or 2 players have now been assigned.
            if (hasTwoPlayers()) {
                beginGame();
            }
        }
    }

    public boolean hasTwoPlayers() {
        if (clientRed != null && clientBlue != null) {
            return true;
        }
        return false;
    }

    public void beginGame() {
        try {
            
            LinkedList<ClientThread> fullClientList = clientGuestList;
            fullClientList.push(clientRed);
            fullClientList.push(clientBlue);
            for (ClientThread c : fullClientList) {
                c.out.writeObject("Players Assigned. The game can now begin!");
            }
            playersAssigned = true;
            System.out.println("Both players are connected, the game can now begin!");
        }
        catch (Exception e) {System.out.println("Uh oh.. Exception when trying to write to players...");}
    }

    private void assignRole(ClientThread c) {
        try {
            if (clientRed == null) {
                clientRed = c;
                clientRed.isRed = true;
                clientRed.out.writeObject("You have been assigned Player Red");
            }
            else if (clientBlue == null) {
                clientBlue = c;
                clientBlue.isBlue = true;
                clientBlue.out.writeObject("You have been assigned Player Blue");
            }
            else {
                c.isGuest = true;
                clientGuestList.push(c);
                c.out.writeObject("You have been assigned to the waiting list");
            }
            callback.accept("New client has connected! They have been designated: " + c.getRole() + "!");
        }
        catch (IOException e) {
            callback.accept("Uh oh. Couldn't write to thread's output...");
        }
    }
}
