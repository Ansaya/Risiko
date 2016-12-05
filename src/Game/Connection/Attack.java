package Game.Connection;

/**
 * Attack packet
 */
public class Attack<T> {

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
    public final int armies;

    public Attack(T From, T To, int Armies) {
        this.from = From;
        this.to = To;
        this.armies = Armies;
    }
}
