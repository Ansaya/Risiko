package Gioco;

import java.net.Socket;
import java.util.ArrayList;

/**
 * Created by fiore on 10/10/2016.
 */
public class GameController {

    private static GameController _istanza = new GameController();

    public static GameController getInstance() { return _istanza; }

    private ArrayList<Partita> partite = new ArrayList<>();

    private ArrayList<Utente> lobby = new ArrayList<>();

    public GameController() {

    }

    public void addUtente(Socket Connessione) {
        lobby.add(new Utente(Connessione));
    }
}
