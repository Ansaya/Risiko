package Game.Connection;

import Game.Map.Card;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

/**
 * Card packet
 */
public class Cards {

    public final ArrayList<Card> combination = new ArrayList<>();

    public Cards(Card... Cards) {
        combination.addAll(Arrays.asList(Cards));
    }

    public Cards(Collection<Card> Cards) {
        combination.addAll(Cards);
    }
}
