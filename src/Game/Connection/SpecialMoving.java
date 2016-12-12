package Game.Connection;

/**
 * SpecialMoving packet
 */
public class SpecialMoving<T> {

    public final T from;

    public final T to;

    /**
     * Server initializer. Calculate possible number of armies to be moved to newly conquered territory
     *
     * @param successfulBattle Successful attack made from the player
     */
    public SpecialMoving(Battle<T> successfulBattle) {
        this.from = successfulBattle.from;
        this.to = successfulBattle.to;
    }

    public SpecialMoving(T FromUpdated, T ToUpdated) {
        this.from = FromUpdated;
        this.to = ToUpdated;
    }
}
