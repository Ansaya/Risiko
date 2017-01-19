package Game.Connection;

import Game.Map.Territory;
import Game.Sounds.Sounds;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

/**
 * Map packet
 */
public class MapUpdate<T extends Territory> {

    /**
     * Updated territories on the map
     */
    public final ArrayList<T> Updated = new ArrayList<>();

    public final Collection<Integer> AttackDice;

    public final Collection<Integer> DefenceDice;

    public final boolean HasMove;

    public final Sounds Sound;

    /**
     * Standard update after armies displacement
     *
     * @param Updated Updated territories after displacement. If null HasMove is set to true and will trigger end turn movement
     */
    @SafeVarargs
    public MapUpdate(T... Updated) {
        if(Updated.length > 0) {
            this.Updated.addAll(Arrays.asList(Updated));
            HasMove = false;
        }
        else
            HasMove = true;

        AttackDice = null;
        DefenceDice = null;
        Sound = Sounds.Match;
    }

    public MapUpdate(Collection<T> Updated) {
        this.Updated.addAll(Updated);
        HasMove = false;
        AttackDice = null;
        DefenceDice = null;
        Sound = Sounds.Match;
    }

    /**
     * Update sent after battle containing dice results
     *
     * @param Attack Attack dice results
     * @param Defense Defence dice results
     * @param HasMove True if special move is expected after battle
     * @param Sound Sound to play for this battle
     * @param From Attacker territory
     * @param To Defender territory
     */
    public MapUpdate(Collection<Integer> Attack, Collection<Integer> Defense, boolean HasMove, Sounds Sound, T From, T To) {
        Updated.add(0, From);
        Updated.add(1, To);
        AttackDice = Attack;
        DefenceDice = Defense;
        this.HasMove = HasMove;
        this.Sound = Sound;
    }
}
