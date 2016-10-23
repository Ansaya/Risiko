package Game.Connection;

/**
 * Positioning packet
 */
public class Positioning {

    private int newArmies;

    public int getNewArmies() { return this.newArmies; }

    /**
     * Server initializer. Specify new armies to be placed
     *
     * @param NewArmies Number of new armies
     */
    public Positioning(int NewArmies) {
        this.newArmies = NewArmies;
    }
}
