package Game.Connection;

import Game.Map.Territory;

/**
 * SpecialMoving packet
 */
public class SpecialMoving {

    private int newArmies;

    public int getNewArmies() { return this.newArmies; }

    private Territory from;

    public Territory getFrom() { return this.from; }

    private Territory to;

    public Territory getTo() { return this.to; }

    /**
     * Server initializer. Calculate possible number of armies to be moved to newly conquered territory
     *
     * @param SuccessfulAttack Successful attack made from the player
     */
    public SpecialMoving(Attack SuccessfulAttack) {
        this.from = SuccessfulAttack.getFrom();
        this.to = SuccessfulAttack.getTo();
        this.newArmies = from.getArmies() - SuccessfulAttack.getArmies() - 1;   // At minimum one army has to occupy the territory
    }

    /**
     * Client initializer. Contains updated territories
     *
     * @param From Territory from where the attack was taken
     * @param To Conquered territory
     */
    public SpecialMoving(Territory From, Territory To) {
        this.from = From;
        this.to = To;
        this.newArmies = 0;
    }
}
