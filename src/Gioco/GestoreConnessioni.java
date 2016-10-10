package Gioco;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

/**
 * Created by fiore on 10/10/2016.
 */
public class GestoreConnessioni implements Runnable {
    private ServerSocket server;

    private Thread _istanza = null;

    private ArrayList<Socket> connessioni = new ArrayList<>();

    public GestoreConnessioni(int porta) {
        try {
            this.server = new ServerSocket(porta);
        }
        catch (IOException e){
            System.out.println("Impossibile creare la connessione");
        }
    }

    public void Ascolta() {
        if(_istanza != null){
            try {
                server.close();
                _istanza.join();
                _istanza = null;
            } catch (Exception e) {}
        }
        _istanza = new Thread(this);
        _istanza.start();
    }

    @Override
    public void run() {
        while (true) {
            try {
                System.out.println("In attesa di utenti...");
                Socket nuovo = server.accept();
                System.out.println("Nuovo utente connesso.");
                connessioni.add(nuovo);
                GameController.getInstance().addUtente(nuovo);
                System.out.println("Utente passato al game controller.");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
