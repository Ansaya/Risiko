package Game.Map.Territories;

import javafx.scene.paint.Color;
import java.util.ArrayList;

/**
 * Created by fiore on 18/12/2016.
 */
public class Area {
    public final String Name;

    public final Color Color;

    private final ArrayList<Territory> territories = new ArrayList<>();

    public Area(String Name, String hexColor) {
        this.Name = Name;
        this.Color = javafx.scene.paint.Color.web(hexColor);
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof Area && ((Area)other).Name.equals(this.Name);
    }

    @Override
    public String toString() {
        return Name;
    }
}
