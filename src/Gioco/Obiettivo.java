package Gioco;

import java.util.Random;

/**
 * Created by fiore on 10/10/2016.
 */
public enum Obiettivo {
    EuropaAustraliaContinente,
    EuropaSudAmericaContinente,
    NordAmericaAfrica,
    NordAmericaAustralia,
    AsiaSudAmerica,
    AsiaAfrica,
    Territori24,
    Territori18Due,
    DistruggiArmata;

    private static Obiettivo[] correnti = Obiettivo.values();

    /**
     * Restituisce un obiettivo da assegnare a un giocatore per la partita
     *
     * @param restart Indica se utilizzare una nuova lista oppure continuare l'assegnazione corrente
     * @return Obiettivo dalla lista
     */
    public Obiettivo next(boolean restart) {
        if(restart)
            correnti = Obiettivo.values();

        Random r = new Random();
        int index = r.nextInt(9);
        if(correnti[index] != null) {
            correnti[index] = null;
            return Obiettivo.values()[index];
        }

        return next(false);
    }

    public boolean Completato(Giocatore Giocatore) {
        switch (this) {
            case EuropaAustraliaContinente:

                break;
            case EuropaSudAmericaContinente:

                break;
            case NordAmericaAfrica:

                break;
            case NordAmericaAustralia:

                break;
            case AsiaSudAmerica:

                break;
            case AsiaAfrica:

                break;
            case Territori24:
                return Giocatore.getTerritori().size() >= 24;
            case Territori18Due:
                boolean completato = false;
                for (Territorio t:Giocatore.getTerritori()
                     ) {
                    t.
                }
                break;
            case DistruggiArmata:

                break;
            default:

                break;
        }
    }
}
