package Client.Connection;

import Client.Observables.ObservableTerritory;

/**
 * Created by fiore on 05/12/2016.
 */
public class Attack {
    /**
     * Attacked territory
     */
    public final ObservableTerritory to;

    /**
     * Attacker territory
     */
    public final ObservableTerritory from;

    /**
     * Attacking armies number
     */
    public final int armies;

    public Attack(ObservableTerritory From, ObservableTerritory To, int Armies) {
        this.from = From;
        this.to = To;
        this.armies = Armies;
    }
}
