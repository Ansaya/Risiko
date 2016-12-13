package Game.Connection;

/**
 * Battle packet
 */
public class Battle<T> {

    /**
     * Attacked Territory
     */
    public final T to;

    /**
     * Attacker Territory
     */
    public final T from;

    /**
     * Attacking Armies number
     */
    public final int atkArmies;

    public int defArmies = 1;

    public Battle(T From, T To, int AtkArmies) {
        this.from = From;
        this.to = To;
        this.atkArmies = AtkArmies;
    }
}
