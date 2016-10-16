package Client;

import Game.Connection.Chat;
import Game.Connection.MessageType;
import com.google.gson.Gson;
import javafx.application.Platform;
import javafx.scene.control.TextArea;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * Handle communication with the server
 */
public class ServerTalk implements Runnable {

    private static ServerTalk _instance = new ServerTalk();

    public static ServerTalk getInstance() { return _instance; }

    private String username;

    public String getUsername() { return this.username; }

    private volatile boolean listen;

    private Socket connection;

    private BufferedReader receive;

    private PrintWriter send;

    private TextArea chat;

    private Thread _threadInstance;

    private ServerTalk() {
        
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

    public void updateHere(javafx.scene.control.TextArea ToUpdate) {
        this.chat = ToUpdate;
    }

    /**
     * Stops current connection with the server
     */
    public void StopConnection() {
        this.listen = false;

        try {
            this.connection.close();
            this._threadInstance.join();
        } catch (Exception e) {}
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

        // Dispose all at the end of communication
        try {
            this.send = null;
            this.receive = null;

            this.connection.close();
            this.connection = null;
        }catch (IOException e) {}
    }

    private void MessageHandler(String Packet) {
        if(Packet.equals("End")){
            this.StopConnection();
            // Notify user
            return;
        }

        String[] info = Packet.split("[-]");
        MessageType type = MessageType.valueOf(info[0]);

        Gson deserialize = new Gson();

        switch (type) {
            case Chat:
                // Get chat object
                Chat message = deserialize.fromJson(info[1], Chat.class);

                // Update chat text area from ui thread
                Platform.runLater(() -> this.chat.appendText(message.getSender() + ": " + message.getMessage() + "\r\n"));
                break;
            case MapUpdate:
                break;
            default:
                break;
        }
    }

    /**
     * Send a message to the client
     *
     * @param Type Type of message
     * @param MessageObj Object of specified type
     */
    public void SendMessage(MessageType Type, Object MessageObj) {
        Gson serialize = new Gson();

        // Build packet string as MessageType-SerializedObject
        String packet = Type.toString() + "-" + serialize.toJson(MessageObj);

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
