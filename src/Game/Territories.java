package Game;

import java.util.*;

import static Game.Continent.*;
import static Game.Card.*;

/**
 * Earth map territories' cards plus the two jolly cards
 */
public enum Territories {
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
     * Territories card
     */
    public final Card card;

    /**
     * Binded continent
     */
    public final Continent continent;

    /**
     * Adjacent territories
     */
    private ArrayList<Territories> adjacent = new ArrayList<>();

    Territories(Card Card, Continent Continent) {
        this.card = Card;
        this.continent = Continent;
    }

    /**
     * Initializer to add adjacent territories
     *
     * @param Territories List of adjacent territories
     */
    private void Init(Territories... Territories) {
        for (Game.Territories t: Territories
             ) {
            adjacent.add(t);
        }
    }

    /**
     * Check if this and the passed territory are adjacent
     *
     * @param Territories Territories to check adjoining with
     * @return True if the two territories are adjacent, false otherwise.
     */
    public boolean isAdjacent(Territories Territories) {
        return this.adjacent.contains(Territories);
    }

    @Override
    public String toString() {
        String[] words = this.name().split("(?=[A-Z])");
        String name = words[0];
        for (int i = 1; i < words.length ; i++) {
            name += " " + words[i];
        }

        return name;
    }
}
