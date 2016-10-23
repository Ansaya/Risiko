package Game.Map;

import java.util.*;

import static Game.Map.Continent.*;
import static Game.Map.Card.*;

/**
 * Earth map territories' cards plus the two jolly cards
 */
public enum Territories {
    // Nord America
    Alaska (Infantry, NorthAmerica),
    Alberta (Infantry, NorthAmerica),
    CentralAmerica(Cavalry, NorthAmerica),
    EasternUnitedStates(Artillery, NorthAmerica),
    Greenland(Cavalry, NorthAmerica),
    NorthwestTerritory(Artillery, NorthAmerica),
    Ontario (Cavalry, NorthAmerica),
    Quebec (Artillery, NorthAmerica),
    WesternUnitedStates(Infantry, NorthAmerica),
    // Sud America
    Argentina(Infantry, SouthAmerica),
    Brazil(Artillery, SouthAmerica),
    Peru(Cavalry, SouthAmerica),
    Venezuela(Artillery, SouthAmerica),
    // Europe
    GreatBritain(Cavalry, Europe),
    Iceland(Infantry, Europe),
    NorthernEurope(Cavalry, Europe),
    Scandinavia(Artillery, Europe),
    SouthernEurope(Cavalry, Europe),
    Ukraine(Artillery, Europe),
    WesternEurope(Cavalry, Europe),
    // Africa
    Congo(Cavalry, Africa),
    EastAfrica(Artillery, Africa),
    Egypt(Infantry, Africa),
    Madagascar(Infantry, Africa),
    NorthAfrica(Infantry, Africa),
    SouthAfrica(Artillery, Africa),
    // Asia
    Afghanistan(Infantry, Asia),
    China(Cavalry, Asia),
    India(Infantry, Asia),
    Irkutsk(Infantry, Asia),
    Japan(Infantry, Asia),
    Kamchatka(Cavalry, Asia),
    MiddleEast(Artillery, Asia),
    Mongolia(Artillery, Asia),
    Siam(Artillery, Asia),
    Siberia(Artillery, Asia),
    Ural(Cavalry, Asia),
    Yakutsk(Cavalry, Asia),
    // Australia
    EasternAustralia(Infantry, Australia),
    Indonesia(Cavalry, Australia),
    NewGuinea(Cavalry, Australia),
    WesternAustralia(Artillery, Australia),
    Jolly1(Jolly, null),
    Jolly2(Jolly, null);

    static {
        // Nord America
        Alaska.Init(Alberta, NorthwestTerritory, Kamchatka);
        Alberta.Init(Alaska, EasternUnitedStates, NorthwestTerritory, Ontario, WesternUnitedStates);
        CentralAmerica.Init(EasternUnitedStates, WesternUnitedStates, Venezuela);
        EasternUnitedStates.Init(CentralAmerica, Ontario, Quebec, WesternUnitedStates);
        Greenland.Init(NorthwestTerritory, Ontario, Quebec, Iceland);
        NorthwestTerritory.Init(Alaska, Alberta, Ontario, Greenland);
        Ontario.Init(Alberta, EasternUnitedStates, Greenland, NorthwestTerritory, Quebec, WesternUnitedStates);
        Quebec.Init(EasternUnitedStates, Ontario, NorthwestTerritory, Greenland);
        WesternUnitedStates.Init(Alberta, CentralAmerica, EasternUnitedStates, Ontario);

        // Sud America
        Argentina.Init(Brazil, Peru);
        Brazil.Init(Argentina, Peru, Venezuela, NorthAfrica);
        Peru.Init(Argentina, Brazil, Venezuela);
        Venezuela.Init(Brazil, Peru, CentralAmerica);

        // Europe
        GreatBritain.Init(Iceland, NorthernEurope, Scandinavia, WesternEurope);
        Iceland.Init(GreatBritain, Scandinavia, Greenland);
        NorthernEurope.Init(GreatBritain, Scandinavia, SouthernEurope, Ukraine, WesternEurope);
        Scandinavia.Init(GreatBritain, Iceland, NorthernEurope, Ukraine);
        SouthernEurope.Init(NorthernEurope, Ukraine, WesternEurope, NorthAfrica, Egypt, MiddleEast);
        Ukraine.Init(NorthernEurope, Scandinavia, SouthernEurope, Afghanistan, Ural, MiddleEast);
        WesternEurope.Init(GreatBritain, NorthernEurope, SouthernEurope, NorthAfrica);

        // Afirca
        Congo.Init(EastAfrica, NorthAfrica, SouthAfrica);
        EastAfrica.Init(Congo, Egypt, Madagascar, SouthAfrica, MiddleEast);
        Egypt.Init(Congo, EastAfrica, NorthAfrica, SouthernEurope, MiddleEast);
        Madagascar.Init(EastAfrica, SouthAfrica);
        NorthAfrica.Init(Congo, EastAfrica, Egypt, SouthernEurope, WesternEurope, Brazil);
        SouthAfrica.Init(Congo, EastAfrica, Madagascar);

        // Asia
        Afghanistan.Init(China, India, MiddleEast, Ural, Ukraine);
        China.Init(Afghanistan, India, Mongolia, Siam, Siberia, Ural);
        India.Init(Afghanistan, China, MiddleEast, Mongolia);
        Irkutsk.Init(Kamchatka, Mongolia, Siberia, Yakutsk);
        Japan.Init(Kamchatka, Mongolia);
        Kamchatka.Init(Irkutsk, Japan, Mongolia, Yakutsk, Alaska);
        MiddleEast.Init(Afghanistan, India, EastAfrica, Egypt, SouthernEurope, Ukraine);
        Mongolia.Init(China, Irkutsk, Japan, Kamchatka, Siberia);
        Siam.Init(China, India, Indonesia);
        Siberia.Init(Irkutsk, Mongolia, Ural, Yakutsk);
        Ural.Init(Afghanistan, China, Siberia, Ukraine);
        Yakutsk.Init(Irkutsk, Kamchatka, Siberia);

        // Australia
        EasternAustralia.Init(NewGuinea, WesternAustralia);
        Indonesia.Init(NewGuinea, WesternAustralia, Siam);
        NewGuinea.Init(EasternAustralia, Indonesia, WesternAustralia);
        WesternAustralia.Init(EasternAustralia, Indonesia, NewGuinea);
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
        for (Game.Map.Territories t: Territories
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
