package Game.Map;

import Game.Player;
import java.util.ArrayList;

/**
 * Generic territory instance
 */
public abstract class Territory<T extends Player> {

    public final String Name;

    public final transient Area Area;

    public transient String SvgPath;

    public final transient float ArmyX;

    public final transient float ArmyY;

    private volatile T owner = null;

    public  T getOwner() { return owner; }

    public void setOwner(T NewOwner) {
        owner = NewOwner;
    }

    private final transient ArrayList<Territory<T>> adjacent;

    public boolean isAdjacent(Territory Territory) {
        return adjacent.contains(Territory);
    }

    public Territory() {
        Name = "";
        Area = null;
        SvgPath = "";
        ArmyX = 0.0f;
        ArmyY = 0.0f;
        adjacent = null;
    }

    @Override
    public final boolean equals(Object other) {
        if(other instanceof String)
            return other.equals(this.Name);
        return other instanceof Territory && ((Territory)other).Name.equals(this.Name);
    }

    @Override
    public final String toString() {
        return this.Name;
    }
}
