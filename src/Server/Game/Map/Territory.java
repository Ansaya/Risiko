package Server.Game.Map;

import Server.Game.Player;

/**
 * Instance of a Territory on the map
 */
public class Territory extends Game.Map.Territory<Player> {

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
}
