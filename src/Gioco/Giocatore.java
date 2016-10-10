package Gioco;

import java.util.ArrayList;
import java.util.StringJoiner;

/**
 * Giocatore relativo ad una partita in corso
 */
public class Giocatore {

    /**
     * Id del giocatore nella partita
     */
    private int id;

    public int getId() { return id; }

    private int idPartita;

    public int getIdPartita() {return idPartita; }

    /**
     * Utente collegato al giocatore della partita
     */
    private Utente utente;

    /**
     * Nome scelto dall'utente durante la connessione
     * @return Nome dell'utente
     */
    public String getNome() { return utente.getNome(); }

    private Colore colore;

    public Colore getColore() { return colore; }

    /**
     * Territori attualmente controllati dal giocatore
     */
    private ArrayList<Territorio> territori = new ArrayList<>();

    /**
     * Lista di territori controllati dal giocatore
     * @return Lista dei territori
     */
    public ArrayList<Territorio> getTerritori() { return (ArrayList<Territorio>)territori.clone(); }

    /**
     * Obiettivo del giocatore per vincere la partita
     */
    private Obiettivo obiettivo;

    /**
     * Obiettivo del giocatore
     * @return Obiettivo del giocatore
     */
    public Obiettivo getObiettivo() { return obiettivo; }

    /**
     * Armate del giocatore ancora da disporre
     */
    private int armate = 0;

    /**
     * Armate che il giocatere può ancora disporre sul campo
     * @return Numero delle armate disponibili
     */
    public int getArmate() { return armate; }

    private static int counter = 0;

    public Giocatore(Utente Utente, Colore Colore, int IdPartita) {
        this.id = counter++;
        this.idPartita = IdPartita;
        this.utente = Utente;
        this.colore = Colore;
    }
}