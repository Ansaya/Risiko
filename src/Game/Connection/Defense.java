package Game.Connection;

import Game.Map.Territory;

/**
 * Defense packet
 */
public class Defense {

    /**
     * Attacked territory
     */
    private Territory to;

    public Territory getTo() { return this.to; }

    /**
     * Attacker territory
     */
    private Territory from;

    public Territory getFrom() { return this.from; }

    /**
     * Attacking armies number
     */
    private int armies;

    public int getArmies() { return this.armies; }

    /**
     * Defending armies number
     */
    private int defArmies;

    public int getDefArmies() { return this.defArmies; }

    public void setDefArmies(int DefArmies) { this.defArmies = DefArmies; }

    public Defense(Territory From, Territory To, int Armies) {
        this.from = From;
        this.to = To;
        this.armies = Armies;
    }
}
