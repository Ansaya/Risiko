package Client;

import Client.Observables.ObservableTerritory;
import Client.Observables.ObservableUser;
import Game.Color;
import Game.Connection.*;
import Game.Map.Territories;
import com.google.gson.Gson;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TreeItem;
import javafx.scene.layout.VBox;
import javafx.util.Builder;
import sun.reflect.generics.tree.Tree;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

/**
 * Handle communication with the server
 */
public class ServerTalk implements Runnable {

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

    private HashMap<MessageType, Consumer<String[]>> packetHandlers = new HashMap<>();

    /**
     * Username associated with last chat message received
     */
    private int lastSenderId = -1;

    /**
     * ScrollPane containing chatContainer VBox
     */
    private ScrollPane chatScrollable;

    /**
     * Container of chat entries
     */
    private VBox chatContainer;

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
    private ObservableList<TreeItem<ObservableUser>> users;

    /**
     * Set where to add lobby users when in lobby or match users when playing
     *
     * @param ToUpdate Observable list linked to UI
     */
    public void setUsersUpdate(ObservableList<TreeItem<ObservableUser>> ToUpdate) {

        this.users = ToUpdate;
        synchronized (users) {
            users.notify();
        }
    }


    /**
     * HashMap of territories
     */
    private HashMap<Territories, ObservableTerritory> map;

    /**
     * Set map connected to UI
     *
     * @param Map HashMap of territories in the UI
     */
    public void setMapUpdate(HashMap<Territories, ObservableTerritory> Map) { this.map = Map; }

    private Thread _threadInstance;

    private ServerTalk() {

        // Handler for incoming chat messages
        packetHandlers.put(MessageType.Chat, (info) -> {
            // Get chat object
            Chat chat = gson.fromJson(info[1], Chat.class);
            Label sender = chatEntryBuilder.build();
            Label message = chatEntryBuilder.build();

            sender.setText(chat.getSender().getUsername());
            message.setText(chat.getMessage());

            // If message is from this client display it on opposite side of chat view
            if(chat.getSender().getUserId() == this.user.getUserId()){
                sender.setAlignment(Pos.TOP_RIGHT);
                message.setAlignment(Pos.TOP_RIGHT);
            }

            if(chat.getSender().getColor() != null) {
                sender.setStyle("-fx-text-fill: " + chat.getSender().getColor().toString().toLowerCase());
                message.setStyle("-fx-text-fill: " + chat.getSender().getColor().toString().toLowerCase());
            }

            // Update chat from ui thread
            Platform.runLater(() -> {
                // If message is from same sender as before, avoid to write sender again
                if(this.lastSenderId != chat.getSender().getUserId())
                    this.chatContainer.getChildren().add(sender);

                this.chatContainer.getChildren().add(message);

                // Scroll container to end
                this.chatScrollable.setVvalue(1.0f);

                this.lastSenderId = chat.getSender().getUserId();
            });
        });

        // Handler for users in lobby
        packetHandlers.put(MessageType.Lobby, (info) -> {
            System.out.println("Lobby message: " + info[1]);
            Lobby lobbyUsers = gson.fromJson(info[1], Lobby.class);

            // Update users in lobby
            Platform.runLater(() -> {
                users.removeIf((t) -> {
                    for (User u: lobbyUsers.getToRemove()
                            ) {
                        return t.getValue().UserId.get() == u.getUserId();
                    }

                    return false;
                });

                lobbyUsers.getToAdd().forEach((u) -> users.add(new TreeItem<>(new ObservableUser(u))));

                System.out.println("Lobby updated");
            });

        });

        // Handler for match initialization
        packetHandlers.put(MessageType.Match, (info) -> {
            System.out.println("Match message: " + info[1]);
            Match match = gson.fromJson(info[1], Match.class);

            // Launch match screen
            Platform.runLater(() -> Main.toMatch());

            // Wait for screen to load and new user list reference to be set
            try {
                synchronized (users) {
                    users.wait();
                }
            } catch (InterruptedException e) {}

            // Load users in player's list
            Platform.runLater(() -> {
                for (User u: match.getPlayers()) {
                    if(u.getUserId() == this.user.getUserId())
                        this.user.setColor(u.getColor());

                    this.users.add(new TreeItem<>(new ObservableUser(u)));
                }
            });
        });

        // Handler for map updates
        packetHandlers.put(MessageType.MapUpdate, (info) -> {
            System.out.println("MapUpdate message: " + info[1]);
            MapUpdate update = gson.fromJson(info[1], MapUpdate.class);

            Platform.runLater(() -> {
                 /* Update each territory with new information */
                update.getUpdated().forEach((u) -> {
                    ObservableTerritory t = map.get(u.getTerritory());
                    t.Armies.set(u.getArmies());
                    if(t.getOwner().getUserId() != u.getOwner().getId())
                        t.setOwner(new User(u.getOwner()));
                });
            });

        });

        // Handler for attacked territory
        packetHandlers.put(MessageType.Attack, (info) -> {
            System.out.println("Defense message: " + info[1]);
            Attack attack = gson.fromJson(info[1], Attack.class);

            // Message shown to the user
            String popupInfo = "Player " + attack.getFrom().getOwner().getName() + " is attacking from " + attack.getFrom().toString() + " with " + attack.getArmies() +
                    " armies to your " + attack.getTo().toString() + "\r\nChoose how many armies do you want to defend with.";

            Integer defArmies = null;

            // Require to user number of defending armies to be used
            try {
                defArmies = map.get(attack.getTo().getTerritory()).requestDefense(popupInfo);
            } catch (InterruptedException e) {
                System.out.println("From defense message handler.");
                e.printStackTrace();
            }

            if(defArmies == null)
                System.out.println("From defense message handler: Null problem");

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

        // If connection is successfully established start listening
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

                    System.out.println("Received: " + Packet);

                    String[] info = Packet.split("[-]");
                    MessageType type = MessageType.valueOf(info[0]);


                    if(packetHandlers.containsKey(type))
                        new Thread(() -> packetHandlers.get(type).accept(info)).start();
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
        System.out.println("Sent to server: " + packet);
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
