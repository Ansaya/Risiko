package Game.Map;

import Game.Logger;
import Game.Map.Army.Color;
import Game.Player;
import com.google.gson.*;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Game map
 */
public class Map<T extends Territory> {

    public final Maps Id;

    public final double PrefWidth;

    public final double PrefHeight;

    private final HashMap<String, Area> areas;

    private final HashMap<String, T> territories;

    public T getTerritory(Territory Territory) {
        return getTerritory(Territory.Id);
    }

    public T getTerritory(String Id) {
        return territories.get(Id);
    }

    public ArrayList<String> getTerritoryIds() {
        return new ArrayList<>(territories.keySet());
    }

    public ArrayList<T> getTerritories() { return new ArrayList<>(territories.values()); }

    public final String ConnectionsPath;

    private final Deck<Mission> missionDeck;

    private final AtomicInteger bonus = new AtomicInteger(4);

    private final Deck<Card> cardDeck;

    /**
     * Load requested map from relative json file
     *
     * @param Id Name of the map
     * @param TerritoryExtender Type of territory class to use for this map
     * @throws NoSuchFieldException If map can not be found
     */
    public Map(Maps Id, Class<T> TerritoryExtender) throws NoSuchFieldException {
        // Load json map filed from resources
        final JsonObject map = (JsonObject) (new JsonParser()).parse(new InputStreamReader(Map.class.getResourceAsStream(Id.name() + "/" + Id.name() + ".json")));

        // Initialize all fields to empty value
        this.Id = Id;
        this.PrefWidth = map.get("prefWidth").getAsDouble();
        this.PrefHeight = map.get("prefHeight").getAsDouble();
        this.areas = new HashMap<>();
        this.territories = new HashMap<>();
        this.ConnectionsPath = "";
        this.cardDeck = null;
        this.missionDeck = null;

        // Gain access to private constructor of class Area
        Constructor<Area> areaConstructor;
        try {
            areaConstructor = Area.class.getDeclaredConstructor(String.class, String.class, int.class, ArrayList.class);
        } catch (NoSuchMethodException e){
            e.printStackTrace();
            return;
        }
        areaConstructor.setAccessible(true);

        // Gain access to all final/private fields of Territory class
        final Field territoryId = Territory.class.getDeclaredField("Id");
        final Field territoryAdjacent = Territory.class.getDeclaredField("adjacent");
        final Field territoryArea = Territory.class.getDeclaredField("Area");
        territoryId.setAccessible(true);
        territoryAdjacent.setAccessible(true);
        territoryArea.setAccessible(true);

        // Basic initialization of each territory
        final JsonArray jTerritories = map.getAsJsonArray("territories");
        jTerritories.forEach(t -> {
            // Get json territory
            JsonObject jt = t.getAsJsonObject();
            T territory;
            try {
                territory = TerritoryExtender.newInstance();
                territoryId.set(territory, jt.get("Id").getAsString());
            } catch (IllegalAccessException | InstantiationException e) {
                Logger.err("Error loading map", e);
                return;
            }
            // If no svgPath, it is only a card
            if(jt.has("SvgPath"))
                territories.put(territory.Id, territory);
        });

        // Initialize all areas with initialized territories
        final JsonArray jAreas = map.getAsJsonArray("areas");
        jAreas.forEach(a -> {
            JsonObject ja = a.getAsJsonObject();

            final ArrayList<Territory> states = new ArrayList<>();
            ja.getAsJsonArray("territories").forEach(t -> states.add(this.territories.get(t.getAsString())));

            final Area area;
            try {
                area = areaConstructor.newInstance(ja.get("Id").getAsString(),
                        ja.get("Color").getAsString(),
                        ja.get("BonusArmies").getAsInt(),
                        states);
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
                return;
            }

            states.forEach(territory -> {
                try {
                    territoryArea.set(territory, area);
                } catch (IllegalAccessException e) {
                    Logger.err("Error loading map on " + territory.Id, e);
                }
            });

            areas.put(area.Id, area);
        });

        // Complete territories initialization with adjacent territories and areas
        jTerritories.forEach(t -> {
            JsonObject jt = t.getAsJsonObject();
            if(!jt.has("adjacent"))
                return;

            T territory = this.territories.get(jt.get("Id").getAsString());

            final ArrayList<T> adjacent = new ArrayList<>();
            jt.getAsJsonArray("adjacent").forEach(a -> adjacent.add(this.territories.get(a.getAsString())));

            try {
                territoryAdjacent.set(territory, adjacent);
            } catch (IllegalAccessException e) {
                Logger.err("Error loading map", e);
            }
        });
    }

    /**
     * Load all graphic information for map territories
     *
     * @throws NoSuchFieldException Cannot find map json file
     * @throws IllegalAccessException Cannot access territory properties
     */
    public void loadGraphic(Locale Locale) throws NoSuchFieldException, IllegalAccessException {
        final JsonObject map = (JsonObject) (new JsonParser()).parse(new InputStreamReader(Map.class.getResourceAsStream(Id + "/" + Id + ".json")));
        final ResourceBundle resources = ResourceBundle.getBundle("Game.Map." + Id + ".Resources", Locale);

        // Gain access to map connection field
        final Field connPath = Map.class.getDeclaredField("ConnectionsPath");
        connPath.setAccessible(true);

        if(map.has("connectionsPath"))
            connPath.set(this, map.get("connectionsPath").getAsString());

        // Gain access to territory fields
        final Field territoryName = Territory.class.getDeclaredField("Name");
        final Field territorySvgPath = Territory.class.getDeclaredField("SvgPath");
        final Field territoryArmyX = Territory.class.getDeclaredField("ArmyX");
        final Field territoryArmyY = Territory.class.getDeclaredField("ArmyY");
        final Field territoryLabelX = Territory.class.getDeclaredField("LabelX");
        final Field territoryLabelY = Territory.class.getDeclaredField("LabelY");
        final Field territoryLabelR = Territory.class.getDeclaredField("LabelR");
        territoryName.setAccessible(true);
        territorySvgPath.setAccessible(true);
        territoryArmyX.setAccessible(true);
        territoryArmyY.setAccessible(true);
        territoryLabelX.setAccessible(true);
        territoryLabelY.setAccessible(true);
        territoryLabelR.setAccessible(true);

        // Gain access to area name field
        final Field areaName = Area.class.getDeclaredField("Name");
        areaName.setAccessible(true);

        // Load graphic for each territory
        final JsonArray jTerritories = map.getAsJsonArray("territories");
        jTerritories.forEach(t -> {
            JsonObject jt = t.getAsJsonObject();
            Territory territory = territories.get(jt.get("Id").getAsString());
            if(territory == null) return;

            try {
                territoryName.set(territory, resources.getString(territory.Id));
                if (jt.has("SvgPath"))
                    territorySvgPath.set(territory, jt.get("SvgPath").getAsString());
                if (jt.has("ArmyX"))
                    territoryArmyX.set(territory, jt.get("ArmyX").getAsFloat());
                if (jt.has("ArmyY"))
                    territoryArmyY.set(territory, jt.get("ArmyY").getAsFloat());
                if(jt.has("LabelX"))
                    territoryLabelX.set(territory, jt.get("LabelX").getAsFloat());
                if(jt.has("LabelY"))
                    territoryLabelY.set(territory, jt.get("LabelY").getAsFloat());
                if(jt.has("LabelR"))
                    territoryLabelR.set(territory, jt.get("LabelR").getAsFloat());
            } catch (IllegalAccessException e) {
                Logger.err("Error loading map graphics", e);
            }
        });

        areas.forEach((name, area) -> {
            try {
                areaName.set(area, resources.getString(area.Id));
            } catch (IllegalAccessException e) {
                Logger.err("Error loading map graphics", e);
            }
        });
    }

    /**
     * Load cards into mission deck
     *
     * @throws NoSuchFieldException Cannot find map json file
     * @throws IllegalAccessException Cannot access deck properties
     */
    private void loadMissionDeck() throws NoSuchMethodException, NoSuchFieldException, IllegalAccessException {
        // Load json map filed from resources
        final JsonObject map = (JsonObject) (new JsonParser()).parse(new InputStreamReader(Map.class.getResourceAsStream(Id + "/" + Id + ".json")));

        final ArrayList<Mission> missions = new ArrayList<>();

        // Gain access to mission deck filed of map
        final Field missionDeck = Map.class.getDeclaredField("missionDeck");
        missionDeck.setAccessible(true);

        //Gain access to mission constructor
        final Constructor<Mission> missionConstructor = Mission.class.getDeclaredConstructor(Maps.class, String.class, ArrayList.class, int.class, Color.class, Mission.MissionType.class);
        missionConstructor.setAccessible(true);

        // Initialize each mission with initialized territories
        final JsonArray jMissions = map.getAsJsonArray("missions");
        jMissions.forEach(m -> {
            JsonObject jm = m.getAsJsonObject();

            Mission mission;
            final ArrayList<T> toConquer = new ArrayList<>();
            int number = 0;
            Color army = null;

            if(jm.has("ToConquer"))
                jm.getAsJsonArray("ToConquer").forEach(t -> toConquer.add(territories.get(t.getAsString())));

            if(jm.has("Number"))
                number = jm.get("Number").getAsInt();

            if(jm.has("Army"))
                army = Color.valueOf(jm.get("Army").getAsString());

            try {
                mission = missionConstructor.newInstance(this.Id,
                        jm.get("Id").getAsString(),
                        toConquer.size() > 0 ? toConquer : null,
                        number,
                        army,
                        Mission.MissionType.valueOf(jm.get("Type").getAsString()));
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                Logger.err("Error loading mission deck", e);
                return;
            }

            missions.add(mission);
        });

        missionDeck.set(this, new Deck<>(missions));
    }

    /**
     * Load cards into territory deck
     *
     * @throws NoSuchFieldException Cannot find map json file
     * @throws IllegalAccessException Cannot access deck properties
     */
    private void loadCardDeck() throws NoSuchFieldException, NoSuchMethodException, IllegalAccessException {
        // Load json map filed from resources
        final JsonObject map = (JsonObject) (new JsonParser()).parse(new InputStreamReader(Map.class.getResourceAsStream(Id + "/" + Id + ".json")));

        final ArrayList<Card> cards = new ArrayList<>();

        // Gain access to deck field
        final Field cardDeck = Map.class.getDeclaredField("cardDeck");
        cardDeck.setAccessible(true);

        // Gain access to cards constructor
        final Constructor<Card> cardConstructor = Card.class.getDeclaredConstructor(String.class, Figure.class, Maps.class);
        cardConstructor.setAccessible(true);

        final JsonArray jTerritories = map.getAsJsonArray("territories");
        jTerritories.forEach(t -> {
            // Get json territory
            JsonObject jt = t.getAsJsonObject();

            if(jt.has("Card"))
                try {
                    cards.add(cardConstructor.newInstance(jt.get("Id").getAsString(), Figure.valueOf(jt.get("Card").getAsString()), this.Id));
                } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                    Logger.err("Error loading card deck", e);
                }
        });

        cardDeck.set(this, new Deck<>(cards));
    }

    /**
     * Load decks for this map object
     */
    public void loadDecks() {
        try {
            loadCardDeck();
            loadMissionDeck();
        } catch (NoSuchMethodException | NoSuchFieldException | IllegalAccessException e) {
            Logger.err("Error loading decks", e);
        }
    }

    /**
     * Pick next card from territory deck
     * @return Card from deck
     */
    public Card nextCard() {
        if(cardDeck == null) {
            try {
                loadCardDeck();
            } catch (NoSuchMethodException | NoSuchFieldException | IllegalAccessException e) {
                e.printStackTrace();
                return null;
            }
        }

        return cardDeck.next();
    }

    /**
     * Put given cards back to cards deck
     *
     * @param Cards Cards to return
     */
    public void returnCards(Collection<Card> Cards) {
        cardDeck.setBack(Cards.toArray(new Card[Cards.size()]));
    }

    /**
     * Pick next card from mission deck
     * @return Next mission from deck
     */
    public Mission nextMission() {
        if(missionDeck == null) {
            try {
                loadMissionDeck();
            } catch (NoSuchMethodException | NoSuchFieldException | IllegalAccessException e) {
                e.printStackTrace();
                return null;
            }
        }

        return missionDeck.next();
    }

    /**
     * Check if specified player has completed his mission
     *
     * @param Player Player to check mission for
     * @param Mission Mission to check on given player
     * @return True if mission accomplished, false if not
     */
    public boolean checkMission(Player Player, Mission Mission) {
        if(Mission == null || Player == null)
            return false;

        switch (Mission.Type){
            case Conquer:
                for (Territory t: Mission.ToConquer) {
                    if(t.getOwner().getId() != Player.getId())
                        return false;
                }
                return true;
            case Destroy:
                for (Territory t: territories.values()) {
                    if(t.getOwner().getColor() == Mission.Army)
                        return false;
                }
                return true;
            case Number:
                // Owned territory counter
                final AtomicInteger i = new AtomicInteger(0);

                // Number of armies on territory to be counted
                final int armies = Mission.Army.ordinal();

                territories.forEach((name, territory) -> {
                    if(territory.getOwner().getId() == Player.getId() && territory.getArmies() >= armies)
                        i.incrementAndGet();
                });
                return i.get() >= Mission.Number;
            case Areas:
                boolean baseAreas = true;
                for (Territory t: Mission.ToConquer) {
                    if(t.getOwner().getId() != Player.getId())
                        baseAreas = false;
                }
                return baseAreas && dominatedAreas(Player) >= Mission.Number;
        }

        return false;
    }

    /**
     * Check owned areas and return total bonus armies for the player
     *
     * @param Player Player to evaluate
     * @return Number of bonus armies
     */
    public int getAreasBonus(Player Player){
        final AtomicInteger bonus = new AtomicInteger();
        this.areas.forEach((name, area) -> {
            if(area.getOwnerId() == Player.getId())
                bonus.addAndGet(area.BonusArmies);
        });

        return bonus.get();
    }

    /**
     * Count number of dominated areas of the player
     *
     * @param Player Player to check
     * @return Number of dominated areas
     */
    public int dominatedAreas(Player Player) {
        final AtomicInteger areas = new AtomicInteger(0);
        this.areas.forEach((name, area) -> {
            if(area.getOwnerId() == Player.getId())
                areas.incrementAndGet();
        });

        return areas.get();
    }

    /**
     * Check if card combination is valid
     *
     * @param Cards Three cards list
     * @return Number of bonus armies if combination is valid, zero otherwise
     */
    public int playCards(ArrayList<Card> Cards) {
        if(!Card.isCombinationValid(Cards))
            return 0;

        int armies = this.bonus.get();

        // Increment bonus armies by 2 till 12 then from 15 by 5 each time
        if(bonus.get() < 12)
            bonus.getAndAdd(2);
        else {
            bonus.compareAndSet(12, 13);
            bonus.getAndAdd(5);
        }

        // Add redeemed cards to the end of the deck
        cardDeck.setBack(Cards.toArray(new Card[Cards.size()]));

        return armies;

    }
}
