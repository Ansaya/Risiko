package Game.Connection;

/**
 * Positioning packet
 */
public class Positioning {

    public final int newArmies;

    /**
     * Server initializer. Specify new Armies To be placed
     *
     * @param NewArmies Number of new Armies
     */
    public Positioning(int NewArmies) {
        this.newArmies = NewArmies;
    }
}
