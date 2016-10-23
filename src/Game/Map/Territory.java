package Game.Map;

import Game.Color;
import Game.Map.Territories;
import Game.Player;

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

    public int getArmies() { return armies; }

    private int newArmies = 0;

    public int getNewArmies() { return this.newArmies; }

    public void addNewArmies(int toAdd) { this.newArmies += toAdd; }

    public boolean canRemoveNewArmies(int toRemove) {
        if(toRemove > this.newArmies)
            return false;

        this.newArmies -= toRemove;
        return true;
    }

    private Player owner;

    public Player getOwner() { return this.owner; }

    public void setOwner(Player newOwner) { this.owner = newOwner; }

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
    public boolean canRemoveArmies(int toRemove) {
        if(toRemove >= this.armies)
            return false;

        this.armies -= toRemove;
        return true;
    }

    @Override
    public String toString() {
        return territory.toString();
    }
}
