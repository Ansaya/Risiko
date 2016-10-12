package Game;

import java.util.ArrayList;
import java.util.Random;

import static Game.Continent.*;

/**
 * Lista di obiettivi relativi alla mappa mondiale
 */
public enum Mission {
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

    private static Mission[] correnti = Mission.values();

    private String descrizione;

    public String getDescrizione() { return descrizione; }

    Mission(String Descrizione) {
        this.descrizione = Descrizione;
    }


    /**
     * Restituisce un obiettivo da assegnare a un giocatore per la partita
     *
     * @return Mission dalla lista
     */
    public Mission next() {
        Random r = new Random();
        int index = r.nextInt(9);
        if(correnti[index] != null) {
            correnti[index] = null;
            return Mission.values()[index];
        }

        return next();
    }

    /**
     * Reimposta la distribuzione degli obiettivi
     */
    public void restart() {
        correnti = Mission.values();
    }

    /**
     * Controlla se il giocatore ha raggiunto l'obiettivo assegnato per la vittoria
     *
     * @param Player Player per il quale controllare l'obiettivo
     * @return Vero se l'obiettiov è completato, falso altrimenti
     */
    public boolean Completato(Player Player) {
        String obiettivo = this.name();

        // Se l'obiettivo è distruggere un'armate cerco tra i giocatori se esiste ancora il colore
        // I problemi di armata distrutta da un altro giocatore o colore uguale a quello del giocatere sono già gestiti da game controller e scontro
        if(obiettivo.contains("DistruggiArmata")){
            Color color = Color.valueOf(obiettivo.substring(15));
            Match match = GameController.getInstance().getPartita(Player.getIdPartita());
            ArrayList<Player> giocatori = match.getGiocatori();

            for (Player g: giocatori
                 ) {
                if(g.getColor() == color)
                    return false;
            }

            return true;
        }

        if(obiettivo.contains("Territori")){
            // Rilevo il numero di territori
            int numero = Integer.valueOf(obiettivo.substring(9, 10));

            // Acquisisco i territori del giocatore
            ArrayList<Territory> territori = Player.getTerritori();

            if(numero == 24)
                return territori.size() >= 24;

            if(territori.size() < 18)
                return false;

            // Se il numero è 18 devo avere due armate per ogni territorio
            for (Territory t: territori
                 ) {
                if(t.getArmate() < 2)
                    return false;
            }
            return true;
        }

        ArrayList<Continent> controllati = Continent.continentiControllati(Player.getTerritori());
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
