package Game.Connection;

import Game.StateType;

/**
 * Current game state for the players
 */
public class GameState {

    private StateType state;

    public StateType getState() { return this.state; }

    private User winner;

    public User getWinner() { return this.winner; }

    public GameState(StateType State, User Winner) {
        this.state = State;
        this.winner = Winner;
    }

}
