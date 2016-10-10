package Gioco;

import java.util.ArrayList;
import java.util.StringJoiner;

/**
 * Created by fiore on 10/10/2016.
 */
public class Giocatore {

    private Utente utente;

    public String getNome() { return utente.getNome(); }

    private ArrayList<Territorio> territori = new ArrayList<>();

    public ArrayList<Territorio> getTerritori() { return (ArrayList<Territorio>)territori.clone(); }

    private Obiettivo obiettivo;

    public Obiettivo getObiettivo() { return obiettivo; }

    public Giocatore(Utente Utente) {
        this.utente = Utente;
    }
}