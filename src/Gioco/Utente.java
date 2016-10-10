package Gioco;

import java.net.Socket;

/**
 * Created by fiore on 10/10/2016.
 */
public class Utente implements Runnable {

    private int id;

    private String nome;

    public String getNome() { return nome;}

    private Socket connessione;

    private Thread _istanza;

    private static int counter = 0;

    public Utente(Socket Connessione) {
        this.id = counter++;
        this.connessione = Connessione;

        this._istanza = new Thread(this);
        _istanza.start();
    }

    @Override
    public void run() {
        // Il primo messaggio contiene il nome scelto dall'utente
        // Imposta il nome utente

        while (true) {

        }
    }
}
