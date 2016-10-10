package Gioco;

import java.net.Socket;
import java.util.ArrayList;

/**
 * Created by fiore on 10/10/2016.
 */
public class GameController {

    private static GameController _istanza = new GameController();

    public static GameController getInstance() { return _istanza; }

    /**
     * Lista delle partite in corso
     */
    private ArrayList<Partita> partite = new ArrayList<>();

    /**
     * Restituisce la lista delle partite attualmente in corso
     *
     * @return Lista di partite
     */
    public ArrayList<Partita> getPartite() { return partite; }

    /**
     * Restituisce la partita con l'id richiesto
     *
     * @param IdPartita Id della partita richiesta
     * @return Partita con l'id cercato
     */
    public Partita getPartita(int IdPartita) {
        for (Partita p: partite
             ) {
            if(p.getId() == IdPartita)
                return p;
        }

        return null;
    }

    private ArrayList<Utente> lobby = new ArrayList<>();

    public GameController() {

    }

    public void addUtente(Socket Connessione) {
        lobby.add(new Utente(Connessione));
    }
}
