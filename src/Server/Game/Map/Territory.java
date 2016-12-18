package Server.Game.Map;

import Game.Map.Territories;
import Server.Game.Player;
import javafx.application.Platform;

/**
 * Instance of a Territory on the map
 */
public class Territory {

    public final Territories Territory;

    /**
     * Armies placed on this Territory
     */
    private volatile int Armies = 0;

    public int getArmies() { return Armies; }

    public volatile int NewArmies = 0;

    private volatile Player owner = null;

    public void setOwner(Player Owner) {
        if(owner != null)
            owner.getTerritories().remove(this);

        owner = Owner;
        Owner.getTerritories().add(this);
    }

    public Player getOwner() { return owner; }

    public Territory(Territories Territory) {
        this.Territory = Territory;
    }

    /**
     * Add Armies To this Territory
     *
     * @param toAdd Armies To add
     */
    public void addArmies(int toAdd) { Armies += toAdd; }

    /**
     * Removes Armies From this Territory if is possible
     *
     * @param toRemove Armies To remove
     * @return True if Armies have been removed, false if cannot remove requested number of Armies
     */
    public boolean canRemoveArmies(int toRemove) {
        if(toRemove >= this.Armies)
            return false;

        this.Armies -= toRemove;
        return true;
    }

    @Override
    public String toString() {
        return Territory.toString();
    }

    @Override
    public boolean equals(Object other) {
        if(other instanceof Territory)
            return ((Territory)other).Territory == this.Territory;

        return other instanceof Territories && other == this.Territory;
    }
}
