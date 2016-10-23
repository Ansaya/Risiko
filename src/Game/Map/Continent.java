package Game.Map;

import java.util.ArrayList;

import static Game.Map.Territories.*;

/**
 * Continents' list from earth map
 */
public enum Continent {
    NorthAmerica(Alaska, Alberta, CentralAmerica, WesternUnitedStates, Greenland, NorthwestTerritory, Ontario, Quebec, EasternUnitedStates),
    SouthAmerica(Argentina, Brazil, Peru, Venezuela),
    Europe(GreatBritain, Iceland, NorthernEurope, Scandinavia, SouthernEurope, Ukraine, WesternEurope),
    Africa(Congo, EastAfrica, Egypt, Madagascar, NorthAfrica, SouthAfrica),
    Asia(Afghanistan, China, India, Irkutsk, Japan, Kamchatka, MiddleEast, Mongolia, Siam, Siberia, Ural, Yakutsk),
    Australia(EasternAustralia, Indonesia, NewGuinea, WesternAustralia);

    /**
     * Continent's territories
     */
    private ArrayList<Territories> territories = new ArrayList<>();

    Continent(Territories... Territories) {
        for (Game.Map.Territories t: Territories
             ) {
            this.territories.add(t);
        }
    }

    /**
     * Check if passed territories list contains complete continent to assign bonus armies
     *
     * @param Territories Player's territories
     * @return int Bonus armies to be assigned
     */
    public static int bonusArmies(ArrayList<Territory> Territories) {

        // Complete continents' list in the Territories array
        ArrayList<Continent> dominated = dominatedContinents(Territories);

        int bonus = 0;

        // If complete continents are present, assign bonus armies
        for (Continent c: dominated
             ) {
            switch (c){
                case NorthAmerica:
                    bonus += 5;
                    break;
                case SouthAmerica:
                    bonus += 2;
                    break;
                case Europe:
                    bonus += 5;
                    break;
                case Africa:
                    bonus += 3;
                    break;
                case Asia:
                    bonus += 7;
                    break;
                case Australia:
                    bonus += 2;
                    break;
                default:
                    break;
            }
        }

        return bonus;
    }

    /**
     * Check if passed territories list contains complete continents
     *
     * @param Territories Player's territories
     * @return List of complete continents
     */
    public static ArrayList<Continent> dominatedContinents(ArrayList<Territory> Territories) {
        int nordAmerica = 0, sudAmerica = 0, europa = 0, africa = 0, asia = 0, australia = 0;

        // Increment continent counter for each territory in the array
        for (Territory t: Territories
                ) {
            switch (t.getTerritory().continent){
                case NorthAmerica:
                    nordAmerica++;
                    break;
                case SouthAmerica:
                    sudAmerica++;
                    break;
                case Europe:
                    europa++;
                    break;
                case Africa:
                    africa++;
                    break;
                case Asia:
                    asia++;
                    break;
                case Australia:
                    australia++;
                    break;
                default:
                    break;
            }
        }

        ArrayList<Continent> dominated = new ArrayList<>();

        // If all territories are present add continent to the output array
        if(nordAmerica == 9)
            dominated.add(NorthAmerica);
        if (sudAmerica == 4)
            dominated.add(SouthAmerica);
        if (europa == 7)
            dominated.add(Europe);
        if (africa == 6)
            dominated.add(Africa);
        if (asia == 12)
            dominated.add(Asia);
        if (australia == 4)
            dominated.add(Australia);

        return dominated;
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
