package Game;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Random;

import static Game.Continent.*;

/**
 * List of missions for earth map
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

    private String description;

    public String getDescription() { return description; }

    Mission(String Description) {
        this.description = Description;
    }

    /**
     * Check if player has completed his mission
     *
     * @param Player Player to check mission for
     * @return True if mission is accomplished, false otherwise.
     */
    public boolean Completed(Player Player) {
        String mission = this.name();

        // If mission is to destroy all armies of one color search inside match's players if color is still present
        // Armies already been destroyed problem is managed from battle class and armies color same as current player is handled in setup phase
        if(mission.contains("DistruggiArmata")){
            // Get armies' color
            Color color = Color.valueOf(mission.substring(15));

            // Get all players from the match
            Match match = GameController.getInstance().getMatch(Player.getMathcId());
            ArrayList<Player> players = match.getPlayers();

            // Check on all players (Check is performed on current player too, but it won't affect result)
            for (Player g: players
                 ) {
                if(g.getColor() == color)
                    return false;
            }

            return true;
        }

        if(mission.contains("Territori")){
            // Get territories number
            int numero = Integer.valueOf(mission.substring(9, 10));

            // Get player's territories
            ArrayList<Territory> territories = Player.getTerritories();

            if(numero == 24)
                return territories.size() >= 24;

            if(territories.size() < 18)
                return false;

            // If mission is 18 territories, player need to place at least two armies on each
            for (Territory t: territories
                 ) {
                if(t.getArmies() < 2)
                    return false;
            }
            return true;
        }

        // Check for common missions
        ArrayList<Continent> dominated = Continent.dominatedContinents(Player.getTerritories());
        if (dominated.size() > 1)
            switch (this) {
                case NordAmericaAfrica:
                    if(dominated.contains(NordAmerica) &&dominated.contains(Africa))
                            return true;
                    break;
                case NordAmericaAustralia:
                    if(dominated.contains(NordAmerica) && dominated.contains(Australia))
                        return true;
                    break;
                case AsiaAfrica:
                    if (dominated.contains(Asia) && dominated.contains(Africa))
                        return true;
                    break;
                case AsiaSudAmerica:
                    if (dominated.contains(Asia) && dominated.contains(SudAmerica))
                        return true;
                    break;
                default:
                    break;
            }
        else
            return false;

        if (dominated.size() > 2)
            switch (this) {
                case EuropaAustraliaContinente:
                    if(dominated.contains(Europa) && dominated.contains(Australia))
                        return true;
                    break;
                case EuropaSudAmericaContinente:
                    if (dominated.contains(Europa) && dominated.contains(SudAmerica))
                        return true;
                    break;
                default:
                    break;
            }

        return false;
    }
}
