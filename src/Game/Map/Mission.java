package Game.Map;

import Game.Color;
import java.util.*;
import static Game.Map.Territories.*;

/**
 * List of missions for earth map
 */
public enum Mission {
    EuropeAustraliaContinent("", 3, GreatBritain, Iceland, NorthernEurope, Scandinavia, SouthernEurope, Ukraine, WesternEurope, EasternAustralia, Indonesia, NewGuinea, WesternAustralia),
    EuropeSouthAmericaContinent("", 3, GreatBritain, Iceland, NorthernEurope, Scandinavia, SouthernEurope, Ukraine, WesternEurope, Argentina, Brazil, Peru, Venezuela),
    NorthAmericaAfrica("", Alaska, Alberta, CentralAmerica, WesternUnitedStates, Greenland, NorthwestTerritory, Ontario, Quebec, EasternUnitedStates, Congo, EastAfrica, Egypt, Madagascar, NorthAfrica, SouthAfrica),
    NorthAmericaAustralia("", Alaska, Alberta, CentralAmerica, WesternUnitedStates, Greenland, NorthwestTerritory, Ontario, Quebec, EasternUnitedStates, EasternAustralia, Indonesia, NewGuinea, WesternAustralia),
    AsiaSouthAmerica("", Afghanistan, China, India, Irkutsk, Japan, Kamchatka, MiddleEast, Mongolia, Siam, Siberia, Ural, Yakutsk, Argentina, Brazil, Peru, Venezuela),
    AsiaAfrica("", Afghanistan, China, India, Irkutsk, Japan, Kamchatka, MiddleEast, Mongolia, Siam, Siberia, Ural, Yakutsk, Congo, EastAfrica, Egypt, Madagascar, NorthAfrica, SouthAfrica),
    Territory24("", 24),
    Territory18Two("", 18),
    DestroyRed("", Color.RED),
    DestroyYellow("", Color.YELLOW),
    DestroyGreen("", Color.GREEN),
    DestroyBlue("", Color.BLUE),
    DestroyBlack("", Color.BLACK),
    DestroyPurple("", Color.PURPLE),
    Multiple1("", Madagascar, EastAfrica, NorthAfrica, Brazil, Venezuela, CentralAmerica, EasternUnitedStates, Ontario, Alberta, Kamchatka, Japan, Yakutsk, Siberia, Ural),
    Multiple2("", Peru, Venezuela, CentralAmerica, WesternUnitedStates, Ontario, NorthwestTerritory, Alaska, Kamchatka, Irkutsk, Mongolia, China, Afghanistan, Siam, Indonesia, WesternAustralia),
    Multiple3("", Peru, Venezuela, CentralAmerica, WesternUnitedStates, Ontario, Quebec, Greenland, Iceland, Scandinavia, Ukraine, Ural, Siberia, Afghanistan, MiddleEast, India),
    Multiple4("", NewGuinea, Indonesia, Siam, China, Siberia, Yakutsk, Kamchatka, Alaska, NorthwestTerritory, Quebec, Greenland, WesternUnitedStates, Iceland, GreatBritain, NorthernEurope),
    Multiple5("", Argentina, Brazil, NorthAfrica, Congo, Egypt, WesternEurope, NorthernEurope, Scandinavia, Ukraine, Ural, China, Mongolia, India, Japan, Siam),
    Multiple6("", Madagascar, EastAfrica, NorthAfrica, SouthernEurope, WesternEurope, Scandinavia, GreatBritain, MiddleEast, China, Ural, Siberia, Siam, Indonesia, WesternAustralia),
    Multiple7("", SouthAfrica, EastAfrica, Egypt, SouthernEurope, WesternEurope, Ukraine, MiddleEast, China, Mongolia, Kamchatka, Alaska, Alberta, Ontario, EasternUnitedStates, Greenland),
    Multiple8("", Argentina, Brazil, NorthAfrica, Congo, SouthAfrica, NorthernEurope, SouthernEurope, Scandinavia, Iceland, Greenland, Quebec, NorthwestTerritory, Alberta, WesternUnitedStates, CentralAmerica);

    public final String Description;

    private final ArrayList<Territories> ToConquer;

    public ArrayList<Territories> getToConquer() { return ToConquer.getClass().cast(ToConquer.clone()); }

    public final Integer Number;

    public final Color Army;

    public final MissionType Type;

    Mission(String Description, Territories... ToConquer) {
        this.Description = Description;
        this.ToConquer = new ArrayList<>(Arrays.asList(ToConquer));
        this.Number = ToConquer.length;
        this.Army = null;
        this.Type = MissionType.Conquer;
    }

    Mission(String Description, Integer Number) {
        this.Description = Description;
        this.ToConquer = null;
        this.Number = Number;
        this.Army = null;
        this.Type = MissionType.Number;
    }

    Mission(String Description, Color Army) {
        this.Description = Description;
        this.ToConquer = null;
        this.Number = null;
        this.Army = Army;
        this.Type = MissionType.Destroy;
    }

    Mission(String Description, Integer Continents, Territories... ToConquer) {
        this.Description = Description;
        this.ToConquer = new ArrayList<>(Arrays.asList(ToConquer));
        this.Number = Continents;
        this.Army = null;
        this.Type = MissionType.Special;
    }

    public enum MissionType {
        Conquer,
        Destroy,
        Number,
        Special;
    }
}
