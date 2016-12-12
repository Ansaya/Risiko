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
     * Removes armies from this territory if is possible
     *
     * @param toRemove armies to remove
     * @return True if armies have been removed, false if cannot remove requested number of armies
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

    @Override
    public boolean equals(Object other) {
        if(other instanceof Territory)
            return ((Territory)other).territory == this.territory;

        return other instanceof Territories && other == this.territory;
    }
}
