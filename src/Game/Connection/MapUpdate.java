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
     * @param Updated Updated territories after displacement
     */
    public MapUpdate(T... Updated) {
        if(Updated != null)
            this.Updated.addAll(Arrays.asList(Updated));
        AttackDice = null;
        DefenceDice = null;
        HasMove = false;
        Sound = Sounds.Match;
    }

    /**
     * MapUpdate to trigger end turn movement
     *
     * @param isMovement Set true to trigger end turn moving
     */
    public MapUpdate(boolean isMovement) {
        AttackDice = null;
        DefenceDice = null;
        HasMove = isMovement;
        Sound = null;
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
