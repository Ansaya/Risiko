package Game.Map;

import javafx.scene.paint.Color;
import java.util.ArrayList;

/**
 * Created by fiore on 18/12/2016.
 */
public class Area {

    public final String Id;

    public final transient String Name;

    public final Color Color;

    public final int BonusArmies;

    public final ArrayList<Territory> territories;

    private Area(String Id, String hexColor, int BonusArmies, ArrayList<Territory> Territories) {
        this.Id = Id;
        this.Name = "";
        this.Color = javafx.scene.paint.Color.web(hexColor);
        this.BonusArmies = BonusArmies;
        this.territories = Territories;
    }

    /**
     * If a player owns all the area return its id
     * @return Owner id or -1 if multiple owners are present
     */
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
        return other instanceof Area && ((Area)other).Id.equals(this.Id);
    }

    @Override
    public String toString() {
        return Name;
    }
}
