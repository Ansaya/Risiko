package Client.Game;

import Client.Game.Connection.Serializer.ObservableUserSerializer;
import Client.Main;
import Game.Connection.Serializer.IntegerPropertySerializer;
import Game.Connection.Serializer.StringPropertySerializer;
import Client.Game.Observables.*;
import Game.Connection.*;
import Game.MessageReceiver;
import Client.Game.Connection.MessageType;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import javafx.application.Platform;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TreeItem;
import javafx.scene.layout.VBox;
import javafx.util.Builder;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Handle communication with the server
 */
public class ServerTalk extends MessageReceiver<MessageType> implements Runnable {

    private static ServerTalk _instance = new ServerTalk();

    public static ServerTalk getInstance() { return _instance; }

    private ObservableUser user;

    public ObservableUser getUser() { return this.user; }

    private volatile boolean listen;

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

    /**
     * Username associated with last chat message received
     */
    private final AtomicInteger lastSenderId = new AtomicInteger(-1);

    /**
     * ScrollPane containing chatContainer VBox
     */
    private volatile ScrollPane chatScrollable;

    /**
     * Container of chat entries
     */
    private volatile VBox chatContainer;

    /**
     * Builder for new chat entries
     */
    private Builder<Label> chatEntryBuilder;

    /**
     * Set where to add incoming chats
     *
     * @param Scrollable Scrollable parent of chat container
     * @param ToUpdate Chat container to add new chats into
     */
    public void setChatUpdate(ScrollPane Scrollable, VBox ToUpdate) {
        this.chatScrollable = Scrollable;
        this.chatContainer = ToUpdate;
        lastSenderId.set(-1);
    }

    /**
     * List of users in lobby
     */
    private volatile ObservableList<TreeItem<ObservableUser>> users;

    /**
     * Set where to add lobby users when in lobby or match users when playing
     *
     * @param ToUpdate Observable list linked to UI
     */
    public void setUsersUpdate(ObservableList<TreeItem<ObservableUser>> ToUpdate) {
        this.users = ToUpdate;
    }

    /**
     * HashMap of territories
     */
    private volatile MapHandler mapHandler;

    /**
     * Set map connected to UI
     *
     * @param Map HashMap of territories in the UI
     */
    public void setMapUpdate(MapHandler Map) {
        this.mapHandler = Map;
    }

    private final Thread _threadInstance = new Thread(this);

    private ServerTalk() {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(IntegerProperty.class, new IntegerPropertySerializer());
        gsonBuilder.registerTypeAdapter(StringProperty.class, new StringPropertySerializer());
        gsonBuilder.registerTypeAdapter(ObservableUser.class, new ObservableUserSerializer());
        gson = gsonBuilder.create();

        // Handler for incoming chat messages
        messageHandlers.put(MessageType.Chat, (message) -> {
            // Get chat object
            final Chat<ObservableUser> chat = gson.fromJson(message.Json, MessageType.Chat.getType());
            final Label sender = chatEntryBuilder.build();
            final Label text = chatEntryBuilder.build();

            sender.setText(chat.sender.username.get());
            text.setText(chat.message);

            // If message is from this client display it on opposite side of chat view
            if(chat.sender.equals(this.user)){
                sender.setAlignment(Pos.TOP_RIGHT);
                text.setAlignment(Pos.TOP_RIGHT);
            }

            if(chat.sender.color != null)
                if(chat.sender.color.get() != "") {
                    sender.setStyle("-fx-text-fill: " + chat.sender.color.get().toLowerCase());
                    text.setStyle("-fx-text-fill: " + chat.sender.color.get().toLowerCase());
                }

            // Update chat from ui thread
            Platform.runLater(() -> {
                // If message is from same sender as before, avoid to write sender again
                if(this.lastSenderId.get() != chat.sender.id.get())
                    this.chatContainer.getChildren().add(sender);

                this.lastSenderId.set(chat.sender.id.get());

                this.chatContainer.getChildren().add(text);

                // Scroll container to end
                this.chatScrollable.setVvalue(1.0f);
            });
        });

        // Handler for users in lobby
        messageHandlers.put(MessageType.Lobby, (message) -> {
            System.out.println("ServerTalk: Lobby message: " + message.Json);
            final Lobby<ObservableUser> lobbyUsers = gson.fromJson(message.Json, MessageType.Lobby.getType());

            // Update users in lobby
            Platform.runLater(() -> {
                this.users.removeIf(t -> lobbyUsers.toRemove.removeIf(tl -> tl.equals(t)));

                lobbyUsers.toAdd.forEach((u) -> {
                    if(!u.equals(this.user))
                        users.add(new TreeItem<>(u));
                });

                System.out.println("Lobby updated");
            });

        });

        // Handler for match initialization
        messageHandlers.put(MessageType.Match, (message) -> {
            System.out.println("ServerTalk: Match message: " + message.Json);
            final Match<ObservableUser> match = gson.fromJson(message.Json, MessageType.Match.getType());

            // Launch match screen
            Platform.runLater(() -> {
                Main.toMatch();
                synchronized (match){
                    match.notify();
                }
            });

            // Wait for screen to load and new user list reference to be set
            try {
                synchronized (match) {
                    match.wait();
                }
            } catch (InterruptedException e) {}

            System.out.println("ServerTalk: Match screen loaded and chat field updated.");

            // Load users in player's list
            Platform.runLater(() -> {
                match.players.forEach((u) -> {
                    if(u.equals(this.user))
                        this.user.color.set(u.color.get());

                    this.users.add(new TreeItem<>(u));
                });
            });
        });

        // Handler for positioning message
        messageHandlers.put(MessageType.Positioning, message -> {
            System.out.println("ServerTalk: Positioning message: " + message.Json);
            final Positioning pos = gson.fromJson(message.Json, MessageType.Positioning.getType());

            while (mapHandler == null){}

            final MapUpdate<ObservableTerritory> update = mapHandler.positionArmies(pos.newArmies);

            SendMessage(MessageType.MapUpdate, update);
        });

        // Handler for map updates
        messageHandlers.put(MessageType.MapUpdate, (message) -> {
            System.out.println("ServerTalk: MapUpdate message: " + message.Json);
            final MapUpdate<ObservableTerritory> update = gson.fromJson(message.Json, MessageType.MapUpdate.getType());

            Platform.runLater(() -> {
                 /* Update each territory with new information */
                update.updated.forEach((u) -> {
                    synchronized (mapHandler) {
                        ObservableTerritory t = mapHandler.getTerritories().get(u.territory);
                        t.armies.set(u.armies.get());
                        t.newArmies.set(0);
                        if (!u.getOwner().equals(t.getOwner()))
                            t.setOwner(u.getOwner());
                    }
                });
            });

        });

        // Handler for attacked territory
        messageHandlers.put(MessageType.Attack, (message) -> {
            System.out.println("ServerTalk: Defense message: " + message.Json);
            final Attack<ObservableTerritory> attack = gson.fromJson(message.Json, MessageType.Attack.getType());

            Integer defArmies = mapHandler.getTerritories().get(attack.to.territory).requestDefense(attack);

            // Send response to server
            SendMessage(MessageType.Defense, new Defense<>(attack.from, attack.to, defArmies));
        });


        // Setup builder for chat entries
        this.chatEntryBuilder = () -> {
            Label chatEntry = new Label();
            chatEntry.prefWidth(228.0f);
            chatEntry.getStyleClass().add("chat");
            chatEntry.setWrapText(true);

            return chatEntry;
        };
    }

    /**
     * Setup and start connection with the server
     *
     * @param Username Username choose from user
     */
    public void InitConnection(String Username) throws Exception {
        try {
            this.connection = new Socket("localhost", 5757);
            this.receive = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            this.send = new PrintWriter(connection.getOutputStream(), true);
            this.listen = true;
        } catch (IOException e) {
            throw new Exception("Cannot connect with the server");
        }

        // Try connecting to server
        String incoming = "";

        // Send username to the server
        this.send.println(Username);

        incoming = receive.readLine();
        System.out.println("Server responded: " + incoming);

        // If server doesn't respond notify the user
        if(!incoming.startsWith("OK"))
            throw new Exception("Wrong response from server.");

        // Set user for this client
        this.user = new ObservableUser(Integer.valueOf(incoming.split("[-]")[1]), Username, null);
        System.out.println("Got id " + user.id.get() + " from server.");

        // If connection is successfully established start listening and receiving
        this.startListen();
        this._threadInstance.start();
    }

    /**
     * Stops current connection with the server
     */
    public void StopConnection(boolean fromClient) {
        if(!listen)
            return;

        this.listen = false;

        // Send close connection notification to server
        if(fromClient)
            send.println("End");

        this.stopListen();

        // Stop thread
        try {
            this.connection.close();
            this._threadInstance.join();
        } catch (Exception e) {}

        // If finalizing exit
        if(fromClient)
            return;

        // Else reset object for new connection attempt
        _instance = new ServerTalk();

        Main.toLogin();

        // Notify user
    }

    @Override
    public void run() {

        // Incoming message buffer
        String Packet = "";

        // Listen to the server until necessary
        while (listen) {

            try {
                while ((Packet = receive.readLine()) != null){
                    if(Packet.equals("End")){
                        Platform.runLater(() -> StopConnection(false));
                        return;
                    }

                    System.out.println("ServerTalk: Received: " + Packet);

                    String[] info = Packet.split("[-]");
                    MessageType type = MessageType.valueOf(info[0]);


                    this.setIncoming(0, type, info[1]);
                }

            }catch (IOException e) {}
        }
    }

    /**
     * Send a message to the server
     *
     * @param Type Type of message
     * @param MessageObj Object of specified type
     */
    public void SendMessage(MessageType Type, Object MessageObj) {
        if(connection.isClosed()) {
            Platform.runLater(() -> StopConnection(false));
            return;
        }

        // Build packet string as MessageType-SerializedObject
        String packet = Type.toString() + "-" + gson.toJson(MessageObj, Type.getType());

        synchronized (send) {
            send.println(packet);
        }
        System.out.println("ServerTalk: Sent to server: " + packet);
    }

    /**
     * Send passed string directly
     *
     * @param packet String to send to the client
     */
    private void RouteMessage(String packet) {
        if(connection.isClosed()) {
            Platform.runLater(() -> StopConnection(false));
            return;
        }

        synchronized (send) {
            send.println(packet);
        }
        System.out.println("Sent to server: " + packet);
    }
}
