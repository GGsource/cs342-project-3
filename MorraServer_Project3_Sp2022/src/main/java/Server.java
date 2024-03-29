import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;
import java.util.function.Consumer;

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
    boolean isGameEnded = false;
    MorraInfo serverInfo;
    boolean receivedRedChoice;
    boolean receivedBlueChoice;
    boolean redWantsReplay = false;
    boolean blueWantsReplay = false;

    Server(Consumer<Serializable> call, int givenPort) {
        callback = call;
        chosenPort = givenPort;
        ourServer = new TheServer();
        ourServer.start();
    }

    public class TheServer extends Thread {
        public void run() {
            try (ServerSocket servingSocket = new ServerSocket(chosenPort);) {
                //System.out.print("Server is ready and waiting for clients on port "+ chosenPort +"!");
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
                System.out.println("New client connected to this server!");
                in = new ObjectInputStream(clientConnection.getInputStream());
                out = new ObjectOutputStream(clientConnection.getOutputStream());
                clientConnection.setTcpNoDelay(true);
                assignRole(this);
            }
            catch (Exception e) {
                System.out.println("Stream failed to open on new client :(");
                e.printStackTrace();
            }

            while (true) {
                try {
                    MorraInfo receivedInfo = (MorraInfo)in.readObject();
                    if (!receivedInfo.isMessagePigeon) {
                        //System.out.print("Incoming info before deciding if its red or blue:");
                        //System.out.print("incoming redPlays:    " + receivedInfo.playerRedPlays);
                        //System.out.print("incoming redGuesses:  " + receivedInfo.playerRedGuesses);
                        //System.out.print("incoming bluePlays:   " + receivedInfo.playerBluePlays);
                        //System.out.print("incoming blueGuesses: " + receivedInfo.playerBlueGuesses);

                        //A player sent info, receive it
                        if (isGameStarted)
                            updateInfo(receivedInfo);
                    }
                    else if (receivedInfo.msg.startsWith("&")) {
                        //One of the players has told us they want to replay, let the others know
                        if (receivedInfo.isPlayerRed) {
                            messageAllClients("Red wants to replay!");
                            redWantsReplay = true;
                        }
                        else {
                            messageAllClients("Blue wants to replay!");
                            blueWantsReplay = true;
                        }
                        if (redWantsReplay && blueWantsReplay) {
                            InitializeGame();
                        }
                    }
                }
                catch (Exception e) {
                    callback.accept(this.getRole() + " has disconnected!");
                    redWantsReplay = false;
                    blueWantsReplay = false;
                    if (this.isRed ) {
                        //red just disconnected, remove it and send blue back into the waiting queue
                        clientRed = null;
                        //System.out.println("Client Blue left so they are now made null");
                        reassignPlayers(true);
                    }
                    else if (this.isBlue) {
                        //Blue just disconnected, remove it and send red back to waiting
                        clientBlue = null;
                        //System.out.println("Client Blue left so they are now made null");
                        reassignPlayers(false);
                    }
                    else {
                        guestCount -= 1;
                        clientGuestList.remove(this); //Unlist this guest since they left
                    }
                    displayUserCounts();
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
                messageAllClients("Red Player left, looking for a replacement...");
            }
            else {
                messageAllClients("Blue Player left, looking for a replacement...");
            }
            playerCount -= 1;
            isGameStarted = false;
            //clearGameState();
            //Should check if new pairing can be made immediately
            try {
                if (!clientGuestList.isEmpty()) {
                    ClientThread c = clientGuestList.remove();
                    if (clientRed == null) {
                        clientRed = c;
                        clientRed.isRed = true;
                        messageAllClients("A new Red Player was successfully been chosen!");
                        clientRed.out.writeObject(new MorraInfo("You have been chosen as the new Red Player"));
                        playerCount += 1;
                        guestCount -= 1;
                    }
                    else if (clientBlue == null) {
                        clientBlue = c;
                        clientBlue.isBlue = true;
                        messageAllClients("A new Blue Player was successfully been chosen!");
                        clientBlue.out.writeObject(new MorraInfo("You have been chosen as the new Blue Player"));
                        playerCount += 1;
                        guestCount -= 1;
                    }
                }
                else {
                    //System.out.println("About to attempt to tell everyone we didn't find a backup player...");
                    messageAllClients("Not enough players for a new match, waiting for new players...");
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
            isGameEnded = false;
            isGameStarted = true;
            messageAllClients("Red and Blue are ready to begin!");
            clientRed.out.writeObject(new MorraInfo("New Game started! Good luck " + clientRed.getRole()));
            clientBlue.out.writeObject(new MorraInfo("New Game started! Good luck " + clientBlue.getRole()));
            for (ClientThread waiter : clientGuestList) {
                waiter.out.writeObject(new MorraInfo("A round has started, you are an observer until its your turn"));
            }
            //System.out.println("Both players are connected, the game can now begin!");
            serverInfo = new MorraInfo();
            displayPlayerScores();
            enableChoosingBtns();
            sendPlayImages();
            //Now send both players a copy of MoraInfo
            MorraInfo rInfo = new MorraInfo();
            MorraInfo bInfo = new MorraInfo();
            rInfo.isPlayerRed = true;
            clientRed.out.writeObject(rInfo);//Need to let it know it is red
            clientBlue.out.writeObject(bInfo);
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
                playerCount += 1;
            }
            else if (clientBlue == null) {
                clientBlue = c;
                clientBlue.isBlue = true;
                clientBlue.out.writeObject(new MorraInfo("You have been assigned Player Blue"));
                playerCount += 1;
            }
            else {
                c.isGuest = true;
                clientGuestList.add(c);
                c.out.writeObject(new MorraInfo("You have been assigned to the waiting list"));
                guestCount += 1;
            }
            displayUserCounts();
            callback.accept("New client has connected! They have been designated: " + c.getRole() + "!");
            if (hasTwoPlayers()) {
                if (!isGameStarted && !isGameEnded) {
                    InitializeGame();
                }
            }
            else {
                c.out.writeObject(new MorraInfo("You are currently the only player, please wait for an opponent!"));
                callback.accept("Only one player present, waiting for another...");
            }
        }
        catch (IOException e) {
            System.out.println("Uh oh. Couldn't write to thread's output...");
            e.printStackTrace();
        }
    }

    private void updateInfo(MorraInfo incomingInfo) {
        //System.out.println("Entered updateInfo...");
        messageAllClients("One of the players has submitted their guess...");
        if (incomingInfo.playerRedPlays.size() > serverInfo.playerRedPlays.size()) {
            //System.out.print("incomingInfo has a larger array of clientRed's plays so it must be sent by Red...");
            // System.out.println("server's redPlays:     " + serverInfo.playerRedPlays);
            // System.out.println("clientRed's redPlays:  " + incomingInfo.playerRedPlays);
            // System.out.println("server's bluePlays:    " + serverInfo.playerBluePlays);
            // System.out.println("clientRed's bluePlays: N/A");
            //Red plays increased so it was sent by red!
            serverInfo.playerRedPlays.add(incomingInfo.getLastPlay());
            serverInfo.playerRedGuesses.add(incomingInfo.getLastGuess());
            receivedRedChoice = true;
        }
        else {
            //Red plays didn't increase so this was sent by blue!
            //System.out.print("incomingInfo did noot have larger array of clientRed's plays so it must be sent by Blue...");
            // System.out.println("server's redPlays:      " + serverInfo.playerRedPlays);
            // System.out.println("clientBlue's redPlays:  N/A");
            // System.out.println("server's bluePlays:     " + serverInfo.playerBluePlays);
            // System.out.println("clientBlue's bluePlays: " + incomingInfo.playerBluePlays);

            serverInfo.playerBluePlays.add(incomingInfo.getLastPlay());
            serverInfo.playerBlueGuesses.add(incomingInfo.getLastGuess());
            receivedBlueChoice = true;
        }
        //System.out.println("Past first updateInfo if statement...");
        //Now we have received the choices of one of the players, determine
        //if both have sent in their answers and if so pick winner
        if (receivedRedChoice && receivedBlueChoice) {
            try {
                //Both have given their guesses
                int redPlay = serverInfo.playerRedPlays.get(serverInfo.playerRedPlays.size()-1);
                int bluePlay = serverInfo.playerBluePlays.get(serverInfo.playerBluePlays.size()-1);
                int correctTotal = redPlay + bluePlay;
                //we now have the correct total, compare it to the guesses
                int redGuess = serverInfo.playerRedGuesses.get(serverInfo.playerRedGuesses.size()-1);
                int blueGuess = serverInfo.playerBlueGuesses.get(serverInfo.playerBlueGuesses.size()-1);
                messageAllClients("Red guessed:       " + redGuess);
                messageAllClients("Blue guessed:      " + blueGuess);
                messageAllClients("Correct total was: " + correctTotal);
                //Send images of plays
                sendPlayImages(redPlay, bluePlay);
                if (redGuess == correctTotal && blueGuess != correctTotal) {
                    serverInfo.wonRound(true); //Only red was correct!
                    displayPlayerScores();
                    messageAllClients("Red guessed correctly! They get +1 point");
                }
                else if (blueGuess == correctTotal && redGuess != correctTotal) {
                    serverInfo.wonRound(false); //Only blue was correct!
                    displayPlayerScores();
                    messageAllClients("Blue guessed correctly! +1 point");
                }
                else {
                    messageAllClients("Tie, no one gets points 😔");
                }
                receivedRedChoice = false;
                receivedBlueChoice = false;
                checkGameEnd(); //Check if the game has been won now
            }
            catch (Exception e) {
                System.out.println("Error when trying to find who won this round..");
                e.printStackTrace();
            }
        }
        //System.out.println("Exited updateInfo...");
    }

    private void checkGameEnd() {
        try {
            if (serverInfo.playerBluePoints == 2) {
                //Blue won!
                //System.out.print("Blue won!");
                messageAllClients("Blue wins the game!");
                clientBlue.out.writeObject(new MorraInfo("Congratulations you won the game!"));
                isGameStarted = false;
                isGameEnded = true;
                //reenable new game button
                enableNewGame();
            }
            else if (serverInfo.playerRedPoints == 2) {
                //Red won!
                //System.out.print("Red won!");
                messageAllClients("Red wins the game!");
                clientRed.out.writeObject(new MorraInfo("Congratulations you won the game!"));
                isGameStarted = false;
                isGameEnded = true;
                //reenable new game button
                enableNewGame();
            }
            else {
                //no one has won, reenable the buttons
                enablePlayingFields();
            }
        } catch (IOException e) {
            System.out.println("Failed to congratulate the winner for finishing 😔");
            e.printStackTrace();
        }

        //Send the clients a message showing the game has not ended so they can continue
        // MorraInfo gameWinState = new MorraInfo(false);
        // try {
        //     clientRed.out.writeObject(gameWinState);
        //     clientBlue.out.writeObject(gameWinState);
        // }
        // catch (IOException e) {
        //     System.out.print("Uh oh. Failed to tell the players to continue their game!");
        //     e.printStackTrace();
        // }

    }
    private void messageAllClients(String message) {

        try {
            callback.accept(message);
            if (clientRed != null)
                clientRed.out.writeObject(new MorraInfo(message));
            if (clientBlue != null)
                clientBlue.out.writeObject(new MorraInfo(message));
            for (ClientThread waiter : clientGuestList) {
                waiter.out.writeObject(new MorraInfo(message));
            }
        } catch (IOException e) {
            System.out.println("Uh oh, failed to message all the clients!");
            e.printStackTrace();
        }
    }

    // public void clearGameState() {
    //     isGameStarted = false;
    //     receivedRedChoice = false;
    //     receivedBlueChoice = false;
        
    //     //everything in serverInfo
    //     serverInfo.reset();
        
    //     //tell clients to clear their infos too
    //     try {
    //         if (clientRed != null)
    //             clientRed.out.writeObject(new MorraInfo(true));
    //         if (clientBlue != null)
    //             clientBlue.out.writeObject(new MorraInfo(true));
    //     } catch (IOException e) {
    //         System.out.println("Failed to tell the players to clear their games 😔");
    //     }
    //     //selectedPlay
    //     //selectedGuess

    // }

    private void displayUserCounts() {
        callback.accept("*" + playerCount + "*" + guestCount);
    }
    private void displayPlayerScores() {
        callback.accept("?" + serverInfo.playerRedPoints + "?" + serverInfo.playerBluePoints);
    }
    private void enablePlayingFields () {
        try {
            clientBlue.out.writeObject(new MorraInfo("%"));
            clientRed.out.writeObject(new MorraInfo("%"));
        }
        catch (IOException e) {
            System.out.println("Uh oh, failed to reenable buttons and fields...");
            e.printStackTrace();
        }
    }
    private void enableChoosingBtns() {
        try {
            clientBlue.out.writeObject(new MorraInfo("#"));
            clientRed.out.writeObject(new MorraInfo("#"));
        }
        catch (IOException e) {
            System.out.println("Uh oh, failed to reenable buttons 0-5...");
            e.printStackTrace();
        }
    }
    private void enableNewGame() {
        try {
            clientRed.out.writeObject(new MorraInfo("@"));
            clientBlue.out.writeObject(new MorraInfo("@"));
        }
        catch (IOException e) {
            System.out.println("Uh oh, failed to reenable new game buttons...");
            e.printStackTrace();
        }
    }
    private void sendPlayImages (int rPlay, int bPlay) {
        try {
            clientRed.out.writeObject(new MorraInfo("^"+bPlay));
            clientBlue.out.writeObject(new MorraInfo("^"+rPlay));
        }
        catch (IOException e) {
            System.out.println("Uh oh, failed to send plays to set images...");
            e.printStackTrace();
        }
    }
    private void sendPlayImages() {
        try {
            clientRed.out.writeObject(new MorraInfo("^q"));
            clientBlue.out.writeObject(new MorraInfo("^q"));
        } catch (IOException e) {
            System.out.println("Uh oh, failed to reset plays images...");
            e.printStackTrace();
        }
    }
}
