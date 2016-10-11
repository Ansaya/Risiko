package Gioco;

import java.lang.reflect.Array;
import java.util.*;

import static Gioco.Continent.*;
import static Gioco.Card.*;

/**
 * Earth map territories' cards plus the two jolly cards
 */
public enum Territory {
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
     * Territory card
     */
    public final Card card;

    /**
     * Binded continent
     */
    public final Continent continent;

    /**
     * Adjacent territories
     */
    private ArrayList<Territory> adjacent = new ArrayList<>();

    /**
     * Armies placed on this territory
     */
    private int armies = 0;

    /**
     * Number of armies on this territory
     * @return Number of armies
     */
    public int getArmies() { return armies; }

    /**
     * Add armies to this territory
     *
     * @param toAdd Armies to add
     */
    public void addArmies(int toAdd) { armies += toAdd; }

    /**
     * Remove armies from this territory
     *
     * @param toRemove Armies to remove
     */
    public void removeArmies(int toRemove) {
        if(armies < toRemove)
            armies = 0;
        else
            armies -= toRemove;
    }

    Territory(Card Card, Continent Continent) {
        this.card = Card;
        this.continent = Continent;
    }

    /**
     * Initializer to add adjacent territories
     *
     * @param Territories List of adjacent territories
     */
    private void Init(Territory... Territories) {
        int i = 0;
        while (Territories[i] != null)
            adjacent.add(Territories[i++]);
    }

    private static ArrayList<Territory> deck = new ArrayList<>();

    private static int index = 0;

    private static int bonus = 4;

    /**
     * Reset deck to original size, index and bonus, then shuffles cards inside the deck
     */
    public static void deckShuffle() {
        deck.clear();
        deck.addAll(Arrays.asList(Territory.values()));
        index = 0;
        bonus = 4;

        Collections.shuffle(deck, new Random(System.nanoTime()));
    }

    /**
     * Get next card from the deck
     *
     * @return Card from deck
     */
    public static Territory next() {
        if(deck.size() == 0)
            deckShuffle();

        return deck.get(index++);
    }

    /**
     * Check if this and the passed territory are adjacent
     *
     * @param Territory Territory to check adjoining with
     * @return True if the two territories are adjacent, false otherwise.
     */
    public boolean isAdjacent(Territory Territory) {
        return this.adjacent.contains(Territory);
    }

    /**
     * Check if card combination is valid
     *
     * @param use If combination is redeemed push cards to the bottom of current deck
     * @param Cards Three cards list
     * @return Number of bonus armies if combination is valid, zero otherwise
     */
    public static int isCombinationValid(boolean use, Territory... Cards) {
        if(Cards.length != 3)
            return 0;

        int infantry = 0, cavalry = 0, artillery = 0, jolly = 0;

        // Increment respective counter for each card
        for (Territory t: Cards
             ) {
            switch (t.card){
                case Fanteria:
                    infantry++;
                    break;
                case Cavalleria:
                    cavalry++;
                    break;
                case Artiglieria:
                    artillery++;
                    break;
                case Jolly:
                    jolly++;
                    break;
                default:
                    break;
            }
        }

        int armies = 0;

        // Check for valid combinations
        // Three same cards         Two same cards plus jolly       Three different cards
        if(infantry == 3 || cavalry == 3 || artillery == 3 || (infantry == 1 && cavalry == 1 && artillery == 1) ||
                ((infantry == 2 || cavalry == 2 || artillery == 2) && jolly == 1)) {
            armies = bonus;

            // If player redeems combination
            if(use) {
                // Increment bonus armies
                bonus += 2;

                // Add redeemed cards to the end of the deck
                deck.addAll(deck.size() - 1, Arrays.asList(Cards));
            }
        }

        return armies;
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
