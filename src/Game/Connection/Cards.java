package Game.Connection;

import Game.Map.Card;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Card packet
 */
public class Cards {

    public final ArrayList<Card> combination = new ArrayList<>();

    public Cards(Card... Cards) {
        combination.addAll(Arrays.asList(Cards));
    }

    public Cards(ArrayList<Card> Cards) {
        combination.addAll(Cards);
    }
}
