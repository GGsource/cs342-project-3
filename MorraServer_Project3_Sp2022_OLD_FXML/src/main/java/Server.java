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
    int playerCount = 0;
    int guestCount = 0;
    LinkedList<ClientThread> clientGuestList = new LinkedList<>();
    TheServer ourServer;
    private Consumer<Serializable> callback;
    int chosenPort;
    ClientThread clientRed = null;
    ClientThread clientBlue = null;
    boolean isGameStarted = false;
    MorraInfo serverInfo;
    boolean receivedRedChoice;
    boolean receivedBlueChoice;

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
                    MorraInfo receivedInfo = (MorraInfo)in.readObject();
                    //A player sent info, receive it
                    updateInfo(receivedInfo);
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
                        clientRed.out.writeObject(new MorraInfo("You have been chosen as the new Red Player"));
                    }
                    else if (clientBlue == null) {
                        clientBlue = c;
                        clientBlue.isBlue = true;
                        callback.accept("A new Blue Player has been chosen!");
                        clientBlue.out.writeObject(new MorraInfo("You have been chosen as the new Blue Player"));
                    }
                }
                else {
                    callback.accept("Not enough players for a new match, waiting for new players...");
                    if (clientRed != null) {
                        clientRed.out.writeObject(new MorraInfo("Not enough players for a new match, please wait for an opponent!"));
                    }
                    else if (clientBlue != null) {
                        clientBlue.out.writeObject(new MorraInfo("Not enough players for a new match, please wait for an opponent!"));
                    }
                }
            }
            catch (IOException e) {
                System.out.println("Uh oh... failied to send message to new players...");
                e.printStackTrace();
            }
            //0, 1, or 2 players have now been assigned.
            if (hasTwoPlayers()) {
                InitializeGame();
            }
        }
    }

    public boolean hasTwoPlayers() {
        if (clientRed != null && clientBlue != null) {
            return true;
        }
        return false;
    }

    public void InitializeGame() {
        try {
            isGameStarted = true;
            clientRed.out.writeObject(new MorraInfo("New Game started! Good luck " + clientRed.getRole()));
            clientBlue.out.writeObject(new MorraInfo("New Game started! Good luck " + clientBlue.getRole()));
            for (ClientThread waiter : clientGuestList) {
                waiter.out.writeObject(new MorraInfo("A round has started, you are an observer until its your turn"));
            }
            callback.accept("Red and Blue are ready to begin!");
            //System.out.println("Both players are connected, the game can now begin!");
            serverInfo = new MorraInfo();
            //Now send both players a copy of MoraInfo
            MorraInfo rInfo = serverInfo;
            rInfo.isPlayerRed = true;
            clientRed.out.writeObject(rInfo);//Need to let it know it is red
            clientBlue.out.writeObject(serverInfo);
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
                clientRed.out.writeObject(new MorraInfo("You have been assigned Player Red"));
            }
            else if (clientBlue == null) {
                clientBlue = c;
                clientBlue.isBlue = true;
                clientBlue.out.writeObject(new MorraInfo("You have been assigned Player Blue"));
            }
            else {
                c.isGuest = true;
                clientGuestList.add(c);
                c.out.writeObject(new MorraInfo("You have been assigned to the waiting list"));
            }
            callback.accept("New client has connected! They have been designated: " + c.getRole() + "!");
            if (hasTwoPlayers()) {
                if (!isGameStarted) {
                    InitializeGame();
                }
            }
            else {
                c.out.writeObject(new MorraInfo("You are currently the only player, please wait for an opponent!"));
            }
        }
        catch (IOException e) {
            callback.accept("Uh oh. Couldn't write to thread's output...");
        }
    }

    private void updateInfo(MorraInfo incomingInfo) {
        //Check who sent by seeing which arraylist was updated
        if (incomingInfo.playerRedPlays.size() > serverInfo.playerRedPlays.size()) {
            //Red plays increased so it was sent by red!
            serverInfo.playerRedPlays = incomingInfo.playerRedPlays;
            serverInfo.playerRedGuesses = incomingInfo.playerRedGuesses;
            receivedRedChoice = true;
        }
        else {
            //Red plays didn't increase so this was sent by blue!
            serverInfo.playerBluePlays = incomingInfo.playerBluePlays;
            serverInfo.playerBlueGuesses = incomingInfo.playerBlueGuesses;
            receivedBlueChoice = true;
        }
        //Now we have received the choices of one of the players, determine
        //if both have sent in their answers and if so pick winner
        if (receivedRedChoice && receivedBlueChoice) {
            //Both have given their guesses
            int correctTotal = serverInfo.playerRedPlays.get(serverInfo.playerRedPlays.size()-1) + serverInfo.playerBluePlays.get(serverInfo.playerBluePlays.size()-1);
            //we now have the correct total, compare it to the guesses
            int redGuess = serverInfo.playerRedGuesses.get(serverInfo.playerRedGuesses.size()-1);
            int blueGuess = serverInfo.playerBlueGuesses.get(serverInfo.playerBlueGuesses.size()-1);
            if (redGuess == correctTotal && blueGuess != correctTotal) {
                serverInfo.wonRound(true); //Only red was correct!
                callback.accept("Red guessed correctly! +1 point");
                checkGameEnd(); //Check if the game has been won now
            }
            else if (blueGuess == correctTotal && redGuess != correctTotal) {
                serverInfo.wonRound(false); //Only blue was correct!
                callback.accept("Blue guessed correctly! +1 point");
                checkGameEnd(); //Check if the game has been won now
            }
            else {
                callback.accept("Tie, no one gets points 😔");
            }
        }
    }

    private void checkGameEnd() {
        if (serverInfo.playerBluePoints == 2) {
            //Blue won!
        }
        else if (serverInfo.playerRedPoints == 2) {
            //Red won!
        }
    }
}
