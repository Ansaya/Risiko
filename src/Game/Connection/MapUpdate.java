package Game.Connection;

import Game.Sounds.Sounds;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Map packet
 */
public class MapUpdate<T> {

    /**
     * Updated territories on the map
     */
    public final ArrayList<T> updated = new ArrayList<>();

    public final ArrayList<Integer> attackDice;

    public final ArrayList<Integer> defenceDice;

    public final Sounds Sound;

    /**
     * Standard update after armies displacement
     *
     * @param Updated Updated territories after displacement
     */
    public MapUpdate(ArrayList<T> Updated) {
        this.updated.addAll(Updated);
        attackDice = null;
        defenceDice = null;
        Sound = Sounds.Match;
    }

    public MapUpdate(T... Updated) {
        updated.addAll(Arrays.asList(Updated));
        attackDice = null;
        defenceDice = null;
        Sound = Sounds.Match;
    }

    /**
     * Update sent after battle containing dice results
     *
     * @param Attack Attack dice results
     * @param Defense Defence dice results
     * @param Battle Battle territories updated
     */
    public MapUpdate(ArrayList<Integer> Attack, ArrayList<Integer> Defense, Sounds Sound, T... Battle) {
        updated.addAll(Arrays.asList(Battle));
        attackDice = Attack;
        defenceDice = Defense;
        this.Sound = Sound;
    }
}
