package Server.Game.Map;

import Game.Map.Card;
import Game.Map.Territories;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Represent a deck of territories cards and two jolly
 */
public class DeckTerritory implements Deck<Territories> {

    /**
     * Represents the cards' deck
     */
    private ArrayList<Territories> deck;

    /**
     * Current bonus armies counter
     */
    private final AtomicInteger bonus = new AtomicInteger(4);

    public DeckTerritory() {
        Shuffle();
    }

    /**
     * Reset deck to original size, index and bonus, then shuffles cards inside the deck
     */
    @Override
    public void Shuffle() {
        System.out.println("Executed from thread " + Thread.currentThread().getId());

        deck = new ArrayList<>(Arrays.asList(Territories.values()));
        bonus.set(4);

        Collections.shuffle(deck, new Random(System.nanoTime()));
    }

    /**
     * Get next card from the deck
     *
     * @return Card from deck
     */
    @Override
    public Territories next() {
        if(deck.size() == 0)
            Shuffle();

        Territories currentCard = deck.get(0);
        deck.remove(0);
        return currentCard;
    }

    /**
     * Check if card combination is valid
     *
     * @param use If combination is redeemed push cards to the bottom of current deck
     * @param Cards Three cards list
     * @return Number of bonus armies if combination is valid, zero otherwise
     */
    public int isCombinationValid(boolean use, ArrayList<Territories> Cards) {
        if(!Card.isCombinationValid(Cards))
            return 0;

            int armies = this.bonus.get();

            // If player redeems combination
            if(use) {

                // Increment bonus armies by 2 till 12 then from 15 by 5 each time
                if(bonus.get() < 12)
                    bonus.getAndAdd(2);
                else {
                    bonus.compareAndSet(12, 13);
                    bonus.getAndAdd(5);
                }

                // Add redeemed cards to the end of the deck
                this.deck.addAll(this.deck.size() - 1, Cards);
            }

        return armies;
    }
}
