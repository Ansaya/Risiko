package Game.Map;

import Game.Map.Army.Color;
import Game.Player;
import com.google.gson.*;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by fiore on 18/12/2016.
 */
public class Map<T extends Territory<? extends Player>> {

    public final String Name;

    private final HashMap<String, Area> areas;

    private final HashMap<String, T> territories;

    public T getTerritory(String Name) {
        return territories.get(Name);
    }

    public ArrayList<String> getTerritoryNames() {
        return new ArrayList<>(territories.keySet());
    }

    public ArrayList<T> getTerritories() { return new ArrayList<>(territories.values()); }

    public final String ConnectionsPath;

    private final Deck<Mission> missionDeck;

    private final AtomicInteger bonus = new AtomicInteger(4);

    private final Deck<Card> cardDeck;

    public Map(String Name, Class<T> TerritoryExtender) throws NoSuchFieldException {
        // Load json map filed from resources
        final JsonObject map = (JsonObject) (new JsonParser()).parse(new InputStreamReader(Map.class.getResourceAsStream(Name + "/" + Name + ".json")));

        this.Name = Name;
        this.areas = new HashMap<>();
        this.territories = new HashMap<>();
        this.ConnectionsPath = "";
        final ArrayList<Mission> missions = new ArrayList<>();
        final ArrayList<Card> cards = new ArrayList<>();

        // Gain access to private final field territories of class Area
        final Field areaTerritories = Area.class.getDeclaredField("territories");
        areaTerritories.setAccessible(true);

        // Gain access to all final/private fields of Territory class
        final Field territoryName = Territory.class.getDeclaredField("Name");
        final Field territoryAdjacent = Territory.class.getDeclaredField("adjacent");
        final Field territoryArea = Territory.class.getDeclaredField("Area");
        territoryName.setAccessible(true);
        territoryAdjacent.setAccessible(true);
        territoryArea.setAccessible(true);

        // Gain access to all final/private fields of Mission class
        final Field missionName = Mission.class.getDeclaredField("Name");
        final Field missionDescription = Mission.class.getDeclaredField("Description");
        final Field missionToConquer = Mission.class.getDeclaredField("ToConquer");
        final Field missionNumber = Mission.class.getDeclaredField("Number");
        final Field missionArmy = Mission.class.getDeclaredField("Army");
        final Field missionType = Mission.class.getDeclaredField("Type");
        missionName.setAccessible(true);
        missionDescription.setAccessible(true);
        missionToConquer.setAccessible(true);
        missionNumber.setAccessible(true);
        missionArmy.setAccessible(true);
        missionType.setAccessible(true);

        // Basic initialization of each territory
        final JsonArray jTerritories = map.getAsJsonArray("territories");
        jTerritories.forEach(t -> {
            // Get json territory
            JsonObject jt = t.getAsJsonObject();
            T territory = null;
            try { territory = TerritoryExtender.newInstance(); } catch (IllegalAccessException | InstantiationException e) {}

            // Initialize all fields available
            try {
                territoryName.set(territory, jt.get("Name").getAsString());
            } catch (IllegalAccessException e) {}

            // If no svgPath, it is only a card
            if(jt.has("SvgPath"))
                territories.put(territory.Name, territory);

            if(jt.has("Card"))
                cards.add(new Card(territory.Name, Figure.valueOf(jt.get("Card").getAsString()), Name));
        });

        this.cardDeck = new Deck<>(cards);

        // Initialize all areas with initialized territories
        final JsonArray jAreas = map.getAsJsonArray("areas");
        jAreas.forEach(a -> {
            JsonObject ja = a.getAsJsonObject();
            Area area = new Area(ja.get("Name").getAsString(), ja.get("Color").getAsString(), ja.get("BonusArmies").getAsInt());

            final ArrayList<Territory> states = new ArrayList<>();
            ja.getAsJsonArray("territories").forEach(t -> states.add(this.territories.get(t.getAsString())));

            try { areaTerritories.set(area, states); } catch (IllegalAccessException e) {}

            areas.put(area.Name, area);
        });

        // Complete territories initialization with adjacent territories and areas
        jTerritories.forEach(t -> {
            JsonObject jt = t.getAsJsonObject();
            if(!jt.has("adjacent"))
                return;

            T territory = this.territories.get(jt.get("Name").getAsString());

            final ArrayList<T> adjacent = new ArrayList<>();
            jt.getAsJsonArray("adjacent").forEach(a -> adjacent.add(this.territories.get(a.getAsString())));

            try {
                territoryAdjacent.set(territory, adjacent);
                territoryArea.set(territory, this.areas.get(jt.get("Area").getAsString()));
            } catch (IllegalAccessException e) {}
        });

        // Initialize each mission with initialized territories
        final JsonArray jMissions = map.getAsJsonArray("missions");
        jMissions.forEach(m -> {
            JsonObject jm = m.getAsJsonObject();

            Mission mission = null;
            try { mission = Mission.class.newInstance(); } catch (Exception e) {}

            try {
                missionName.set(mission, jm.get("Name").getAsString());
                missionDescription.set(mission, jm.get("Description").getAsString());
                missionType.set(mission, Mission.MissionType.valueOf(jm.get("Type").getAsString()));
                if(jm.has("Number"))
                    missionNumber.set(mission, jm.get("Number").getAsInt());
                if(jm.has("Army"))
                    missionArmy.set(mission, Color.valueOf(jm.get("Army").getAsString()));
                if(jm.has("ToConquer")){
                    final ArrayList<T> toConquer = new ArrayList<>();
                    jm.getAsJsonArray("ToConquer").forEach(t -> toConquer.add(territories.get(t.getAsString())));
                    missionToConquer.set(mission, toConquer);
                }
            } catch (IllegalAccessException e) {}

            missions.add(mission);
        });

        this.missionDeck = new Deck<>(missions);
    }

    public void loadGraphic() throws NoSuchFieldException, IllegalAccessException {
        final JsonObject map = (JsonObject) (new JsonParser()).parse(new InputStreamReader(Map.class.getResourceAsStream(Name + "/" + Name + ".json")));

        final Field connPath = Map.class.getDeclaredField("ConnectionsPath");
        connPath.setAccessible(true);

        if(map.has("connectionsPath"))
            connPath.set(this, map.get("connectionsPath").getAsString());

        final Field territorySvgPath = Territory.class.getDeclaredField("SvgPath");
        final Field territoryArmyX = Territory.class.getDeclaredField("ArmyX");
        final Field territoryArmyY = Territory.class.getDeclaredField("ArmyY");
        territorySvgPath.setAccessible(true);
        territoryArmyX.setAccessible(true);
        territoryArmyY.setAccessible(true);

        final JsonArray jTerritories = map.getAsJsonArray("territories");
        jTerritories.forEach(t -> {
            JsonObject jt = t.getAsJsonObject();

            Territory territory = territories.get(jt.get("Name").getAsString());

            try {
                if (jt.has("SvgPath"))
                    territorySvgPath.set(territory, jt.get("SvgPath").getAsString());
                if (jt.has("ArmyX"))
                    territoryArmyX.set(territory, jt.get("ArmyX").getAsFloat());
                if (jt.has("ArmyY"))
                    territoryArmyY.set(territory, jt.get("ArmyY").getAsFloat());
            } catch (IllegalAccessException e) {}
        });

        final Field allCards = Deck.class.getDeclaredField("allCards");
        allCards.setAccessible(true);

        ArrayList<Card> cards = (ArrayList<Card>) allCards.get(cardDeck);
        cards.forEach(Card::loadGraphic);
    }

    public Card nextCard() { return cardDeck.next(); }

    public Mission nextMission() {
        return missionDeck.next();
    }

    /**
     * Check if specified player has completed his Mission
     *
     * @param Player Player to check mission for
     * @param Mission Mission to check on given player
     * @return True if Mission accomplished, false if not
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
                final AtomicInteger i = new AtomicInteger(0);
                territories.forEach((name, territory) -> {
                    if(territory.getOwner().getId() == Player.getId())
                        i.incrementAndGet();
                });
                return i.get() >= Mission.Number;
            case Special:
                boolean baseAreas = true;
                for (Territory t: Mission.ToConquer) {
                    if(t.getOwner().getId() != Player.getId())
                        baseAreas = false;
                }
                return baseAreas && dominatedAreas(Player) >= Mission.Number;
        }

        return false;
    }

    public int getAreasBonus(Player Player){
        final AtomicInteger bonus = new AtomicInteger();
        this.areas.forEach((name, area) -> {
            if(area.getOwnerId() == Player.getId())
                bonus.addAndGet(area.BonusArmies);
        });

        return bonus.get();
    }

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

        // Increment bonus Armies by 2 till 12 then From 15 by 5 each time
        if(bonus.get() < 12)
            bonus.getAndAdd(2);
        else {
            bonus.compareAndSet(12, 13);
            bonus.getAndAdd(5);
        }

        // Add redeemed cards To the end of the deck
        cardDeck.setBack(Cards.toArray(new Card[Cards.size()]));

        return armies;

    }
}
