package Client.Game;

import Client.Game.Connection.Serializer.ObservableUserSerializer;
import Client.Game.Connection.Serializer.SimpleObjectPropertySerializer;
import Client.Main;
import Client.UI.ChatBox.ChatBox;
import Game.Connection.Mission;
import Game.Connection.Serializer.IntegerPropertySerializer;
import Game.Connection.Serializer.StringPropertySerializer;
import Client.Game.Map.*;
import Game.Connection.*;
import Game.MessageReceiver;
import Client.Game.Connection.MessageType;
import Game.Sounds.Sounds;
import Game.StateType;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.StringProperty;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.function.Consumer;
import java.util.prefs.Preferences;

/**
 * Handle communication with the server
 */
public class GameController extends MessageReceiver<MessageType> {

    private final String serverAddress = "localhost";

    private final short serverPort = 8080;

    private volatile ResourceBundle resources;

    public void setLocale(Locale Language) {
        resources = ResourceBundle.getBundle("Client.UI.Resources", Language);
    }

    public ResourceBundle getResources() { return resources; }

    private volatile ConnectionHandler CH;

    /* Chat section */

    private volatile ChatBox chatBox;

    public ChatBox getChatBox() {
        if(chatBox == null)
            chatBox = new ChatBox(-10, this::SendChat);

        return chatBox;
    }

    /* User section */

    private Player user;

    public Player getUser() { return this.user; }

    /* UI Handlers */

    private volatile boolean inMatch = false;

    private volatile Consumer<Lobby<Player>> updateUsers = null;

    /**
     * Set method reference to update users list in UI
     *
     * @param UpdateUsers Method reference
     */
    public void setUpdateUsers(Consumer<Lobby<Player>> UpdateUsers) { updateUsers = UpdateUsers; }

    private volatile Consumer<MatchLobby<Match<Player>>> updateMatches = null;

    public void setUpdateMatches(Consumer<MatchLobby<Match<Player>>> UpdateMatches) { updateMatches = UpdateMatches; }

    private volatile MapHandler mapHandler;

    public void setMapHandler(MapHandler MapHandler){
        this.mapHandler = MapHandler;
    }

    private volatile CardsHandler cardsHandler;

    public void setCardsHandler(CardsHandler CardsHandler){ this.cardsHandler = CardsHandler; }

    /* UI Handlers end */

    /**
     * Initializer for all message handlers
     */
    public GameController() {
        super("GameController");

        setLocale(new Locale(Preferences.userNodeForPackage(Client.Main.class).get("language", "en")));

        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(IntegerProperty.class, new IntegerPropertySerializer());
        gsonBuilder.registerTypeAdapter(StringProperty.class, new StringPropertySerializer());
        gsonBuilder.registerTypeAdapter(Player.class, new ObservableUserSerializer());
        gsonBuilder.registerTypeAdapter(new TypeToken<SimpleObjectProperty<Player>>(){}.getType(),
                new SimpleObjectPropertySerializer(Player.class));
        final Gson gson = gsonBuilder.create();

        // Handler for incoming chat messages
        messageHandlers.put(MessageType.Chat, (message) -> {
            final Chat<Player> chat = gson.fromJson(message.Json, MessageType.Chat.getType());
            chatBox.addChat(chat);
        });

        // Handler for matches list
        messageHandlers.put(MessageType.MatchLobby, (message) -> {
           final MatchLobby<Match<Player>> matchLobby = gson.fromJson(message.Json, MessageType.MatchLobby.getType());
           updateMatches.accept(matchLobby);
        });

        // Handler for user list in match room
        messageHandlers.put(MessageType.Lobby, (message) -> {
            final Lobby<Player> lobbyUsers = gson.fromJson(message.Json, MessageType.Lobby.getType());
            updateUsers.accept(lobbyUsers);
        });

        // Handler for match initialization
        messageHandlers.put(MessageType.Match, (message) -> {
            stopExecutor();
            System.out.println("GameController: Match message: " + message.Json);
            final Match<Player> match = gson.fromJson(message.Json, MessageType.Match.getType());

            Main.toMatch(match);
            inMatch = true;
        });

        // Handler for positioning message
        messageHandlers.put(MessageType.Positioning, message -> {
            final Positioning pos = gson.fromJson(message.Json, MessageType.Positioning.getType());
            SendMessage(MessageType.MapUpdate, mapHandler.positionArmies(pos.newArmies));
        });

        messageHandlers.put(MessageType.Mission, (message) -> {
            final Mission mission = gson.fromJson(message.Json, MessageType.Mission.getType());
            mapHandler.setMission(mission.Mission);
        });

        // Handler for map updates
        messageHandlers.put(MessageType.MapUpdate, (message) ->
            mapHandler.updateMap(gson.fromJson(message.Json, MessageType.MapUpdate.getType())));

        // Handler for card messages
        messageHandlers.put(MessageType.Cards, (message) -> {
            System.out.println("Game Controller: Cards message receive.\n" + message.Json);
            final Cards cards = gson.fromJson(message.Json, MessageType.Cards.getType());

            // If message is not empty add the card to user's list
            if(!cards.combination.isEmpty()) {
                // Add card to user local cards
                cardsHandler.addCard(cards.combination.get(0));
                return;
            }

            // Else ask user to play a combination of cards and
            // return response to server
            SendMessage(MessageType.Cards, cardsHandler.requestCombination());
        });

        // Handler for attacked territory
        messageHandlers.put(MessageType.Battle, (message) -> {
            final Battle<Territory> battle = gson.fromJson(message.Json, MessageType.Battle.getType());

            if(battle.from == null){
                // Start attack phase
                mapHandler.attackPhase();
                // Report end of attack phase to server
                SendMessage(MessageType.Battle, battle);
            }
            else {// Else player is under attack, than request defense armies and update server
                SendMessage(MessageType.Battle, mapHandler.requestDefense(battle));
            }
        });

        // Handler for game state changes
        messageHandlers.put(MessageType.GameState, (message) -> {
            stopExecutor();
            System.out.println("GameController: GameState message received.\n" + message.Json);

            final GameState<Player> gameState = gson.fromJson(message.Json, MessageType.GameState.getType());

            user.Territories.clear();
            user.Color = null;

            switch (gameState.state){
                case Winner:
                    Main.toLobby();
                    if(gameState.winner.equals(user)) {
                        Main.showDialog(resources.getString("gameStateMessage"),
                                resources.getString("victoryMessage"),
                                resources.getString("close"));
                        Sounds.Victory.play();
                    }
                    else {
                        Main.showDialog(resources.getString("gameStateMessage"),
                                String.format(resources.getString("lostMessage"), gameState.winner.Username.get()),
                                resources.getString("close"));
                    }
                    break;
                case Abandoned:
                    Main.toLobby();
                    Main.showDialog(resources.getString("gameStateMessage"),
                            String.format(resources.getString("abandonedMessage"), gameState.winner.Username.get()),
                            resources.getString("close"));
                    break;
                case Defeated:
                    Main.showDialog(resources.getString("gameStateMessage"),
                            resources.getString("defeatedMessage"),
                            resources.getString("close"));

                    // Return cards to server
                    SendMessage(MessageType.Cards, new Cards(cardsHandler.returnCards()));
                    return;
            }

            mapHandler = null;
            cardsHandler = null;
        });
    }

    /**
     * Setup and start connection with the server
     *
     * @param Username Username choose from user
     */
    public void initConnection(String Username) throws Exception {
        final Socket connection;
        final BufferedReader receive;
        final PrintWriter send;

        try {
            connection = new Socket(serverAddress, serverPort);
            receive = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            send = new PrintWriter(connection.getOutputStream(), true);
        } catch (IOException e) {
            throw new Exception(resources.getString("initError"));
        }

        // Try connecting to server
        // Send username to the server
        send.println(Username);

        final String incoming = receive.readLine();

        // If server doesn't respond notify the user
        if(!incoming.startsWith("OK"))
            throw new Exception(resources.getString("wrongResponse"));

        // Set user for this client
        this.user = new Player(Integer.valueOf(incoming.split("[#]")[1]), Username, null);

        this.chatBox = new ChatBox(user.getId(), this::SendChat);

        // If connection is successfully established initialize connection handler
        CH = new ConnectionHandler(this, connection, receive, send, "GameController-ConnectionHandler");

        // If an error log is present, send it to the server
        final Path errlog = Paths.get("./errlog.txt");
        if(Files.exists(errlog)) {
            CH.SendMessage(MessageType.LogFile, Files.readAllLines(errlog).stream().reduce("", (s1, s2) -> s1 + "\n" + s2));
            Files.delete(errlog);
        }
    }

    /**
     * Stops current connection with the server
     */
    public void stopConnection(boolean fromClient) {
        // Send close connection notification to server
        if(fromClient)
            CH.RouteMessage("End");

        // Stop executor and close connection
        stopExecutor();
        CH.closeConnection();

        // If finalizing exit
        if(fromClient) {
            System.out.println("Client finalized");
            return;
        }

        // Else reset object for new connection attempt
        Main.setGameController(new GameController());

        Main.toLogin();

        Main.showDialog(resources.getString("applicationErrorTitle"), resources.getString("connectionError"), resources.getString("close"));
    }

    /**
     * Leave current match and go back to lobby
     */
    public void abortMatch() {
        // If player is not in a match return
        if(!inMatch) return;

        Main.toLobby();
        SendMessage(MessageType.GameState, new GameState<>(StateType.Abandoned, user));
    }

    private void SendChat(String Text) {
        SendMessage(MessageType.Chat, new Chat<>(user, Text));
    }

    /**
     * Send a message to the server
     *
     * @param Type Type of message
     * @param MessageObj Object of specified type
     */
    public void SendMessage(MessageType Type, Object MessageObj) {
        CH.SendMessage(Type, MessageObj);
    }
}
