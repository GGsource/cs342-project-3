import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;
import java.util.function.Consumer;
//TODO: Homestuck elevator music while players join?

public class Server {
    int clientCount = 0;
    LinkedList<ClientThread> clientGuestList = new LinkedList<>();
    TheServer ourServer;
    private Consumer<Serializable> callback;
    int chosenPort;
    ClientThread clientRed = null;
    ClientThread clientBlue = null;
    boolean isGameStarted = false;

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
                    if (this.isRed ) {
                        //red just disconnected, remove it and send blue back into the waiting queue
                        clientRed = null;
                        reassignPlayers(true);
                    }
                    else if (this.isBlue) {
                        //Blue just disconnected, remove it and send red back to waiting
                        clientBlue = null;
                        reassignPlayers(false);
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

        public void reassignPlayers(boolean wasRed) {
            if (wasRed) {
                callback.accept("Red Player left, looking for a replacement...");
            }
            else {
                callback.accept("Blue Player left, looking for a replacement...");
            }
            isGameStarted = false;
            //Should check if new pairing can be made immediately
            try {
                if (!clientGuestList.isEmpty()) {
                    ClientThread c = clientGuestList.remove();
                    if (clientRed == null) {
                        clientRed = c;
                        clientRed.isRed = true;
                        callback.accept("A new Red Player has been chosen!");
                        clientRed.out.writeObject("You have been chosen as the new Red Player");
                    }
                    else if (clientBlue == null) {
                        clientBlue = c;
                        clientBlue.isBlue = true;
                        callback.accept("A new Blue Player has been chosen!");
                        clientBlue.out.writeObject("You have been chosen as the new Blue Player");
                    }
                }
                else {
                    callback.accept("Not enough players for a new match, waiting for new players...");
                    if (clientRed != null) {
                        clientRed.out.writeObject("Not enough players for a new match, please wait for an opponent!");
                    }
                    else if (clientBlue != null) {
                        clientBlue.out.writeObject("Not enough players for a new match, please wait for an opponent!");
                    }
                }
            }
            catch (IOException e) {
                System.out.println("Uh oh... failied to send message to new players...");
                e.printStackTrace();
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
            isGameStarted = true;
            // LinkedList<ClientThread> fullClientList = clientGuestList;
            if (clientGuestList.contains(clientRed)) {callback.accept("Red was in the client list already?");}
            if (clientGuestList.contains(clientBlue)) {callback.accept("Blue was in the client list already?");}
            // fullClientList.push(clientRed);
            // fullClientList.push(clientBlue);
            clientRed.out.writeObject("New Game started! Good luck " + clientRed.getRole());
            clientBlue.out.writeObject("New Game started! Good luck " + clientBlue.getRole());
            for (ClientThread waiter : clientGuestList) {
                waiter.out.writeObject("A round has started, you are an observer until its your turn");
            }
            callback.accept("Red and Blue are ready to begin!");
            System.out.println("Both players are connected, the game can now begin!");
        }
        catch (Exception e) {
            System.out.println("Uh oh.. Exception when trying to write to players...");
            e.printStackTrace();
        }
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
                clientGuestList.add(c);
                c.out.writeObject("You have been assigned to the waiting list");
            }
            callback.accept("New client has connected! They have been designated: " + c.getRole() + "!");
            if (hasTwoPlayers()) {
                if (!isGameStarted) {
                    beginGame();
                }
            }
            else {
                c.out.writeObject("You are currently the only player, please wait for an opponent!");
            }
        }
        catch (IOException e) {
            callback.accept("Uh oh. Couldn't write to thread's output...");
        }
    }
}
