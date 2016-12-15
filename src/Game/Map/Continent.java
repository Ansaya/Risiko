package Game.Map;

import Server.Game.Map.Territory;
import javafx.scene.paint.Color;
import java.util.ArrayList;
import java.util.Arrays;
import static Game.Map.Territories.*;

/**
 * Continents' list From earth map
 */
public enum Continent {
    NorthAmerica("#d0ad00", Alaska, Alberta, CentralAmerica, WesternUnitedStates, Greenland, NorthwestTerritory, Ontario, Quebec, EasternUnitedStates),
    SouthAmerica("#a52a2a", Argentina, Brazil, Peru, Venezuela),
    Europe("#6495ed", GreatBritain, Iceland, NorthernEurope, Scandinavia, SouthernEurope, Ukraine, WesternEurope),
    Africa("#b8860b", Congo, EastAfrica, Egypt, Madagascar, NorthAfrica, SouthAfrica),
    Asia("#556b2f", Afghanistan, China, India, Irkutsk, Japan, Kamchatka, MiddleEast, Mongolia, Siam, Siberia, Ural, Yakutsk),
    Australia("#8a2be2", EasternAustralia, Indonesia, NewGuinea, WesternAustralia);

    public final Color hexColor;

    /**
     * Continent's territories
     */
    private final ArrayList<Territories> territories = new ArrayList<>();

    Continent(String HexColor, Territories... Territories) {
        this.hexColor = Color.web(HexColor);

        this.territories.addAll(Arrays.asList(Territories));
    }

    /**
     * Check if passed territories list contains complete continent To assign bonus Armies
     *
     * @param Territories Player's territories
     * @return int Bonus Armies To be assigned
     */
    public static int bonusArmies(ArrayList<Territory> Territories) {

        // Complete continents' list in the Territories array
        ArrayList<Continent> dominated = dominatedContinents(Territories);

        int bonus = 0;

        // If complete continents are present, assign bonus Armies
        for (Continent c: dominated) {
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

        // Increment continent counter for each Territory in the array
        for (Territory t: Territories) {
            switch (t.Territory.continent){
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

        // If all territories are present add continent To the output array
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
