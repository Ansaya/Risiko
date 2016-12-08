package Game;

/**
 * Possible player end state
 */
public enum StateType {
    Winner,     // GameState message contains winner and the match is finished
    Defeated,   // If the player is defeated by another player, keep watching the match
    Abandoned;  // If player decides to abandon match, match is finished
}
