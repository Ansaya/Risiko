package Server.Game.Map;

import Game.Map.Card;
import Game.Map.Territories;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Represent a deck of territories cards and two jolly
 */
public class DeckTerritory extends Deck<Territories> {

    /**
     * Current bonus Armies counter
     */
    private final AtomicInteger bonus = new AtomicInteger(4);

    public DeckTerritory() {
        super(Territories.class);
        shuffle();
    }

    /**
     * Reset deck To original size, index and bonus, then shuffles cards inside the deck
     */
    @Override
    public void shuffle() {
        super.shuffle();
        bonus.set(4);
    }

    /**
     * Check if card combination is valid
     *
     * @param use If combination is redeemed push cards To the bottom of current deck
     * @param Cards Three cards list
     * @return Number of bonus Armies if combination is valid, zero otherwise
     */
    public int isCombinationValid(boolean use, ArrayList<Territories> Cards) {
        if(!Card.isCombinationValid(Cards))
            return 0;

            int armies = this.bonus.get();

            // If player redeems combination
            if(use) {

                // Increment bonus Armies by 2 till 12 then From 15 by 5 each time
                if(bonus.get() < 12)
                    bonus.getAndAdd(2);
                else {
                    bonus.compareAndSet(12, 13);
                    bonus.getAndAdd(5);
                }

                // Add redeemed cards To the end of the deck
                Cards.forEach(this::setBack);
            }

        return armies;
    }
}
