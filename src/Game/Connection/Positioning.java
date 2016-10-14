package Game.Connection;

import Game.Map.Territory;
import com.sun.org.apache.bcel.internal.generic.NEW;
import javafx.geometry.Pos;

import java.util.ArrayList;

/**
 * Positioning packet
 */
public class Positioning {

    private int newArmies;

    public int getNewArmies() { return this.newArmies; }

    private ArrayList<Territory> territories;

    public ArrayList<Territory> getTerritories() { return this.territories; }

    /**
     * Server initializer. Specify new armies to be placed
     *
     * @param NewArmies Number of new armies
     */
    public Positioning(int NewArmies) {
        this.newArmies = NewArmies;
        this.territories = null;
    }

    /**
     * Client initializer. Specify new armies placement on player's territories
     *
     * @param Territories Player's territories
     */
    public Positioning(ArrayList<Territory> Territories) {
        this.territories = Territories;
        this.newArmies = 0;
    }
}
