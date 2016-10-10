package Gioco;

import java.util.ArrayList;
import java.util.Random;

import static Gioco.Continente.*;

/**
 * Lista di obiettivi relativi alla mappa mondiale
 */
public enum Obiettivo {
    EuropaAustraliaContinente(""),
    EuropaSudAmericaContinente(""),
    NordAmericaAfrica(""),
    NordAmericaAustralia(""),
    AsiaSudAmerica(""),
    AsiaAfrica(""),
    Territori24(""),
    Territori18Due(""),
    DistruggiArmataROSSO(""),
    DistruggiArmataGIALLO(""),
    DistruggiArmataVERDE(""),
    DistruggiArmataBLU(""),
    DistruggiArmataNERO(""),
    DistruggiArmataROSA("");

    private static Obiettivo[] correnti = Obiettivo.values();

    private String descrizione;

    public String getDescrizione() { return descrizione; }

    Obiettivo(String Descrizione) {
        this.descrizione = Descrizione;
    }


    /**
     * Restituisce un obiettivo da assegnare a un giocatore per la partita
     *
     * @return Obiettivo dalla lista
     */
    public Obiettivo next() {
        Random r = new Random();
        int index = r.nextInt(9);
        if(correnti[index] != null) {
            correnti[index] = null;
            return Obiettivo.values()[index];
        }

        return next();
    }

    /**
     * Reimposta la distribuzione degli obiettivi
     */
    public void restart() {
        correnti = Obiettivo.values();
    }

    /**
     * Controlla se il giocatore ha raggiunto l'obiettivo assegnato per la vittoria
     *
     * @param Giocatore Giocatore per il quale controllare l'obiettivo
     * @return Vero se l'obiettiov è completato, falso altrimenti
     */
    public boolean Completato(Giocatore Giocatore) {
        String obiettivo = this.name();

        // Se l'obiettivo è distruggere un'armate cerco tra i giocatori se esiste ancora il colore
        // I problemi di armata distrutta da un altro giocatore o colore uguale a quello del giocatere sono già gestiti da game controller e scontro
        if(obiettivo.contains("DistruggiArmata")){
            Colore colore = Colore.valueOf(obiettivo.substring(15));
            Partita partita = GameController.getInstance().getPartita(Giocatore.getIdPartita());
            ArrayList<Giocatore> giocatori = partita.getGiocatori();

            for (Giocatore g: giocatori
                 ) {
                if(g.getColore() == colore)
                    return false;
            }

            return true;
        }

        if(obiettivo.contains("Territori")){
            // Rilevo il numero di territori
            int numero = Integer.valueOf(obiettivo.substring(9, 10));

            // Acquisisco i territori del giocatore
            ArrayList<Territorio> territori = Giocatore.getTerritori();

            if(numero == 24)
                return territori.size() >= 24;

            if(territori.size() < 18)
                return false;

            // Se il numero è 18 devo avere due armate per ogni territorio
            for (Territorio t: territori
                 ) {
                if(t.getArmate() < 2)
                    return false;
            }
            return true;
        }

        ArrayList<Continente> controllati = Continente.continentiControllati(Giocatore.getTerritori());
        if (controllati.size() > 1)
            switch (this) {
                case NordAmericaAfrica:
                    if(controllati.contains(NordAmerica) &&controllati.contains(Africa))
                            return true;
                    break;
                case NordAmericaAustralia:
                    if(controllati.contains(NordAmerica) && controllati.contains(Australia))
                        return true;
                    break;
                case AsiaAfrica:
                    if (controllati.contains(Asia) && controllati.contains(Africa))
                        return true;
                    break;
                case AsiaSudAmerica:
                    if (controllati.contains(Asia) && controllati.contains(SudAmerica))
                        return true;
                    break;
                default:
                    break;
            }
        else
            return false;

        if (controllati.size() > 2)
            switch (this) {
                case EuropaAustraliaContinente:
                    if(controllati.contains(Europa) && controllati.contains(Australia))
                        return true;
                    break;
                case EuropaSudAmericaContinente:
                    if (controllati.contains(Europa) && controllati.contains(SudAmerica))
                        return true;
                    break;
                default:
                    break;
            }

        return false;
    }
}
