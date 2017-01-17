package Server.Game;

import Game.Connection.MatchLobby;
import Game.Map.Army.Color;
import Game.Connection.Chat;
import Game.Map.Maps;
import Game.MessageReceiver;
import Server.Game.Connection.MessageType;
import com.google.gson.*;
import javafx.collections.FXCollections;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import java.lang.reflect.Type;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Main controller to manage matches and user lobby
 */
public class GameController extends MessageReceiver<MessageType> {

    private static GameController _instance = new GameController();

    public static GameController getInstance() { return _instance; }

    private final Gson gson;

    /**
     * Currently playing matches' list
     */
    private final ObservableMap<Integer, Match> matches = FXCollections.observableHashMap();

    /**
     * Return requested match
     *
     * @param MatchId Match's id
     * @return Match with corresponding id. Null if id isn't found
     */
    Match getMatch(Integer MatchId) {
        if(matches.containsKey(MatchId))
            return matches.get(MatchId);

        return null;
    }

    /**
     * Users waiting to play
     */
    private final HashMap<Integer, Player> lobby = new HashMap<>();

    private volatile ObservableList<Player> players;

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
            System.out.println("Game controller: New match request from " + message.PlayerId);

            // Get requested match
            Match match = matches.get(requested.Id);

            // If match is null create it
            if(match == null) {
                try {
                    match = new Match(Match.counter.getAndIncrement(), requested.Name, requested.GameMap);
                } catch (ClassNotFoundException e){
                    System.err.println("Game Controller: Can not create new match");
                    return;
                }

                matches.put(match.Id, match);

                match.addPlayer(requested.Players.get(0));

                sendAll(MessageType.MatchLobby, new MatchLobby<>(match, null));

                System.out.println("Game controller: New match created with id " + match.Id);
            }
            else {
                match.addPlayer(requested.Players.get(0));
                if(!match.isStarted())
                    sendAll(MessageType.MatchLobby, new MatchLobby<>(match, match));
            }
        });
    }

    /**
     * Starts game controller
     *
     * @param Players TableView players list
     * @param Matches TableView matches list
     */
    public void init(ObservableList<Player> Players, ObservableList<Match> Matches) {

        if(Players != null)
            players = Players;

        if(Matches != null)
            matches.addListener((MapChangeListener.Change<? extends Integer, ? extends Match> change) -> {
               if(change.wasAdded())
                   Matches.add(change.getValueAdded());

               if(change.wasRemoved())
                   Matches.remove(change.getValueRemoved());
            });

        // Start message receiver
        this.startExecutor();

        System.out.println("Game controller: Message receiver up and running.");
    }

    public void init() {
        init(null, null);
    }

    /**
     * Terminate all matches and join game controller thread
     */
    public synchronized void terminate() {
        // Stop message receiver
        this.stopExecutor();
        System.out.println("Game controller: Message receiver stopped.");

        final Chat<Player> end = new Chat<>(Player.getAI(), "Server is shutting down.");

        // Send end message To matches Players and close connection
        matches.forEach((matchId, m) -> endMatch(m));

        // Send end message and close connection of lobby Players
        lobby.forEach((id, p) -> {
            System.out.println("Game controller: Releasing player " + p.getUsername());
            p.SendMessage(MessageType.Chat, end);
            p.closeConnection(true);
        });

        System.out.println("Game controller: All users released. Ready To join.");
    }

    private void sendAll(MessageType Type, Object Message) {
        lobby.forEach((id, p) -> p.SendMessage(Type, Message));
    }

    /**
     * Add a new user to the lobby
     *
     * @param Connection Connection relative to the user
     */
    public void addPlayer(int Id, String Username, Socket Connection) {

        final Player newP = new Player(Id, Username, Connection);

        lobby.put(Id, newP);

        if(players != null)
            players.add(newP);

        // Send current matches list to new player
        newP.SendMessage(MessageType.MatchLobby, new MatchLobby<>(matches.values(), null));

        System.out.println("Game controller: New player in lobby.");
    }

    /**
     * User gets back from match to lobby
     *
     * @param Player User to set back to lobby
     */
    void returnPlayer(Player Player) {

        System.out.println("Game controller: Player " + Player.getUsername() + " got back from match.");

        // Send update to all players
        final Match match = matches.get(Player.getMatchId());
        if(!match.isStarted())
            sendAll(MessageType.MatchLobby, new MatchLobby<>(match, match));

        // Clean player and put it back to lobby
        Player.exitMatch();
        lobby.put(Player.id, Player);

        //Send current matches to player
        Player.SendMessage(MessageType.MatchLobby, new MatchLobby<>(matches.values(), null));
    }

    /**
     * User is disconnecting from game
     *
     * @param Player Player to remove from lobby
     * @param Remove True if player has exited, false if player has entered a match
     */
    void releasePlayer(Player Player, boolean Remove) {
        lobby.remove(Player.id);

        if(Remove && players != null)
            players.removeIf(Player::equals);
    }

    /**
     * End specified match and removes it from match list
     *
     * @param Match Match to remove
     */
    void endMatch(Match Match) {
        lobby.putAll(Match.terminate());
        System.out.println("Game controller: Match " + Match.Id + " terminated.");
        matches.remove(Match.Id);

        lobby.forEach((id, p) -> p.SendMessage(MessageType.MatchLobby, new MatchLobby<>(null, Match)));
    }

    private GsonBuilder getGsonBuilder(GsonBuilder builder){
        if(builder == null)
            builder = new GsonBuilder();

        builder.registerTypeAdapter(Player.class, new PlayerDeserializer(this));
        builder.registerTypeAdapter(Match.class, new MatchSerializer());

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

    public static class MatchSerializer implements JsonSerializer<Server.Game.Match> {
        @Override
        public JsonElement serialize(Match src, Type typeOfSrc, JsonSerializationContext context) {
            final JsonObject jm = new JsonObject();

            jm.addProperty("Id", src.Id);
            jm.addProperty("Name", src.Name);
            jm.addProperty("GameMap", src.GameMap.name());
            jm.addProperty("IsStarted", src.isStarted());

            final JsonArray pa = new JsonArray();
            src.getPlayers().forEach((id, p) -> {
                if(p.isPlaying() || !src.isStarted())
                    pa.add(context.serialize(p, Player.class));
            });

            jm.add("Players", pa);

            return jm;
        }
    }
}
