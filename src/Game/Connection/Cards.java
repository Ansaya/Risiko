package Game.Connection;

import Game.Map.Territories;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Card packet
 */
public class Cards {

    public final ArrayList<Territories> combination = new ArrayList<>();

    public Cards(Territories... Cards) {
        combination.addAll(Arrays.asList(Cards));
    }

    public Cards(ArrayList<Territories> Cards) {
        combination.addAll(Cards);
    }
}
