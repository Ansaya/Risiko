package Game.Connection;

import Game.StateType;

/**
 * Current game state for the players
 */
public class GameState<T> {

    public final StateType state;

    public final T winner;

    public GameState(StateType State, T Winner) {
        this.state = State;
        this.winner = Winner;
    }
}
