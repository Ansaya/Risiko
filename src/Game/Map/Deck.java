package Game.Map;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Random;

/**
 * Deck of cards
 */
public class Deck<T> {

    private final ArrayList<T> allCards;

    private final ArrayList<T> deck;

    public Deck(ArrayList<T> Cards) {
        allCards = Cards;
        deck = new ArrayList<>();
        shuffle();
    }

    @SafeVarargs
    public Deck(T... Cards) {
        allCards = new ArrayList<>(Arrays.asList(Cards));
        deck = new ArrayList<>();
        shuffle();
    }

    /**
     * Remove and return next card from the deck
     *
     * @return Card from deck
     */
    public T next() {
        if (deck.size() <= 0)
            shuffle();

        return deck.remove(0);
    }

    /**
     * Reset and shuffle missions' deck
     */
    public void shuffle() {
        deck.clear();
        deck.addAll(allCards);
        Collections.shuffle(deck, new Random(System.nanoTime()));
    }

    @SafeVarargs
    public final void setBack(T... Cards) {
        deck.addAll(deck.size(), Arrays.asList(Cards));
    }
}
