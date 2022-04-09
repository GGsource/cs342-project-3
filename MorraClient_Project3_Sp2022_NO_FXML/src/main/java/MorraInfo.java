import java.io.Serializable;
import java.util.ArrayList;

public class MorraInfo implements Serializable {
    public int playerRedPoints = 0;
    public int playerBluePoints = 0;
    public ArrayList<Integer> playerRedPlays;
    public ArrayList<Integer> playerRedGuesses;
    public ArrayList<Integer> playerBluePlays;
    public ArrayList<Integer> playerBlueGuesses;
    public boolean isMessagePigeon = false;
    public String msg;
    public boolean isResetPigeon = false;
    //public boolean isGameWon = false;
    //public boolean isWinnerRed = false;
    public boolean isPlayerRed = false;

    MorraInfo() {
        playerRedPlays = new ArrayList<>();
        playerBluePlays = new ArrayList<>();
        playerRedGuesses = new ArrayList<>();
        playerBlueGuesses = new ArrayList<>();
    }

    //If object is made with a string, then it is only sending a message
    MorraInfo(String message) {
        isMessagePigeon = true;
        msg = message;
    }

    //If object is made with a bool, then it is only telling players if the game has
    //ended or not
    MorraInfo(boolean isTimeToReset) {
        isResetPigeon = true;
    }

    //Since we only have 2 players, using a boolean to tell who called a
    //method is much more convenient. If wasRed == 1 then it was redPlayer.
    
    public void wonRound(boolean wasRed) {
        if (wasRed) {
            playerRedPoints += 1;
        }
        else {
            playerBluePoints += 1;
        }
    }
    public void makeMove(int move, boolean wasRed) {
        if (wasRed) {
            playerRedPlays.add(move);
        }
        else {
            playerBluePlays.add(move);
        }
    }
    public void makeGuess(int guess, boolean wasRed) {
        if (wasRed) {
            playerRedGuesses.add(guess);
        }
        else {
            playerBlueGuesses.add(guess);
        }
    }
    public int getLastPlay() {
        if (isPlayerRed){
            System.out.println("getLastPlay believes you are Red.");
            return playerRedPlays.get(playerRedPlays.size()-1);
        }
        System.out.println("getLastPlay believes you are Blue.");
        return playerBluePlays.get(playerBluePlays.size()-1);
    }
    public int getLastGuess() {
        if (isPlayerRed){
            return playerRedGuesses.get(playerRedGuesses.size()-1);
        }
        return playerBlueGuesses.get(playerBlueGuesses.size()-1);
    }
    public ArrayList<Integer> getPlaysList() {
        if (isPlayerRed){
            return playerRedPlays;
        }
        return playerBluePlays;
    }
    public ArrayList<Integer> getGuessList() {
        if (isPlayerRed){
            return playerRedGuesses;
        }
        return playerBlueGuesses;
    }

    public void reset() {
        playerRedPoints = 0;
        playerBluePoints = 0;
        playerRedPlays.clear();
        playerRedGuesses.clear();
        playerBluePlays.clear();
        playerBlueGuesses.clear();
    }
}
