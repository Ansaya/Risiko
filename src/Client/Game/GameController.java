package Client.Game;

import Client.Game.Connection.Serializer.ObservableUserSerializer;
import Client.Main;
import Client.UI.ChatBox.ChatBox;
import Game.Connection.Mission;
import Game.Connection.Serializer.IntegerPropertySerializer;
import Game.Connection.Serializer.StringPropertySerializer;
import Client.Game.Observables.*;
import Game.Connection.*;
import Game.MessageReceiver;
import Client.Game.Connection.MessageType;
import Game.Sounds.Sounds;
import Game.StateType;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import javafx.application.Platform;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.StringProperty;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.function.Consumer;
import java.util.prefs.Preferences;

/**
 * Handle communication with the server
 */
public class GameController extends MessageReceiver<MessageType> implements Runnable {

    private final String serverAddress = "localhost";

    private volatile ResourceBundle resources;

    public void setLocale(Locale Language) {
        resources = ResourceBundle.getBundle("Client.UI.Resources", Language);
    }

    public ResourceBundle getResources() { return resources; }

    /* Connection section */
    /**
     * Connection to the server
     */
    private Socket connection;

    /**
     * Incoming stream
     */
    private BufferedReader receive;

    /**
     * Outgoing stream
     */
    private PrintWriter send;

    private final Gson gson;

    private volatile boolean listen;

    /* Connection section end */

    /* Chat section */

    private volatile ChatBox chatBox;

    public ChatBox getChatBox() {
        if(chatBox == null)
            chatBox = new ChatBox(-10, this::SendChat);

        return chatBox;
    }

    /* Chat section end */

    /* User section */
    /**
     * Current user
     */
    private ObservableUser user;

    public ObservableUser getUser() { return this.user; }

    /* User section end */

    /* UI Handlers */
    private volatile boolean inMatch = false;

    private volatile Consumer<Lobby<ObservableUser>> updateUsers = null;

    /**
     * Set method reference to update users list in UI
     *
     * @param UpdateUsers Method reference
     */
    public void setUpdateUsers(Consumer<Lobby<ObservableUser>> UpdateUsers) { updateUsers = UpdateUsers; }

    private volatile Consumer<MatchLobby<Match<ObservableUser>>> updateMatches = null;

    public void setUpdateMatches(Consumer<MatchLobby<Match<ObservableUser>>> UpdateMatches) { updateMatches = UpdateMatches; }

    private volatile MapHandler mapHandler;

    public void setMapHandler(MapHandler MapHandler){
        this.mapHandler = MapHandler;
    }

    private volatile CardsHandler cardsHandler;

    public void setCardsHandler(CardsHandler CardsHandler){ this.cardsHandler = CardsHandler; }

    /* UI Handlers end */

    private final Thread _threadInstance = new Thread(this, "GameController-SocketHandler");

    /**
     * Initializer for all message handlers
     */
    public GameController() {
        super("GameController");

        setLocale(new Locale(Preferences.userNodeForPackage(Client.Main.class).get("language", "it")));

        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(IntegerProperty.class, new IntegerPropertySerializer());
        gsonBuilder.registerTypeAdapter(StringProperty.class, new StringPropertySerializer());
        gsonBuilder.registerTypeAdapter(ObservableUser.class, new ObservableUserSerializer());
        gson = gsonBuilder.create();

        // Handler for incoming chat messages
        messageHandlers.put(MessageType.Chat, (message) -> {
            final Chat<ObservableUser> chat = gson.fromJson(message.Json, MessageType.Chat.getType());
            chatBox.addChat(chat);
        });

        // Handler for matches list
        messageHandlers.put(MessageType.MatchLobby, (message) -> {
           final MatchLobby<Match<ObservableUser>> matchLobby = gson.fromJson(message.Json, MessageType.MatchLobby.getType());
           updateMatches.accept(matchLobby);
        });

        // Handler for user list in match room
        messageHandlers.put(MessageType.Lobby, (message) -> {
            System.out.println("GameController: Lobby message: " + message.Json);
            final Lobby<ObservableUser> lobbyUsers = gson.fromJson(message.Json, MessageType.Lobby.getType());
            updateUsers.accept(lobbyUsers);
        });

        // Handler for match initialization
        messageHandlers.put(MessageType.Match, (message) -> {
            stopExecutor();
            System.out.println("GameController: Match message: " + message.Json);
            final Match<ObservableUser> match = gson.fromJson(message.Json, MessageType.Match.getType());

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
        messageHandlers.put(MessageType.MapUpdate, (message) -> {
            mapHandler.updateMap(gson.fromJson(message.Json, MessageType.MapUpdate.getType()));
        });

        // Handler for card messages
        messageHandlers.put(MessageType.Cards, (message) -> {
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
            final Battle<ObservableTerritory> battle = gson.fromJson(message.Json, MessageType.Battle.getType());

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
            System.out.println("GameController: GameState message: " + message.Json);

            final GameState<ObservableUser> gameState = gson.fromJson(message.Json, MessageType.GameState.getType());

            user.Territories.set(0);
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
                                resources.getString("lostMessage") + gameState.winner.Username.get(),
                                resources.getString("close"));
                    }
                    break;
                case Abandoned:
                    Main.toLobby();
                    Main.showDialog(resources.getString("gameStateMessage"),
                            gameState.winner.Username.get() + resources.getString("abandonedMessage"),
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
    public void InitConnection(String Username) throws Exception {
        try {
            this.connection = new Socket(serverAddress, 5757);
            this.receive = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            this.send = new PrintWriter(connection.getOutputStream(), true);
            this.listen = true;
        } catch (IOException e) {
            throw new Exception(resources.getString("initError"));
        }

        // Try connecting to server
        // Send username to the server
        this.send.println(Username);

        String incoming = receive.readLine();
        System.out.println("Server responded: " + incoming);

        // If server doesn't respond notify the user
        if(!incoming.startsWith("OK"))
            throw new Exception(resources.getString("wrongResponse"));

        // Set user for this client
        this.user = new ObservableUser(Integer.valueOf(incoming.split("[#]")[1]), Username, null);
        System.out.println("Got id " + user.Id.get() + " from server.");

        this.chatBox = new ChatBox(user.getId(), this::SendChat);

        // If connection is successfully established start listening and receiving
        _threadInstance.start();
    }

    /**
     * Stops current connection with the server
     */
    public void StopConnection(boolean fromClient) {
        if(!listen)
            return;

        listen = false;

        // Send close connection notification to server
        if(fromClient)
            send.println("End");

        stopExecutor();

        // Stop thread
        try {
            connection.close();
            _threadInstance.join();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

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
    public void AbortMatch() {
        // If player is not in a match return
        if(!inMatch)
            return;

        Main.toLobby();

        SendMessage(MessageType.GameState, new GameState<>(StateType.Abandoned, user));
    }

    @Override
    public void run() {

        // Incoming message buffer
        String Packet;

        // Listen to the server until necessary
        while (listen) {

            try {
                while ((Packet = receive.readLine()) != null){
                    if(Packet.equals("End")){
                        Platform.runLater(() -> StopConnection(false));
                        return;
                    }

                    String[] info = Packet.split("[#]", 2);

                    this.setIncoming(0, MessageType.valueOf(info[0]), info[1]);
                }

            }catch (Exception e) {
                if(e instanceof IOException) {
                    System.err.println("GamerController: Server connection lost");
                    break;
                }

                System.err.println("GameController: Message not recognized.");
                e.printStackTrace();
            }
        }

        if(listen)
            Platform.runLater(() -> StopConnection(false));
    }

    public void SendChat(String Text) {
        SendMessage(MessageType.Chat, new Chat<>(user, Text));
    }

    /**
     * Send a message to the server
     *
     * @param Type Type of message
     * @param MessageObj Object of specified type
     */
    public void SendMessage(MessageType Type, Object MessageObj) {
        if(gson == null)
            return;

        // Build packet string as MessageType#SerializedObject
        RouteMessage(Type.toString() + "#" + gson.toJson(MessageObj, Type.getType()));
    }

    /**
     * Send given string directly
     *
     * @param packet String to send to the server
     */
    private void RouteMessage(String packet) {
        if(send == null)
            return;

        synchronized (send) {
            send.println(packet);
        }
        System.out.println("GameController: Sent -> " + packet);
    }
}
