package Game;

/**
 * Possible player end state
 */
public enum StateType {
    Winner,     // If the player has completed the mission
    Defeated,   // If the player is defeated by another player
    Looser;     // If another player wins the game
}
