package Game.Map;

import java.util.ArrayList;

/**
 * List of possible risk cards
 */
public enum Card {
    Infantry,
    Cavalry,
    Artillery,
    Jolly;

    /**
     * Check if card combination is valid
     *
     * @param Cards Three cards list
     * @return True if combination is valid, false if not
     */
    public static boolean isCombinationValid(ArrayList<Territories> Cards) {
        if(Cards.size() != 3)
            return false;

        int infantry = 0, cavalry = 0, artillery = 0, jolly = 0;

        // Increment respective counter for each card
        for (Territories t: Cards) {
            switch (t.card){
                case Infantry:
                    infantry++;
                    break;
                case Cavalry:
                    cavalry++;
                    break;
                case Artillery:
                    artillery++;
                    break;
                case Jolly:
                    jolly++;
                    break;
                default:
                    break;
            }
        }

        // Check for valid combinations
        // Three same cards         Two same cards plus jolly       Three different cards
        if(infantry == 3 || cavalry == 3 || artillery == 3 || (infantry == 1 && cavalry == 1 && artillery == 1) ||
                ((infantry == 2 || cavalry == 2 || artillery == 2) && jolly == 1))
            return true;

        return false;
    }
}
