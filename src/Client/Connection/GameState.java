package Client.Connection;

import Client.Observables.ObservableUser;
import Game.StateType;

/**
 * Created by fiore on 05/12/2016.
 */
public class GameState {
    public final StateType state;

    public final ObservableUser winner;

    public GameState(StateType State, ObservableUser Winner) {
        this.state = State;
        this.winner = Winner;
    }
}
