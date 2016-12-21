package Server.Game;

import Game.Map.Army.Color;
import Game.Connection.Chat;
import Game.Connection.Lobby;
import Game.MessageReceiver;
import Server.Game.Connection.MessageType;
import com.google.gson.*;
import java.lang.reflect.Type;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Main controller To manage matches and user lobby
 */
public class GameController extends MessageReceiver<MessageType> {

    private static GameController _instance = new GameController();

    public static GameController getInstance() { return _instance; }

    private final Gson gson;

    /**
     * Currently playing matches' list
     */
    private final HashMap<Integer, Match> matches = new HashMap<>();

    /**
     * Return requested match
     *
     * @param MatchId Match's id
     * @return Match with corresponding id. Null if id isn't found
     */
    public Match getMatch(int MatchId) {
        if(matches.containsKey(MatchId))
            return matches.get(MatchId);

        return null;
    }

    /**
     * Users waiting To play
     */
    private final HashMap<Integer, Player> lobby = new HashMap<>();

    private GameController() {
        super("GameController");

        gson = getGsonBuilder(null).create();

        // Handler for incoming chat messages routing
        messageHandlers.put(MessageType.Chat, (message) -> {
            this.lobby.forEach((id, p) -> p.RouteMessage(message.Type + "#" + message.Json));
        });

        // Handler for new match initialization request
        messageHandlers.put(MessageType.Match, (message) -> {
            final Game.Connection.Match<Player> requested = gson.fromJson(message.Json, MessageType.Match.getType());
            System.out.println("Game controller: New match request From " + message.PlayerId);

            final int newMatchId = Match.counter.getAndIncrement();
            Match newMatch;
            try {
                newMatch = new Match(Match.counter.getAndIncrement(), "RealWorldMap");
            } catch (ClassNotFoundException e){
                System.err.println("Can not create new match");
                return;
            }

            matches.put(newMatchId, newMatch);

            requested.Players.forEach(u -> {
                releasePlayer(u);
                newMatch.addPlayer(u);
            });

            newMatch.initMatch();
            System.out.println("Game controller: Launch new match with " + requested.Players.size() + " players.");
        });

        // Handler for GameState message received From match (always used To finalize match)
        messageHandlers.put(MessageType.GameState, (message) -> endMatch(message.PlayerId));
    }

    /**
     * Starts game controller
     */
    public void init() {
        // Start message receiver
        this.startExecutor();

        System.out.println("Game controller: Message receiver up and running.");
    }

    /**
     * Terminate all matches and join game controller thread
     */
    public synchronized void terminate() {
        // Stop message receiver
        this.stopExecutor();
        System.out.println("Game controller: Message receiver stopped.");

        final Chat<Player> end = new Chat<>(Player.getAI(-1, Color.RED), "Server is shutting down.");

        // Send end message To matches Players and close connection
        matches.forEach((matchId, m) -> endMatch(matchId));

        // Send end message and close connection of lobby Players
        lobby.forEach((id, p) -> {
            System.out.println("Game controller: Releasing player " + p.username);
            p.SendMessage(MessageType.Chat, end);
            p.closeConnection(true);
        });

        System.out.println("Game controller: All users released. Ready To join.");
    }

    /**
     * Add a new user To the lobby
     *
     * @param Connection Connection relative To the user
     */
    public void addPlayer(int Id, String Username, Socket Connection) {

        final Player newP = new Player(Id, Username, Connection);

        // Notify all Players for new player
        lobby.forEach((id, p) -> p.SendMessage(MessageType.Lobby, new Lobby<>(newP, null)));

        lobby.put(Id, newP);

        // Notify new player for all Players
        lobby.get(Id).SendMessage(MessageType.Lobby, new Lobby<>(new ArrayList<>(lobby.values()), null));

        System.out.println("Game controller: New player in lobby.");
    }

    /**
     * User gets back From match To lobby
     *
     * @param Player User To set back To lobby
     */
    public void returnPlayer(Player Player) {

        System.out.println("Game controller: Player " + Player.username + " got back From match.");

        // Notify all Players for new player
        lobby.forEach((id ,p) -> p.SendMessage(MessageType.Lobby, new Lobby<>(Player, null)));

        lobby.put(Player.id, Player);

        // Notify new player for all Players
        Player.SendMessage(MessageType.Lobby, new Lobby<>(new ArrayList<>(lobby.values()), null));
    }

    /**
     * User is disconnecting From game
     *
     * @param PlayerId Player To remove From lobby
     */
    void releasePlayer(int PlayerId) {
        final Player leaving = lobby.get(PlayerId);
        lobby.remove(PlayerId);
        lobby.forEach((id, p) -> p.SendMessage(MessageType.Lobby, new Lobby<>(null, leaving)));
    }

    void releasePlayer(Player Player) {
        lobby.remove(Player.id);
        lobby.forEach((id, p) -> p.SendMessage(MessageType.Lobby, new Lobby<>(null, Player)));
    }

    /**
     * End specified match and removes it From match list
     *
     * @param MatchId Match To remove
     */
    private void endMatch(int MatchId) {
        matches.get(MatchId).terminate();
        System.out.println("Game controller: Terminating match " + MatchId);
        matches.remove(MatchId);
    }

    private GsonBuilder getGsonBuilder(GsonBuilder builder){
        if(builder == null)
            builder = new GsonBuilder();

        builder.registerTypeAdapter(Player.class, new PlayerDeserializer(this));

        return builder;
    }

    private class PlayerDeserializer implements JsonDeserializer<Player> {

        private final GameController gc;

        public PlayerDeserializer(GameController GC){
            this.gc = GC;
        }


        @Override
        public Player deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            return gc.lobby.get(json.getAsJsonObject().get("id").getAsInt());
        }
    }
}
