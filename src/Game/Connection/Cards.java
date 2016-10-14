package Game.Connection;

import Game.Map.Territories;

import java.util.ArrayList;

/**
 * Card packet
 */
public class Cards {

    private ArrayList<Territories> combination = new ArrayList<>();

    public ArrayList<Territories> getCombination() { return this.combination; }

    public Cards(Territories... Cards) {
        for (Territories t: Cards
             ) {
            combination.add(t);
        }
    }
}
