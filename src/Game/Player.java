package Game;

import Game.Map.Mission;
import Game.Map.Territory;

import java.util.ArrayList;

/**
 * Player relative to a match
 */
public class Player {

    public int getId() { return user.getId(); }

    /**
     * Id of match the player is inside
     */
    private int matchId;

    public int getMathcId() { return matchId; }

    /**
     * User bounded to this player
     */
    private User user;

    /**
     * User name chosen from the user
     *
     * @return User name
     */
    public String getName() { return user.getName(); }

    /**
     * Color assigned for the match
     */
    private Color color;

    public Color getColor() { return color; }

    /**
     * Dominated territories
     */
    private ArrayList<Territory> territories = new ArrayList<>();

    public ArrayList<Territory> getTerritories() { return (ArrayList<Territory>)territories.clone(); }

    /**
     * Player's mission
     */
    private Mission mission;

    public Mission getMission() { return mission; }

    public Player(User User, Color Color, int MatchId) {
        this.matchId = MatchId;
        this.user = User;
        this.color = Color;
    }
}