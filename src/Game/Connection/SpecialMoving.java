package Game.Connection;

/**
 * SpecialMoving packet
 */
public class SpecialMoving<T> {

    public final int newArmies;

    public final T from;

    public final T to;

    /**
     * Server initializer. Calculate possible number of armies to be moved to newly conquered territory
     *
     * @param SuccessfulAttack Successful attack made from the player
     */
    public SpecialMoving(Attack<T> SuccessfulAttack, int NewArmies) {
        this.from = SuccessfulAttack.from;
        this.to = SuccessfulAttack.to;
        this.newArmies = NewArmies;
    }
}
