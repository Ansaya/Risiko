package Game;

/**
 * Represents a deck of cards
 */
public interface Deck<T> {
    /**
     * Shuffle deck
     */
    void Shuffle();

    /**
     * Get next card from top
     * @return Card from deck
     */
    T next();
}
