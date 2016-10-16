package Game;

import Game.Connection.MessageType;

import java.net.Socket;
import java.util.ArrayList;

/**
 * Main controller to manage matches and user lobby
 */
public class GameController extends MessageReceiver implements Runnable {

    private static GameController _instance = new GameController();

    public static GameController getInstance() { return _instance; }

    /**
     * Currently playing matches' list
     */
    private ArrayList<Match> matches = new ArrayList<>();

    /**
     * List of currently opened matches
     *
     * @return Matches' list
     */
    public ArrayList<Match> getMatches() { return matches; }

    /**
     * Return requested match
     *
     * @param MatchId Match's id
     * @return Match with corresponding id. Null if id isn't found
     */
    public Match getMatch(int MatchId) {
        for (Match p: matches
             ) {
            if(p.getId() == MatchId)
                return p;
        }

        return null;
    }

    /**
     * Users waiting to play
     */
    private ArrayList<Player> lobby = new ArrayList<>();

    private Thread _thread = null;

    public GameController() {

    }

    /**
     * Add a new user to the lobby
     *
     * @param Connection Connection relative to the user
     */
    public void addPlayer(Socket Connection) {

        lobby.add(new Player(Connection));
    }

    /**
     * Starts a new match with passed users
     *
     * @param Players Players to add to new match
     */
    public void newMatch(Player... Players) {
        matches.add(new Match(Players));
    }

    /**
     * Removes ended match from list
     *
     * @param MatchId Match to remove
     */
    protected void endMatch(int MatchId) { matches.remove(getMatch(MatchId)); }

    @Override
    public void run() {
    }
}
