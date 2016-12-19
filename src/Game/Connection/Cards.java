package Game.Connection;

import Game.Map.RealWorldMap;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Card packet
 */
public class Cards {

    public final ArrayList<RealWorldMap> combination = new ArrayList<>();

    public Cards(RealWorldMap... Cards) {
        combination.addAll(Arrays.asList(Cards));
    }

    public Cards(ArrayList<RealWorldMap> Cards) {
        combination.addAll(Cards);
    }
}
