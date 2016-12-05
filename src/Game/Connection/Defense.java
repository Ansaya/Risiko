package Game.Connection;

/**
 * Defense packet
 */
public class Defense<T> {

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

    /**
     * Defending armies number
     */
    public int defArmies;

    public Defense(T From, T To, int Armies) {
        this.from = From;
        this.to = To;
        this.armies = Armies;
    }
}
