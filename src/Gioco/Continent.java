package Gioco;

import java.util.ArrayList;

import static Gioco.Territory.*;

/**
 * Continents' list from earth map
 */
public enum Continent {
    NordAmerica(Alaska, Alberta, AmericaCentrale, StatiUnitiOccidentali, Groenlandia, TerritoriDelNordOvest, Ontario, Quebec, StatiUnitiOrientali),
    SudAmerica(Argentina, Brasile, Peru, Venezuela),
    Europa(GranBretagna, Islanda, EuropaSettentrionale, Scandinavia, EuropaMeridionale, Ukraina, EuropaOccidentale),
    Africa(Congo, AfricaOrientale, Egitto, Madagascar, NordAfrica, SudAfrica),
    Asia(Afghanistan, Cina, India, Cita, Giappone, Kamchatka, MedioOriente, Mongolia, Siam, Siberia, Urali, Jacuzia),
    Australia(AustraliaOrientale, Indonesia, NuovaGuinea, AustraliaOccidentale);

    /**
     * Continent's territories
     */
    private ArrayList<Territory> territories = new ArrayList<>();

    Continent(Territory... Territories) {
        for (Territory t: Territories
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
                case NordAmerica:
                    bonus += 5;
                    break;
                case SudAmerica:
                    bonus += 2;
                    break;
                case Europa:
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
        for (Territory t:Territories
                ) {
            switch (t.continent){
                case NordAmerica:
                    nordAmerica++;
                    break;
                case SudAmerica:
                    sudAmerica++;
                    break;
                case Europa:
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
            dominated.add(NordAmerica);
        if (sudAmerica == 4)
            dominated.add(SudAmerica);
        if (europa == 7)
            dominated.add(Europa);
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
