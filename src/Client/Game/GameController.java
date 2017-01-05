package Client.Game;

import Client.Game.Connection.Serializer.ObservableUserSerializer;
import Client.Main;
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
import java.util.function.Consumer;

/**
 * Handle communication with the server
 */
public class GameController extends MessageReceiver<MessageType> implements Runnable {

    private static GameController _instance = new GameController();

    public static GameController getInstance() { return _instance; }

    private final String serverAddress = "localhost";

    /* Connection section */
    /**
     * Connection To the server
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

    private volatile Consumer<Chat<ObservableUser>> addChatEntry = null;

    /**
     * Set method reference to add new message to chatbos in UI
     *
     * @param AddChatEntry Method reference
     */
    public void setChatEntry(Consumer<Chat<ObservableUser>> AddChatEntry) { addChatEntry = AddChatEntry; }

    /* Chat section end */

    /* User section */
    /**
     * Current user
     */
    private ObservableUser user;

    public ObservableUser getUser() { return this.user; }

    private volatile Consumer<Lobby<ObservableUser>> updateUsers = null;

    /**
     * Set method reference to update users list in UI
     *
     * @param UpdateUsers Method reference
     */
    public void setUpdateUsers(Consumer<Lobby<ObservableUser>> UpdateUsers) { updateUsers = UpdateUsers; }

    /* User section end */

    /* UI Handlers */
    private volatile boolean inMatch = false;

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
    private GameController() {
        super("GameController");

        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(IntegerProperty.class, new IntegerPropertySerializer());
        gsonBuilder.registerTypeAdapter(StringProperty.class, new StringPropertySerializer());
        gsonBuilder.registerTypeAdapter(ObservableUser.class, new ObservableUserSerializer());
        gson = gsonBuilder.create();

        // Handler for incoming chat messages
        messageHandlers.put(MessageType.Chat, (message) -> {
            final Chat<ObservableUser> chat = gson.fromJson(message.Json, MessageType.Chat.getType());
            addChatEntry.accept(chat);
        });

        // Handler for userList in lobby
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
            System.out.println("GameController: Positioning message: " + message.Json);
            final Positioning pos = gson.fromJson(message.Json, MessageType.Positioning.getType());

            SendMessage(MessageType.MapUpdate, mapHandler.positionArmies(pos.newArmies));
        });

        messageHandlers.put(MessageType.Mission, (message) -> {
            final Mission mission = gson.fromJson(message.Json, MessageType.Mission.getType());
            mapHandler.setMission(mission.Mission);
        });

        // Handler for map updates
        messageHandlers.put(MessageType.MapUpdate, (message) -> {
            System.out.println("GameController: MapUpdate message: " + message.Json);
            mapHandler.updateMap(gson.fromJson(message.Json, MessageType.MapUpdate.getType()));
        });

        // Handler for card messages
        messageHandlers.put(MessageType.Cards, (message) -> {
            final Cards cards = gson.fromJson(message.Json, MessageType.Cards.getType());

            // If message is not empty add the card To user's list
            if(!cards.combination.isEmpty()) {
                // Add card To user local cards
                cardsHandler.addCard(cards.combination.get(0));

                // Notify user
                Main.showDialog("Territories cards",
                                  "You received " + cards.combination.get(0).toString() + " card!",
                                "Continue");
                return;
            }

            // Else ask user To play a combination of cards
            // Return response To server
            SendMessage(MessageType.Cards, cardsHandler.requestCombination());
        });

        // Handler for attacked territory
        messageHandlers.put(MessageType.Battle, (message) -> {
            System.out.println("GameController: Battle message: " + message.Json);
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

        // Handler for special move
        messageHandlers.put(MessageType.SpecialMoving, (message) -> {
            System.out.println("GameController: Special moving message: " + message.Json);
            // Request user To move Armies and send response to server
            SendMessage(MessageType.SpecialMoving, mapHandler.specialMoving(gson.fromJson(message.Json, MessageType.SpecialMoving.getType())));
        });

        // Handler for game state changes
        messageHandlers.put(MessageType.GameState, (message) -> {
            stopExecutor();
            System.out.println("GameController: GameState message: " + message.Json);
            // If a GameState message is received than match is no longer valid, so go back To lobby
            Main.toLobby();
            user.territories.set(0);
            user.color = null;
            mapHandler = null;
            cardsHandler = null;

           final GameState<ObservableUser> gameState = gson.fromJson(message.Json, MessageType.GameState.getType());

           switch (gameState.state){
               case Winner:
                   if(gameState.winner.equals(user)) {
                       Main.showDialog("Game state message", "You won the match!", "Close");
                       Sounds.Victory.play();
                   }
                   else {
                       Main.showDialog("Game state message",
                               "You lost this match. The winner is " + gameState.winner.username.get(),
                               "Close");
                   }
                   break;
               case Abandoned:
                   Main.showDialog("Game state message", "Player " + gameState.winner.username.get() + " left the game.", "Close");
                   break;
           }
        });
    }

    /**
     * Setup and start connection with the server
     *
     * @param Username Username choose From user
     */
    public void InitConnection(String Username) throws Exception {
        try {
            this.connection = new Socket(serverAddress, 5757);
            this.receive = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            this.send = new PrintWriter(connection.getOutputStream(), true);
            this.listen = true;
        } catch (IOException e) {
            throw new Exception("Cannot connect with the server");
        }

        // Try connecting To server
        // Send username To the server
        this.send.println(Username);

        String incoming = receive.readLine();
        System.out.println("Server responded: " + incoming);

        // If server doesn't respond notify the user
        if(!incoming.startsWith("OK"))
            throw new Exception("Wrong response from server.");

        // Set user for this client
        this.user = new ObservableUser(Integer.valueOf(incoming.split("[#]")[1]), Username, null);
        System.out.println("Got id " + user.id.get() + " from server.");

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

        // Send close connection notification To server
        if(fromClient)
            send.println("End");

        stopExecutor();

        // Stop thread
        try {
            connection.close();
            _threadInstance.join();
        } catch (Exception e) {}

        // If finalizing exit
        if(fromClient) {
            System.out.println("Client finalized");
            return;
        }

        // Else reset object for new connection attempt
        _instance = new GameController();

        Main.toLogin();

        Main.showDialog("Application error", "There has been a problem with server connection.", "Close");
    }

    /**
     * Leave current match and go back To lobby
     */
    public void AbortMatch() {
        // If player is not in a match return
        if(!inMatch)
            return;

        SendMessage(MessageType.GameState, new GameState<>(StateType.Abandoned, user));
        Main.toLobby();
    }

    @Override
    public void run() {

        // Incoming message buffer
        String Packet;

        // Listen To the server until necessary
        while (listen) {

            try {
                while ((Packet = receive.readLine()) != null){
                    if(Packet.equals("End")){
                        Platform.runLater(() -> StopConnection(false));
                        return;
                    }

                    System.out.println("GameController: Received <- " + Packet);

                    String[] info = Packet.split("[#]");

                    this.setIncoming(0, MessageType.valueOf(info[0]), info[1]);
                }

            }catch (Exception e) {
                System.err.println("GamerController: Server connection lost");
                break;
            }
        }

        if(listen)
            Platform.runLater(() -> StopConnection(false));
    }

    public void SendChat(String Text) {
        SendMessage(MessageType.Chat, new Chat<>(user, Text));
    }

    /**
     * Send a message To the server
     *
     * @param Type Type of message
     * @param MessageObj Object of specified type
     */
    public void SendMessage(MessageType Type, Object MessageObj) {

        // Build packet string as MessageType#SerializedObject
        String packet = Type.toString() + "#" + gson.toJson(MessageObj, Type.getType());

        synchronized (send) {
            send.println(packet);
        }
        System.out.println("GameController: Sent -> " + packet);
    }

    /**
     * Send passed string directly
     *
     * @param packet String To send To the client
     */
    private void RouteMessage(String packet) {

        synchronized (send) {
            send.println(packet);
        }
        System.out.println("Sent To server: " + packet);
    }
}
