package Game;

import Game.Connection.MessageType;
import Game.Map.Mission;

/**
 * Represents a match's turn
 */
public class Turn extends MessageReceiver implements Runnable {

    /**
     * Currently active player
     */
    private Player playing;

    public Player getPlaying() { return this.playing; }

    /**
     * Match where this turn is taking place
     */
    private Match associatedMatch;

    private Thread _instance;

    public Turn(Match Match, Player Current) {
        this.associatedMatch = Match;
        this.playing = Current;

        this._instance = new Thread(this);
        this._instance.start();
    }

    @Override
    public void run() {
        // Implement turn phases


        // After battle phase check if mission completed
        if(Mission.Completed(playing)){
            associatedMatch.setIncoming(playing.getId(), MessageType.Turn, playing.getName() + "-Winner");
        }

        // Notify end of turn when completed
        associatedMatch.setIncoming(playing.getId(), MessageType.Turn, "goAhead");
    }
}
