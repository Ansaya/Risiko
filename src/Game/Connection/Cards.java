package Game.Connection;

import Game.Map.Territories;
import java.util.ArrayList;

/**
 * Card packet
 */
public class Cards {

    public final ArrayList<Territories> combination = new ArrayList<>();

    public Cards(Territories... Cards) {
        for (Territories t: Cards) {
            combination.add(t);
        }
    }
}
