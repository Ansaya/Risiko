package Game.Connection;

/**
 * Positioning packet
 */
public class Positioning {

    public final int newArmies;

    /**
     * Server initializer. Specify new armies to be placed
     *
     * @param NewArmies Number of new armies
     */
    public Positioning(int NewArmies) {
        this.newArmies = NewArmies;
    }
}
