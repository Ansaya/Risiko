package Game.Map.Territories;

import java.util.ArrayList;

/**
 * Created by fiore on 18/12/2016.
 */
public class Territory<T> {

    public final String Name;

    public final transient Card Card;

    public final transient Area Area;

    public transient String SvgPath = "";

    public transient float ArmyX = 0.0f;

    public transient float ArmyY = 0.0f;

    private volatile T owner = null;

    public T getOwner() { return owner; }

    public void setOwner(T NewOwner) {
        owner = NewOwner;
    }

    private final transient ArrayList<String> adjacent = new ArrayList<>();

    public Territory(Territory Territory) {
        Name = Territory.Name;
        Card = Territory.Card;
        Area = Territory.Area;
        SvgPath = Territory.SvgPath;
        ArmyX = Territory.ArmyX;
        ArmyY = Territory.ArmyY;
    }

    private Territory(Card Card, Area Area, String Name, String SvgPath) {
        this.Card = Card;
        this.Area = Area;
        this.Name = Name;
        this.SvgPath = SvgPath;
    }

    public boolean isAdjacent(Territory Territory) {
        return adjacent.contains(Territory.Name);
    }

    public String toString() {  return Name; }
}
