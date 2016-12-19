package Game.Map;

import Game.Map.Army.Color;
import java.util.*;
import static Game.Map.RealWorldMap.*;

/**
 * List of missions for earth map
 */
public enum Mission {
    EuropeAustraliaContinent("You have to conquer entire Europe, Australia and another continent of your choice.", 3, GreatBritain, Iceland, NorthernEurope, Scandinavia, SouthernEurope, Ukraine, WesternEurope, EasternAustralia, Indonesia, NewGuinea, WesternAustralia),
    EuropeSouthAmericaContinent("You have to conquer entire Europe, South America and another continent of your choice.", 3, GreatBritain, Iceland, NorthernEurope, Scandinavia, SouthernEurope, Ukraine, WesternEurope, Argentina, Brazil, Peru, Venezuela),
    NorthAmericaAfrica("You have to conquer entire North America and Africa.", Alaska, Alberta, CentralAmerica, WesternUnitedStates, Greenland, NorthwestTerritory, Ontario, Quebec, EasternUnitedStates, Congo, EastAfrica, Egypt, Madagascar, NorthAfrica, SouthAfrica),
    NorthAmericaAustralia("You have to conquer entire North America and Australia.", Alaska, Alberta, CentralAmerica, WesternUnitedStates, Greenland, NorthwestTerritory, Ontario, Quebec, EasternUnitedStates, EasternAustralia, Indonesia, NewGuinea, WesternAustralia),
    AsiaSouthAmerica("You have to conquer entire Asia and South America.", Afghanistan, China, India, Irkutsk, Japan, Kamchatka, MiddleEast, Mongolia, Siam, Siberia, Ural, Yakutsk, Argentina, Brazil, Peru, Venezuela),
    AsiaAfrica("You have to conquer entire Asia and Africa.", Afghanistan, China, India, Irkutsk, Japan, Kamchatka, MiddleEast, Mongolia, Siam, Siberia, Ural, Yakutsk, Congo, EastAfrica, Egypt, Madagascar, NorthAfrica, SouthAfrica),
    Territory24("You have to conquer 24 territories.", 24),
    Territory18Two("You have to conquer 18 territories and place two armies on each.", 18),
    DestroyRed("You have to destroy red armies.", Color.RED),
    DestroyYellow("You have to destroy yellow armies.", Color.YELLOW),
    DestroyGreen("You have to destroy green armies.", Color.GREEN),
    DestroyBlue("You have to destroy blue armies.", Color.BLUE),
    DestroyBlack("You have to destroy black armies.", Color.BLACK),
    DestroyPurple("You have to destroy purple armies.", Color.PURPLE),
    Multiple1("You have to conquer:\r\n" +
            "AFRICA\r\n" +
            "Madagascar - East Africa - North Africa\r\n" +
            "SOUTH AMERICA\r\n" +
            "Brazil - Venezuela\r\n" +
            "NORTH AMERICA\r\n" +
            "Central America - Eastern United States - Ontario - Alberta\r\n" +
            "ASIA\r\n" +
            "Kamchatka - Japan - Yakutsk - Siberia - Ural",
            Madagascar, EastAfrica, NorthAfrica, Brazil, Venezuela, CentralAmerica, EasternUnitedStates, Ontario, Alberta, Kamchatka, Japan, Yakutsk, Siberia, Ural),
    Multiple2("You have to conquer:\r\n" +
            "SOUTH AMERICA\r\n" +
            "Peru - Venezuela\r\n" +
            "NORTH AMERICA\r\n" +
            "Central America - Western United States - Ontario - Northwest Territory - Alaska\r\n" +
            "ASIA\r\n" +
            "Kamchatka - Irkutsk - Mongolia - China - Afghanistan - Siam\r\n" +
            "AUSTRALIA\r\n" +
            "Indonesia - Western Australia",
            Peru, Venezuela, CentralAmerica, WesternUnitedStates, Ontario, NorthwestTerritory, Alaska, Kamchatka, Irkutsk, Mongolia, China, Afghanistan, Siam, Indonesia, WesternAustralia),
    Multiple3("You have to conquer:\r\n" +
            "SOUTH AMERICA\r\n" +
            "Peru - Venezuela\r\n" +
            "NORTH AMERICA\r\n" +
            "Central America - Western United States - Ontario - Quebec - Greenland\r\n" +
            "EUROPE\r\n" +
            "Iceland - Scandinavia - Ukraine\r\n" +
            "ASIA\r\n" +
            "Ural - Siberia - Afghanistan - Middle East - India",
            Peru, Venezuela, CentralAmerica, WesternUnitedStates, Ontario, Quebec, Greenland, Iceland, Scandinavia, Ukraine, Ural, Siberia, Afghanistan, MiddleEast, India),
    Multiple4("You have to conquer:\r\n" +
            "AUSTRALIA\r\n" +
            "New Guinea - Indonesia\r\n" +
            "ASIA\r\n" +
            "Siam - China - Siberia - Yakutsk - Kamchatka\r\n" +
            "NORTH AMERICA\r\n" +
            "Alaska - Northwest Territory - Quebec - Greenland - Western United States\r\n" +
            "EUROPE\r\n" +
            "Iceland - Great Britain - Northern Europe",
            NewGuinea, Indonesia, Siam, China, Siberia, Yakutsk, Kamchatka, Alaska, NorthwestTerritory, Quebec, Greenland, WesternUnitedStates, Iceland, GreatBritain, NorthernEurope),
    Multiple5("You have to conquer:\r\n" +
            "SOUTH AMERICA\r\n" +
            "Argentina - Brazil\r\n" +
            "AFRICA\r\n" +
            "North Africa - Congo - Egypt\r\n" +
            "EUROPE\r\n" +
            "Western Europe - Northern Europe - Scandinavia - Ukraine\r\n" +
            "ASIA\r\n" +
            "Ural - China - Mongolia - India - Japan - Siam",
            Argentina, Brazil, NorthAfrica, Congo, Egypt, WesternEurope, NorthernEurope, Scandinavia, Ukraine, Ural, China, Mongolia, India, Japan, Siam),
    Multiple6("You have to conquer:\r\n" +
            "AFRICA\r\n" +
            "Madagascar - East Africa - North Africa\r\n" +
            "EUROPE\r\n" +
            "Southern Europe - Western Europe - Scandinavia - Great Britain\r\n" +
            "ASIA\r\n" +
            "Middle East - China - Ural - Siberia - Siam\r\n" +
            "AUSTRALIA\r\n" +
            "Indonesia - Western Australia",
            Madagascar, EastAfrica, NorthAfrica, SouthernEurope, WesternEurope, Scandinavia, GreatBritain, MiddleEast, China, Ural, Siberia, Siam, Indonesia, WesternAustralia),
    Multiple7("You have to conquer:\r\n" +
            "AFRICA\r\n" +
            "South Africa - East Africa - Egypt\r\n" +
            "EUROPE\r\n" +
            "Southern Europe - Western Europe - Ukraine\r\n" +
            "ASIA\r\n" +
            "Middle East - China - Mongolia - Kamchatka\r\n" +
            "NORTH AMERICA\r\n" +
            "Alaska - Alberta - Ontario - Eastern United States - Greenland",
            SouthAfrica, EastAfrica, Egypt, SouthernEurope, WesternEurope, Ukraine, MiddleEast, China, Mongolia, Kamchatka, Alaska, Alberta, Ontario, EasternUnitedStates, Greenland),
    Multiple8("You have to conquer:\r\n" +
            "SOUTH AMERICA\r\n" +
            "Argentina - Brazil\r\n" +
            "AFRICA\r\n" +
            "North Africa - Congo - South Africa\r\n" +
            "EUROPE\r\n" +
            "Northern Europe - Southern Europe - Scandinavia - Iceland\r\n" +
            "NORTH AMERICA\r\n" +
            "Greenland - Quebec - Northwest Territory - Alberta - Western United States - Central America",
            Argentina, Brazil, NorthAfrica, Congo, SouthAfrica, NorthernEurope, SouthernEurope, Scandinavia, Iceland, Greenland, Quebec, NorthwestTerritory, Alberta, WesternUnitedStates, CentralAmerica);

    public final String Description;

    private final ArrayList<RealWorldMap> ToConquer;

    public ArrayList<RealWorldMap> getToConquer() { return ToConquer.getClass().cast(ToConquer.clone()); }

    public final Integer Number;

    public final Color Army;

    public final MissionType Type;

    Mission(String Description, RealWorldMap... ToConquer) {
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
        this.Number = 24;
        this.Army = Army;
        this.Type = MissionType.Destroy;
    }

    Mission(String Description, Integer Continents, RealWorldMap... ToConquer) {
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
        Special
    }
}
