package Game.Map;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Random;

/**
 * Deck of mission cards for earth map
 */
public class DeckMission implements Deck<Mission> {

    private ArrayList<Mission> deck;

    public DeckMission() { Shuffle(); }

    /**
     * Get a mission to be assigned to a player
     *
     * @return Mission from deck.
     */
    public Mission next() {

        if (deck.size() <= 0)
            Shuffle();

        Mission raised = deck.get(0);
        deck.remove(0);

        return raised;
    }

    /**
     * Reset and shuffle missions' deck
     */
    public void Shuffle() {
        deck = new ArrayList<>(Arrays.asList(Mission.values()));

        Collections.shuffle(deck, new Random(System.nanoTime()));
    }
}
