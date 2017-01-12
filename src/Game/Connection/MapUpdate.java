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
    public final ArrayList<T> Updated = new ArrayList<>();

    public final ArrayList<Integer> AttackDice;

    public final ArrayList<Integer> DefenceDice;

    public final Sounds Sound;

    /**
     * Standard update after armies displacement
     *
     * @param Updated Updated territories after displacement
     */
    public MapUpdate(ArrayList<T> Updated) {
        this.Updated.addAll(Updated);
        AttackDice = null;
        DefenceDice = null;
        Sound = Sounds.Match;
    }

    public MapUpdate(T... Updated) {
        this.Updated.addAll(Arrays.asList(Updated));
        AttackDice = null;
        DefenceDice = null;
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
        Updated.addAll(Arrays.asList(Battle));
        AttackDice = Attack;
        DefenceDice = Defense;
        this.Sound = Sound;
    }
}
