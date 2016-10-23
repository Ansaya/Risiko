package Game.Map;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Random;

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
    private int bonus = 4;

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
        bonus = 4;

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
        if(Cards.size() != 3)
            return 0;

        int infantry = 0, cavalry = 0, artillery = 0, jolly = 0;

        // Increment respective counter for each card
        for (Territories t: Cards
                ) {
            switch (t.card){
                case Infantry:
                    infantry++;
                    break;
                case Cavalry:
                    cavalry++;
                    break;
                case Artillery:
                    artillery++;
                    break;
                case Jolly:
                    jolly++;
                    break;
                default:
                    break;
            }
        }

        int armies = 0;

        // Check for valid combinations
        // Three same cards         Two same cards plus jolly       Three different cards
        if(infantry == 3 || cavalry == 3 || artillery == 3 || (infantry == 1 && cavalry == 1 && artillery == 1) ||
                ((infantry == 2 || cavalry == 2 || artillery == 2) && jolly == 1)) {
            armies = this.bonus;

            // If player redeems combination
            if(use) {
                // Increment bonus armies
                this.bonus += 2;

                // Add redeemed cards to the end of the deck
                this.deck.addAll(this.deck.size() - 1, Cards);
            }
        }

        return armies;
    }
}
