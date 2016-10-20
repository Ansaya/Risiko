package Client;

import Client.Observables.ObservableUser;
import Game.Connection.Chat;
import Game.Connection.Lobby;
import Game.Connection.MessageType;
import Game.Connection.User;
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

    private int id;

    public int getId() { return this.id; }

    /**
     * Username of this client
     */
    private String username;

    public String getUsername() { return this.username; }

    private volatile boolean listen;

    /**
     * Connection to the server
     */
    private Socket connection;

    /**
     * Incoming strem
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
    private String lastSender = "";

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
    private ObservableList<TreeItem<ObservableUser>> lobbyUsers;

    /**
     * Set where to add lobby users
     *
     * @param ToUpdate Observable list linked to UI
     */
    public void setLobbyUpdate(ObservableList<TreeItem<ObservableUser>> ToUpdate) {
        this.lobbyUsers = ToUpdate;
    }

    private Thread _threadInstance;

    private ServerTalk() {

        // Handler for incoming chat messages
        packetHandlers.put(MessageType.Chat, (info) -> {
            // Get chat object
            Chat chat = gson.fromJson(info[1], Chat.class);
            Label sender = chatEntryBuilder.build();
            Label message = chatEntryBuilder.build();

            sender.setText(chat.getSender());
            message.setText(chat.getMessage());

            // If message is from this client display it on opposite side of chat view
            if(chat.getSender().equals(this.username)){
                sender.setAlignment(Pos.TOP_RIGHT);
                message.setAlignment(Pos.TOP_RIGHT);
            }

            // Update chat from ui thread
            Platform.runLater(() -> {
                // If message is from same sender as before, avoid to write sender again
                if(!this.lastSender.equals(chat.getSender()))
                    this.chatContainer.getChildren().add(sender);

                this.chatContainer.getChildren().add(message);

                // Scroll container to end
                this.chatScrollable.setVvalue(1.0f);

                this.lastSender = chat.getSender();
            });
        });

        // Handler for users in lobby
        packetHandlers.put(MessageType.Lobby, (info) -> {
            System.out.println("Lobby message: " + info[1]);
            Lobby users = gson.fromJson(info[1], Lobby.class);

            // Update users in lobby
            Platform.runLater(() -> {
                lobbyUsers.removeIf((t) -> {
                    for (User u: users.getToRemove()
                            ) {
                        return t.getValue().UserId.getValue().equals(String.valueOf(u.getUserId()));
                    }

                    return false;
                });

                users.getToAdd().forEach((u) -> lobbyUsers.add(new TreeItem<>(new ObservableUser(u))));

                System.out.println("Lobby updated");
            });

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
        this.username = Username;

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
        this.send.println(this.username);

        incoming = receive.readLine();
        System.out.println("Server responded: " + incoming);

        // If server doesn't respond notify the user
        if(!incoming.startsWith("OK"))
            throw new Exception("Wrong response from server.");

        this.id = Integer.valueOf(incoming.split("[-]")[1]);
        System.out.println("Got id " + id + " from server.");

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

        Main.toLogin.run();

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
                        packetHandlers.get(type).accept(info);
                }

            }catch (IOException e) {}
        }
    }

    /**
     * Send a message to the client
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

        send.println(packet);
        System.out.println("Sent to server: " + packet);
    }

    /**
     * Send passed string directly
     *
     * @param packet String to send to the client
     */
    public void RouteMessage(String packet) {
        if(connection.isClosed()) {
            Platform.runLater(() -> StopConnection(false));
            return;
        }

        send.println(packet);
        System.out.println("Sent to server: " + packet);
    }
}
