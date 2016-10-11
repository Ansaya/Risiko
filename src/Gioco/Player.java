package Gioco;

import java.util.ArrayList;

/**
 * Player relative to a match
 */
public class Player {

    /**
     * Player id given from match object
     */
    private int id;

    public int getId() { return id; }

    private int matchId;

    public int getMathcId() { return matchId; }

    /**
     * User binded to this player
     */
    private User user;

    /**
     * User name chosen from the user
     * @return User name
     */
    public String getName() { return user.getNome(); }

    private Color color;

    public Color getColor() { return color; }

    /**
     * Dominated territories
     */
    private ArrayList<Territory> territories = new ArrayList<>();

    /**
     * List of dominated user's territories
     * @return Territories' list
     */
    public ArrayList<Territory> getTerritori() { return (ArrayList<Territory>)territories.clone(); }

    /**
     * Player's mission
     */
    private Mission mission;

    /**
     * Get player's mission
     * @return Player's mission
     */
    public Mission getMission() { return mission; }

    /**
     * Player's armies still to be placed
     */
    private int armies = 0;

    /**
     * Armies still to be placed on the map
     * @return Number of armies
     */
    public int getArmies() { return armies; }

    private static int counter = 0;

    public Player(User User, Color Color, int MatchId) {
        this.id = counter++;
        this.matchId = MatchId;
        this.user = User;
        this.color = Color;
    }
}