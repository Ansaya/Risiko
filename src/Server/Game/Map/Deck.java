package Server.Game.Map;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Random;

/**
 * Deck of cards
 */
public class Deck<T extends Enum<T>> {

    private ArrayList<T> deck;

    private final Class<T> enumType;

    public Deck(Class<T> EnumType) {
        enumType = EnumType;
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
        deck = new ArrayList<>(Arrays.asList(enumType.getEnumConstants()));
        Collections.shuffle(deck, new Random(System.nanoTime()));
    }

    public void setBack(T Card) {
        deck.add(deck.size(), Card);
    }
}
