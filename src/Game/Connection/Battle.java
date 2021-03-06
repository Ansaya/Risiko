package Game.Connection;

import Game.Map.Territory;

/**
 * Battle packet
 */
public class Battle<T extends Territory> {

    /**
     * Attacked territory
     */
    public final T to;

    /**
     * Attacker territory
     */
    public final T from;

    /**
     * Attacking armies number
     */
    public final int atkArmies;

    /**
     * Defending armies number
     */
    public int defArmies = 1;

    public Battle(T From, T To, int AtkArmies) {
        this.from = From;
        this.to = To;
        this.atkArmies = AtkArmies;
    }
}
