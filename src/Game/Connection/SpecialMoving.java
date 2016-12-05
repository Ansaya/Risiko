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
    public SpecialMoving(Attack<T> SuccessfulAttack) {
        this.from = SuccessfulAttack.from;
        this.to = SuccessfulAttack.to;
        this.newArmies = from.getArmies() - SuccessfulAttack.getArmies() - 1;   // At minimum one army has to occupy the territory
    }
}
