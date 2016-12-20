package Game.Map;

import javafx.scene.paint.Color;
import java.util.ArrayList;

/**
 * Created by fiore on 18/12/2016.
 */
public class Area {
    public final String Name;

    public final Color Color;

    public final int BonusArmies;

    public final ArrayList<Territory> territories;

    public Area(String Name, String hexColor, int BonusArmies) {
        this.Name = Name;
        this.Color = javafx.scene.paint.Color.web(hexColor);
        this.BonusArmies = BonusArmies;
        territories = null;
    }

    public int getOwnerId() {
        int id = territories.get(0).getOwner().getId();

        for (Territory t: territories) {
            if(t.getOwner().getId() != id)
                return -1;
        }

        return id;
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
