package Game.Map;

import Game.Player;
import java.util.ArrayList;

/**
 * Generic territory instance
 */
public abstract class Territory<T extends Player> {

    /**
     * Identifier for this territory
     */
    public final String Id;

    /**
     * UI localized name
     */
    public final transient String Name;

    public final transient Area Area;

    public transient String SvgPath;

    public final transient float ArmyX;

    public final transient float ArmyY;

    public final transient float LabelX;

    public final transient float LabelY;

    public final transient float LabelR;

    public abstract T getOwner();

    public abstract void setOwner(T NewOwner);

    public abstract int getArmies();

    private final transient ArrayList<Territory<T>> adjacent;

    public boolean isAdjacent(Territory Territory) {
        return adjacent.contains(Territory);
    }

    public Territory() {
        Id = "";
        Name = "";
        Area = null;
        SvgPath = "";
        ArmyX = 0.0f;
        ArmyY = 0.0f;
        LabelX = 0.0f;
        LabelY = 0.0f;
        LabelR = 0.0f;
        adjacent = null;
    }

    @Override
    public final boolean equals(Object other) {
        if(other instanceof String)
            return other.equals(this.Id);
        return other instanceof Territory && ((Territory)other).Id.equals(this.Id);
    }

    @Override
    public final String toString() {
        return Name.equals("") ? Id : Name;
    }
}
