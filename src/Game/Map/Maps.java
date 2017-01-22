package Game.Map;

import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Created by fiore on 12/01/2017.
 */
public enum Maps {
    RealWorldMap,
    ClassicRisikoMap;

    private String Name;

    private static Locale loadedLocale;

    public static String getName(Maps Map, Locale Locale) {
        if(Map.Name != null && Locale.equals(loadedLocale))
            return Map.Name;

        loadedLocale = Locale;

        for (Maps m: Maps.values()) {
            m.Name = ResourceBundle.getBundle("Game.Map." + m.name() + ".Resources", loadedLocale).getString("Name");
        }

        return getName(Map, Locale);
    }
}
