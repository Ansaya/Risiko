package Server.Game.Map;

import Game.Map.Territories;
import Server.Game.Player;

/**
 * Instance of a territory on the map
 */
public class Territory {

    public final Territories territory;

    /**
     * Armies placed on this territory
     */
    private volatile int armies = 0;

    public int getArmies() { return armies; }

    public volatile int newArmies = 0;

    public volatile Player owner = null;

    public Territory(Territories Territory) {
        territory = Territory;
    }

    /**
     * Add armies to this territory
     *
     * @param toAdd armies to add
     */
    public void addArmies(int toAdd) { armies += toAdd; }

    /**
     * Remove armies from this territory
     *
     * @param toRemove armies to remove
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
