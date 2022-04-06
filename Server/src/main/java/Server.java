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

                while (true) {
                    ClientThread c = new ClientThread(servingSocket.accept());
                    if (clientRed == null) {
                        clientRed = c;
                        clientRed.isRed = true;
                    }
                    else if (clientBlue == null) {
                        clientBlue = c;
                        clientBlue.isBlue = true;
                    }
                    else {
                        c.isGuest = true;
                        clientGuestList.add(c);
                    }
                    callback.accept("New client has connected! They have been designated: " + c.getColor() + "!");
                    c.start();

                    if (hasTwoPlayers()) {
                        beginGame();
                    }
                }
            }
            catch (Exception e) {
                callback.accept("Server failed to launch :(");
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
            }
            catch (Exception e) {
                System.out.println("Stream failed to open on new client :(");
            }

            while (true) {
                try {
                    String data = in.readObject().toString();
                    callback.accept(this.getColor() + "sent: " + data);
                }
                catch (Exception e) {
                    callback.accept(this.getColor() + "has disconnected!");
                    if (this.isRed) {
                        //red just disconnected, remove it and send blue back into the waiting queue
                        clientRed = null;
                        if (clientBlue != null) {
                            clientGuestList.push(clientBlue);
                            clientBlue = null;
                        }
                        interruptGame();
                    }
                    else if (this.isBlue) {
                        //Blue just disconnected, remove it and send red back to waiting
                        clientBlue = null;
                        if (clientRed != null) {
                            clientGuestList.push(clientRed);
                            clientRed = null;
                        }
                        interruptGame();
                    }
                    else {
                        clientGuestList.remove(this); //Unlist this guest since they left
                    }

                }
            }
        }
        
        public String getColor() {
            if (isRed)
                return "Red Player";
            if (isBlue)
                return "Blue Player";
            return "a Guest Observer";
        }

        public void interruptGame() {
            System.out.println("A player left before game could be finished.");
            //Should check if new pairing can be made immediately
            if (!clientGuestList.isEmpty()) {
                clientRed = clientGuestList.pop();
                clientRed.isRed = true;
            }
            if (!clientGuestList.isEmpty()) {
                clientBlue = clientGuestList.pop();
                clientBlue.isBlue = true;
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
            clientRed.out.writeObject("You have been assigned Player Red");
            clientBlue.out.writeObject("You have been assigned Player Blue");
        }
        catch (Exception e) {System.out.println("Uh oh.. Exception when trying to write to players...");}
    }
}
