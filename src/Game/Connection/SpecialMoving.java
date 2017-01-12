package Game.Connection;

import Game.Map.Territory;

/**
 * SpecialMoving packet
 */
public class SpecialMoving<T extends Territory> {

    public final T From;

    public final T To;

    /**
     * Send updated armies after battle to ask user to move other armies to new territory
     *
     * @param From Attacker territory To move armies From
     * @param To Conquered territory To move armies To
     */
    public SpecialMoving(T From, T To) {
        this.From = From;
        this.To = To;
    }
}
