package Client;

import Client.Observables.MapHandler;
import Client.Observables.ObservableTerritory;
import Client.Observables.ObservableUser;
import Game.Connection.*;
import Game.MessageReceiver;
import com.google.gson.Gson;
import javafx.application.Platform;
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
import java.util.concurrent.locks.Lock;

/**
 * Handle communication with the server
 */
public class ServerTalk extends MessageReceiver implements Runnable {

    private static ServerTalk _instance = new ServerTalk();

    public static ServerTalk getInstance() { return _instance; }

    private User user;

    public User getUser() { return this.user; }

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

    private Gson gson = new Gson();

    /**
     * Username associated with last chat message received
     */
    private int lastSenderId = -1;

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

    private Thread _threadInstance;

    private ServerTalk() {

        // Handler for incoming chat messages
        messageHandlers.put(MessageType.Chat, (message) -> {
            // Get chat object
            Chat chat = gson.fromJson(message.Json, Chat.class);
            Label sender = chatEntryBuilder.build();
            Label text = chatEntryBuilder.build();

            sender.setText(chat.getSender().getUsername());
            text.setText(chat.getMessage());

            // If message is from this client display it on opposite side of chat view
            if(chat.getSender().getUserId() == this.user.getUserId()){
                sender.setAlignment(Pos.TOP_RIGHT);
                text.setAlignment(Pos.TOP_RIGHT);
            }

            if(chat.getSender().getColor() != null) {
                sender.setStyle("-fx-text-fill: " + chat.getSender().getColor().toString().toLowerCase());
                text.setStyle("-fx-text-fill: " + chat.getSender().getColor().toString().toLowerCase());
            }

            // Update chat from ui thread
            Platform.runLater(() -> {
                // If message is from same sender as before, avoid to write sender again
                if(this.lastSenderId != chat.getSender().getUserId())
                    this.chatContainer.getChildren().add(sender);

                this.chatContainer.getChildren().add(text);

                // Scroll container to end
                this.chatScrollable.setVvalue(1.0f);

                this.lastSenderId = chat.getSender().getUserId();
            });
        });

        // Handler for users in lobby
        messageHandlers.put(MessageType.Lobby, (message) -> {
            System.out.println("ServerTalk: Lobby message: " + message.Json);
            Lobby lobbyUsers = gson.fromJson(message.Json, Lobby.class);

            // Update users in lobby
            Platform.runLater(() -> {
                this.users.removeIf((t) -> {
                    for (User u: lobbyUsers.getToRemove()
                            ) {
                        return t.getValue().UserId.get() == u.getUserId();
                    }

                    return false;
                });

                lobbyUsers.getToAdd().forEach((u) -> {
                    if(u.getUserId() != user.getUserId())
                        users.add(new TreeItem<>(new ObservableUser(u)));
                });

                System.out.println("Lobby updated");
            });

        });

        // Handler for match initialization
        messageHandlers.put(MessageType.Match, (message) -> {
            System.out.println("ServerTalk: Match message: " + message.Json);
            Match match = gson.fromJson(message.Json, Match.class);

            final Object o = new Object();

            // Launch match screen
            Platform.runLater(() -> {
                Main.toMatch();
                synchronized (o){
                    o.notify();
                }
            });

            // Wait for screen to load and new user list reference to be set
            try {
                synchronized (o) {
                    o.wait();
                }
            } catch (InterruptedException e) {}

            System.out.println("ServerTalk: Match screen loaded and chat field updated.");

            // Load users in player's list
            Platform.runLater(() -> {
                for (User u: match.getPlayers()) {
                    if(u.getUserId() == this.user.getUserId())
                        this.user.setColor(u.getColor());

                    this.users.add(new TreeItem<>(new ObservableUser(u)));
                }
            });
        });

        // Handler for positioning message
        messageHandlers.put(MessageType.Positioning, message -> {
            System.out.println("ServerTalk: Positioning message: " + message.Json);
            Positioning pos = gson.fromJson(message.Json, Positioning.class);

            MapUpdate update = mapHandler.positionArmies(pos.getNewArmies());

            SendMessage(MessageType.MapUpdate, update);
        });

        // Handler for map updates
        messageHandlers.put(MessageType.MapUpdate, (message) -> {
            System.out.println("ServerTalk: MapUpdate message: " + message.Json);
            MapUpdate update = gson.fromJson(message.Json, MapUpdate.class);

            Platform.runLater(() -> {
                 /* Update each territory with new information */
                update.getUpdated().forEach((u) -> {
                    synchronized (mapHandler) {
                        ObservableTerritory t = mapHandler.getTerritories().get(u.getTerritory());
                        t.Armies.set(u.getArmies());
                        if (t.getOwner().getUserId() != u.getOwner().getId())
                            t.setOwner(new User(u.getOwner()));
                    }
                });
            });

        });

        // Handler for attacked territory
        messageHandlers.put(MessageType.Attack, (message) -> {
            System.out.println("ServerTalk: Defense message: " + message.Json);
            Attack attack = gson.fromJson(message.Json, Attack.class);

            Integer defArmies = null;

            // Require to user number of defending armies to be used
            try {
                defArmies = mapHandler.getTerritories().get(attack.getTo().getTerritory()).requestDefense(attack);
            } catch (InterruptedException e) {
                System.out.println("ServerTalk: Error from defense message handler.");
                e.printStackTrace();
            }

            if(defArmies == null)
                System.out.println("ServerTalk: Error from defense message handler: Null problem");

            // Send response to server
            SendMessage(MessageType.Defense, new Defense(attack.getFrom(), attack.getTo(), defArmies));
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
        this.user = new User(Integer.valueOf(incoming.split("[-]")[1]), Username, null);
        System.out.println("Got id " + user.getUserId() + " from server.");

        // If connection is successfully established start listening and receiving
        this.startListen();
        this._threadInstance = new Thread(this);
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
        String packet = Type.toString() + "-" + gson.toJson(MessageObj);

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
