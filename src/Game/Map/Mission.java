package Game.Map;

import Game.Map.Army.Color;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Created by fiore on 19/12/2016.
 */
public class Mission {
    public final String Name;

    private final Maps map;

    public String getDescription(Locale Locale) {
        return ResourceBundle.getBundle(map + ".Resources", Locale).getString(Name);
    }

    public final transient ArrayList<Territory> ToConquer;

    public final transient Integer Number;

    public final transient Color Army;

    public final MissionType Type;

    private Mission(Maps Map, String Name, ArrayList<Territory> Territories, int Number, Color Army, MissionType Type) {
        this.map = Map;
        this.Name = Name;
        this.ToConquer = Territories;
        this.Number = Number;
        this.Army = Army;
        this.Type = Type;
    }

    /**
     * Change mission type from Destroy to Number if the army to destroy is not present in the match
     */
    public void changeToNumber() {
        if(Type != MissionType.Destroy)
            return;

        try {
            final Field type = Mission.class.getDeclaredField("Type");
            type.setAccessible(true);
            type.set(this, MissionType.Number);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public enum MissionType {
        Conquer,
        Destroy,
        Number,
        Areas
    }
}
