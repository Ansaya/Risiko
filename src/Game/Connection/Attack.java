package Game.Connection;

import Game.Map.Territory;

/**
 * Attack packet
 */
public class Attack {

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

    public Attack(Territory From, Territory To, int Armies) {
        this.from = From;
        this.to = To;
        this.armies = Armies;
    }
}
