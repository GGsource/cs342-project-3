import java.io.Serializable;
import java.util.ArrayList;

public class MorraInfo implements Serializable {
    public int playerRedPoints = 0;
    public int playerBluePoints = 0;
    public ArrayList<Integer> playerRedPlays;
    public ArrayList<Integer> playerRedGuesses;
    public ArrayList<Integer> playerBluePlays;
    public ArrayList<Integer> playerBlueGuesses;
    public boolean isCarrierPigeon = false;
    public String msg;
    public boolean isGameWon = false;
    public boolean isWinnerRed = false;
    public boolean isPlayerRed = false;

    MorraInfo() {
        playerRedPlays = new ArrayList<>();
        playerBluePlays = new ArrayList<>();
        playerRedGuesses = new ArrayList<>();
        playerBlueGuesses = new ArrayList<>();
    }

    MorraInfo(String message) {
        isCarrierPigeon = true;
        msg = message;
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
}
