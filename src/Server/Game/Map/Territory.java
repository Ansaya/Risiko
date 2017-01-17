package Server.Game.Map;

import Server.Game.Player;

/**
 * Instance of a territory on the map
 */
public class Territory extends Game.Map.Territory<Player> {

    /**
     * Armies placed on this territory
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

    /**
     * Add Armies to this territory
     *
     * @param toAdd Armies to add
     */
    public void addArmies(int toAdd) { Armies += toAdd; }

    /**
     * Removes armies from this territory if is possible
     *
     * @param toRemove Armies to remove
     * @return True if armies have been removed, false if cannot remove requested number of armies
     */
    public boolean canRemoveArmies(int toRemove) {
        if(toRemove >= this.Armies)
            return false;

        this.Armies -= toRemove;
        return true;
    }
}
