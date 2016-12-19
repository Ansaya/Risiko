package Game.Map.Territories;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by fiore on 18/12/2016.
 */
public class Map<T> {

    public final String Name;

    private final ArrayList<Area> areas = new ArrayList<>();

    private final HashMap<String, Territory<T>> territories = new HashMap<>();

    public Map(String Name) {
        this.Name = Name;
    }
}
