package Gioco;

import java.util.ArrayList;

import static Gioco.Continente.*;
import static Gioco.Figura.*;

/**
 * Carte dei territori della mappa mondiale, comprensive di due carte jolly
 */
public enum Territorio {
    // Nord America
    Alaska (Fanteria, NordAmerica),
    Alberta (Fanteria, NordAmerica),
    AmericaCentrale (Cavalleria, NordAmerica),
    StatiUnitiOrientali (Artiglieria, NordAmerica),
    Groenlandia (Cavalleria, NordAmerica),
    TerritoriDelNordOvest (Artiglieria, NordAmerica),
    Ontario (Cavalleria, NordAmerica),
    Quebec (Artiglieria, NordAmerica),
    StatiUnitiOccidentali (Fanteria, NordAmerica),
    // Sud America
    Argentina(Fanteria, SudAmerica),
    Brasile(Artiglieria, SudAmerica),
    Peru(Cavalleria, SudAmerica),
    Venezuela(Artiglieria, SudAmerica),
    // Europa
    GranBretagna(Cavalleria, Europa),
    Islanda(Fanteria, Europa),
    EuropaSettentrionale(Cavalleria, Europa),
    Scandinavia(Artiglieria, Europa),
    EuropaMeridionale(Cavalleria, Europa),
    Ukraina(Artiglieria, Europa),
    EuropaOccidentale(Cavalleria, Europa),
    // Africa
    Congo(Cavalleria, Africa),
    AfricaOrientale(Artiglieria, Africa),
    Egitto(Fanteria, Africa),
    Madagascar(Fanteria, Africa),
    NordAfrica(Fanteria, Africa),
    SudAfrica(Artiglieria, Africa),
    // Asia
    Afghanistan(Fanteria, Asia),
    Cina(Cavalleria, Asia),
    India(Fanteria, Asia),
    Cita(Fanteria, Asia),
    Giappone(Fanteria, Asia),
    Kamchatka(Cavalleria, Asia),
    MedioOriente(Artiglieria, Asia),
    Mongolia(Artiglieria, Asia),
    Siam(Artiglieria, Asia),
    Siberia(Artiglieria, Asia),
    Urali(Cavalleria, Asia),
    Jacuzia(Cavalleria, Asia),
    // Australia
    AustraliaOrientale(Fanteria, Australia),
    Indonesia(Cavalleria, Australia),
    NuovaGuinea(Cavalleria, Australia),
    AustraliaOccidentale(Artiglieria, Australia),
    Jolly1(Jolly, null),
    Jolly2(Jolly, null);

    static {
        // Nord America
        Alaska.Init(Alberta, TerritoriDelNordOvest, Kamchatka);
        Alberta.Init(Alaska, StatiUnitiOrientali, TerritoriDelNordOvest, Ontario, StatiUnitiOccidentali);
        AmericaCentrale.Init(StatiUnitiOrientali, StatiUnitiOccidentali, Venezuela);
        StatiUnitiOrientali.Init(AmericaCentrale, Ontario, Quebec, StatiUnitiOccidentali);
        Groenlandia.Init(TerritoriDelNordOvest, Ontario, Quebec, Islanda);
        TerritoriDelNordOvest.Init(Alaska, Alberta, Ontario, Groenlandia);
        Ontario.Init(Alberta, StatiUnitiOrientali, Groenlandia, TerritoriDelNordOvest, Quebec, StatiUnitiOccidentali);
        Quebec.Init(StatiUnitiOrientali, Ontario, TerritoriDelNordOvest, Groenlandia);
        StatiUnitiOccidentali.Init(Alberta, AmericaCentrale, StatiUnitiOrientali, Ontario);

        // Sud America
        Argentina.Init(Brasile, Peru);
        Brasile.Init(Argentina, Peru, Venezuela, NordAfrica);
        Peru.Init(Argentina, Brasile, Venezuela);
        Venezuela.Init(Brasile, Peru, AmericaCentrale);

        // Europa
        GranBretagna.Init(Islanda, EuropaSettentrionale, Scandinavia, EuropaOccidentale);
        Islanda.Init(GranBretagna, Scandinavia, Groenlandia);
        EuropaSettentrionale.Init(GranBretagna, Scandinavia, EuropaMeridionale, Ukraina, EuropaOccidentale);
        Scandinavia.Init(GranBretagna, Islanda, EuropaSettentrionale, Ukraina);
        EuropaMeridionale.Init(EuropaSettentrionale, Ukraina, EuropaOccidentale, NordAfrica, Egitto, MedioOriente);
        Ukraina.Init(EuropaSettentrionale, Scandinavia, EuropaMeridionale, Afghanistan, Urali, MedioOriente);
        EuropaOccidentale.Init(GranBretagna, EuropaSettentrionale, EuropaMeridionale, NordAfrica);

        // Afirca
        Congo.Init(AfricaOrientale, NordAfrica, SudAfrica);
        AfricaOrientale.Init(Congo, Egitto, Madagascar, SudAfrica, MedioOriente);
        Egitto.Init(Congo, AfricaOrientale, NordAfrica, EuropaMeridionale, MedioOriente);
        Madagascar.Init(AfricaOrientale, SudAfrica);
        NordAfrica.Init(Congo, AfricaOrientale, Egitto, EuropaMeridionale, EuropaOccidentale, Brasile);
        SudAfrica.Init(Congo, AfricaOrientale, Madagascar);

        // Asia
        Afghanistan.Init(Cina, India, MedioOriente, Urali, Ukraina);
        Cina.Init(Afghanistan, India, Mongolia, Siam, Siberia, Urali);
        India.Init(Afghanistan, Cina, MedioOriente, Mongolia);
        Cita.Init(Kamchatka, Mongolia, Siberia, Jacuzia);
        Giappone.Init(Kamchatka, Mongolia);
        Kamchatka.Init(Cita, Giappone, Mongolia, Jacuzia, Alaska);
        MedioOriente.Init(Afghanistan, India, AfricaOrientale, Egitto, EuropaMeridionale, Ukraina);
        Mongolia.Init(Cina, Cita, Giappone, Kamchatka, Siberia);
        Siam.Init(Cina, India, Indonesia);
        Siberia.Init(Cita, Mongolia, Urali, Jacuzia);
        Urali.Init(Afghanistan, Cina, Siberia, Ukraina);
        Jacuzia.Init(Cita, Kamchatka, Siberia);

        // Australia
        AustraliaOrientale.Init(NuovaGuinea, AustraliaOccidentale);
        Indonesia.Init(NuovaGuinea, AustraliaOccidentale, Siam);
        NuovaGuinea.Init(AustraliaOrientale, Indonesia, AustraliaOccidentale);
        AustraliaOccidentale.Init(AustraliaOrientale, Indonesia, NuovaGuinea);
    }

    /**
     * Figura corrispondente alla carta del territorio
     */
    public final Figura figura;

    /**
     * Continente di appartenenza del territorio
     */
    public final Continente continente;

    /**
     * Lista dei terrtori confinanti
     */
    private ArrayList<Territorio> confinanti = new ArrayList<>();

    /**
     * Numero di armate presenti sul territorio
     */
    private int armate = 0;

    /**
     * Restituisce il numero di armate sul territorio
     * @return Numero di armate
     */
    public int getArmate() { return armate; }

    /**
     * Aggiunge armate al territorio
     *
     * @param numero Armate da aggiungere
     */
    public void addArmate(int numero) { armate += numero; }

    /**
     * Rimuove armate dal territorio
     *
     * @param numero Armate da rimuovere
     */
    public void removeArmate(int numero) {
        if(armate < numero)
            armate = 0;
        else
            armate -= numero;
    }

    Territorio(Figura Figura, Continente Continente) {
        this.figura = Figura;
        this.continente = Continente;
    }

    /**
     * Inizializzatore per la lista dei territori confinanti
     *
     * @param Territori Lista dei territori confinanti
     */
    private void Init(Territorio... Territori) {
        int i = 0;
        while (Territori[i] != null)
            confinanti.add(Territori[i++]);
    }

    /**
     * Verifica se il territorio corrente confina col territorio richiesto
     *
     * @param Territorio Torritorio col quale verificare il confine
     * @return Vero se i due territori confinano, falso altrimenti.
     */
    public boolean confinaCon(Territorio Territorio) {
        return this.confinanti.contains(Territorio);
    }

    /**
     * Controlla se le tre carte passate sono una combinazione valida
     *
     * @param Carte Terna di carte
     * @return Vero se la combinazione è valida, falso altrimenti
     */
    public static boolean isCombinazione(Territorio... Carte) {
        if(Carte.length != 3)
            return false;

        int fanteria = 0, cavalleria = 0, artiglieria = 0, jolly = 0;

        // Incremento il contatore di figura a seconda delle carte
        for (Territorio t: Carte
             ) {
            switch (t.figura){
                case Fanteria:
                    fanteria++;
                    break;
                case Cavalleria:
                    cavalleria++;
                    break;
                case Artiglieria:
                    artiglieria++;
                    break;
                case Jolly:
                    jolly++;
                    break;
                default:
                    break;
            }
        }

        // Controllo se riscontro combinazioni valide
        // Tre carte uguali
        if(fanteria == 3 || cavalleria == 3 || artiglieria == 3)
            return true;

        // Tre carte diverse
        if(fanteria == 1 && cavalleria == 1 && artiglieria == 1)
            return true;

        // Due carte uguali più jolly
        if (fanteria == 2 || cavalleria == 2 || artiglieria == 2)
            if(jolly == 1)
                return true;

        return false;
    }

    @Override
    public String toString() {
        String[] parti = this.name().split("(?=[A-Z])");
        String nome = parti[0];
        for (int i = parti.length; i > 0 ; i--) {
            nome += " " + parti;
        }

        return nome;
    }
}
