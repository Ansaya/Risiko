package Gioco;

import java.util.ArrayList;

/**
 * Rappresenta una partita. Gestisce lo svolgimento del gioco dall'inizio alla fine tramite un thread dedicato
 */
public class Partita implements Runnable {

    /**
     * Id della partita
     */
    private int id;

    /**
     * Restituisce l'id della partita
     *
     * @return Id della partita
     */
    public int getId() { return id; }

    /**
     * Lista dei giocatori impegnati nella partita
     */
    private ArrayList<Giocatore> giocatori = new ArrayList<>();

    /**
     * Restituisce la lista dei giocatori in partita
     *
     * @return Lista dei giocatori
     */
    public ArrayList<Giocatore> getGiocatori() { return giocatori; }

    /**
     * Thread che gestisce la partita
     */
    private Thread _instance;

    /**
     * Contatore generale delle partite create
     */
    private static int counter = 0;

    public Partita(Utente... Utenti) {
        if(Utenti.length < 2 || Utenti.length > 6)
            throw new UnsupportedOperationException(String.format("Non è possibile giocare in %d", Utenti.length));

        this.id = counter++;

        Colore.reset();

        for (Utente u: Utenti
             ) {
            giocatori.add(new Giocatore(u, Colore.next(), this.id));
        }

        this._instance = new Thread(this);
        _instance.start();
    }

    @Override
    public void run() {

    }
}
