package Game.Map;

import Game.Map.Army.Color;
import java.lang.reflect.Field;
import java.util.ArrayList;

/**
 * Created by fiore on 19/12/2016.
 */
public class Mission {
    public final String Name;

    public final String Description;

    public final transient ArrayList<Territory> ToConquer;

    public final transient Integer Number;

    public final transient Color Army;

    public final MissionType Type;

    public Mission(){
        this.Name = "";
        this.Description = "";
        this.ToConquer = null;
        this.Number = 0;
        this.Army = null;
        this.Type = null;
    }

    public void changeToNumber() {
        if(Type != MissionType.Destroy)
            return;

        try {
            final Field type = Mission.class.getDeclaredField("Type");
            type.setAccessible(true);
            type.set(this, MissionType.Number);
        } catch (NoSuchFieldException | IllegalAccessException e) {}
    }

    public enum MissionType {
        Conquer,
        Destroy,
        Number,
        Special
    }
}
