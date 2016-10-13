package Game;

/**
 * Instance of a territory on the map
 */
public class Territory {

    private final Territories territory;

    public Territories getTerritory() { return territory; }

    /**
     * Armies placed on this territory
     */
    private int armies = 0;

    /**
     * Get placed armies number
     *
     * @return Armies number
     */
    public int getArmies() { return armies; }

    private Color owner;

    public Color getOwner() { return owner; }

    public void setOwner(Color newOwner) { owner = newOwner; }

    public Territory(Territories Territory) {
        territory = Territory;
    }

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
}
