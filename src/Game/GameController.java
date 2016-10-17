package Game;

import Game.Connection.Chat;
import Game.Connection.Lobby;
import Game.Connection.MessageType;
import com.google.gson.Gson;

import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;

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

    private Thread _threadInstance;

    private GameController() {}

    /**
     * Starts game controller
     */
    public void init() {
        this._threadInstance = new Thread(this);
        this._threadInstance.start();
    }

    /**
     * Terminate all matches and join game controller thread
     */
    public void terminate() {
        Chat end = new Chat("Admin", "Server is shutting down");
        Message endMessage = new Message(-1, MessageType.Chat, (new Gson()).toJson(end));

        setIncoming(endMessage);

        for (Match m: matches) {
            m.setIncoming(endMessage);
            m.getPlayers().forEach((p) -> p.closeConnection());
        }

        try {
            this._threadInstance.interrupt();
            this._threadInstance.join();
        } catch (Exception e) {}
    }

    /**
     * Add a new user to the lobby
     *
     * @param Connection Connection relative to the user
     */
    public void addPlayer(Socket Connection) {

        Player newP = new Player(Connection);

        lobby.add(newP);

        // Notify all players for new player
        lobby.forEach((p) -> p.SendMessage(MessageType.Lobby, new Lobby(newP, null)));

        // Notify new player for all players
        newP.SendMessage(MessageType.Lobby, new Lobby(lobby, null));
    }

    /**
     * User gets back from match to lobby
     *
     * @param Player User to set back to lobby
     */
    public void returnPlayer(Player Player) {

        lobby.add(Player);

        // Notify all players for new player
        lobby.forEach((p) -> p.SendMessage(MessageType.Lobby, new Lobby(Player, null)));

        // Notify new player for all players
        Player.SendMessage(MessageType.Lobby, new Lobby(lobby, null));
    }

    /**
     * User is disconnecting from game
     *
     * @param PlayerId Player to remove from lobby
     */
    public void releasePlayer(int PlayerId) {
        for (Player p: lobby) {
            if(p.getId() == PlayerId) {
                lobby.remove(p);

                // Notify all players of leaving player
                lobby.forEach((l) -> l.SendMessage(MessageType.Lobby, new Lobby(null, p)));
                return;
            }
        }
    }

    /**
     * Starts a new match with passed users
     *
     * @param Players Players to add to new match
     */
    public void newMatch(Player... Players) {
        matches.add(new Match(Players));

        lobby.removeAll(Arrays.asList(Players));
    }

    /**
     * Removes ended match from list
     *
     * @param MatchId Match to remove
     */
    protected void endMatch(int MatchId) { matches.remove(getMatch(MatchId)); }

    @Override
    public void run() {
        while (true) {

            try {
                // If queue is empty wait for notification of new packet
                if(this.queue.isEmpty())
                    waitIncoming();

                System.out.println("GameController: message received");

                Message packet = this.queue.get(0);

                // If player is disconnecting remove it from lobby
                if(packet.Type == MessageType.GameState && packet.Json.equals(StateType.Abandoned.toString()))
                    releasePlayer(packet.PlayerId);

                // If its a chat message forward to all players in lobby
                if(packet.Type == MessageType.Chat) {
                    this.lobby.forEach((p) -> p.RouteMessage(packet.Type + "-" + packet.Json));
                    System.out.println("GameController: Chat routed");
                }

                // Remove packet from queue
                this.queue.remove(0);

            }catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
