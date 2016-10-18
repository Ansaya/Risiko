package Client;

import Game.Connection.Chat;
import Game.Connection.MessageType;
import com.google.gson.Gson;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import javafx.util.Builder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashMap;
import java.util.function.Consumer;

/**
 * Handle communication with the server
 */
public class ServerTalk implements Runnable {

    private static ServerTalk _instance = new ServerTalk();

    public static ServerTalk getInstance() { return _instance; }

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

    private Thread _threadInstance;

    private ServerTalk() {

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
    public void InitConnection(String Username) {
        this.username = Username;

        try {
            this.connection = new Socket("localhost", 5757);
            this.receive = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            this.send = new PrintWriter(connection.getOutputStream(), true);
            this.listen = true;
        } catch (IOException e) {
            System.out.println("Cannot connect with the server");
            return;
        }

        this._threadInstance = new Thread(this);
        this._threadInstance.start();
    }

    /**
     * Set where to add incoming chats
     *
     * @param Scrollable Scrollable parent of chat container
     * @param ToUpdate Chat container to add new chats into
     */
    public void updateHere(ScrollPane Scrollable, VBox ToUpdate) {

        this.chatScrollable = Scrollable;
        this.chatContainer = ToUpdate;
    }

    /**
     * Stops current connection with the server
     */
    public void StopConnection(boolean exit) {
        this.listen = false;

        // Send close connection notification to server
        send.println("End");

        // Stop thread
        try {
            this.connection.close();
            this._threadInstance.join();
        } catch (Exception e) {}

        // If finalizing exit
        if(exit)
            return;

        // Else reset object for new connection attempt
        _instance = new ServerTalk();

        Main.toLogin.run();

        // Notify user
    }

    @Override
    public void run() {

        // Incoming message buffer
        String incoming = "";

        // Send username to the server
        this.send.println(this.username);

        try {
            incoming = receive.readLine();

            // If server doesn't respond notify the user
            if(!incoming.equals("OK")){
                // Call user interface and print connection error
                return;
            }
        }catch(IOException e) {
            e.printStackTrace();
            return;
        }

        // Listen to the server until necessary
        while (listen) {

            try {
                while ((incoming = receive.readLine()) != null){
                    MessageHandler(incoming);
                }

            }catch (IOException e) {}
        }
    }

    /**
     * Handle incoming messages to update UI and notify user for events
     *
     * @param Packet Received packet
     */
    private void MessageHandler(String Packet) {
        if(Packet.equals("End")){
            Platform.runLater(() -> StopConnection(false));
            return;
        }

        String[] info = Packet.split("[-]");
        MessageType type = MessageType.valueOf(info[0]);


        if(packetHandlers.containsKey(type))
            packetHandlers.get(type).accept(info);
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
    }

    /**
     * Send passed string directly
     *
     * @param packet String to send to the client
     */
    public void RouteMessage(String packet) {
        send.println(packet);
    }
}
