package Client;

import javax.jws.soap.SOAPBinding;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;

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
            this.receive = new BufferedReader(new InputStreamReader(this.connection.getInputStream()));
            this.send = new PrintWriter(this.connection.getOutputStream());
            this.listen = true;
        } catch (IOException e) {
            System.out.println("Cannot connect with the server");
            return;
        }

        this._threadInstance = new Thread(this);
        this._threadInstance.start();
    }

    /**
     * Stops current connection with the server
     */
    public void StopConnection() {
        this.listen = false;

        try {
            this.connection.close();
        } catch (IOException e) {}

    }

    @Override
    public void run() {

        // Incoming message buffer
        String incoming = "";

        // Send username to the server
        this.send.println(this.username);

        try {

            // If server doesn't respond notify the user
            if((incoming = receive.readLine()) != "OK"){
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
                    // Invoke MessageHandler on main thread

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

    private void MessageHandler(String SerializedMessage) {

    }
}
