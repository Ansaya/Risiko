package Game.Connection;

import Game.StateType;

/**
 * Current game state for the players
 */
public class GameState {

    private StateType state;

    public StateType getState() { return this.state; }

    private String winner;

    public String getWinner() { return this.winner; }

    public GameState(StateType State, String Winner) {
        this.state = State;
        this.winner = Winner;
    }

}
