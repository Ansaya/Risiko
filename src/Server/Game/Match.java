package Server.Game;

import Game.Map.*;
import Game.Map.Army.Color;
import Game.Connection.*;
import Game.Connection.Battle;
import Game.Connection.GameState;
import Game.Map.Map;
import Game.Map.Mission;
import Game.MessageReceiver;
import Game.Sounds.Sounds;
import Game.StateType;
import Server.Game.Connection.MessageType;
import Server.Game.Map.Territory;
import com.google.gson.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Match object to manage game turns in a dedicated thread
 */
public class Match extends MessageReceiver<MessageType> {

    public final int Id;

    public final String Name;

    public final Maps GameMap;

    /**
     * Players' list for this match (contains witnesses too)
     */
    private final transient ObservableMap<Integer, Player> players = FXCollections.observableHashMap();

    public ObservableMap<Integer, Player> getPlayers() { return players; }

    /**
     * Add player to this match removing it from lobby
     *
     * @param Player Player to add to the match
     */
    public void addPlayer(Player Player) {
        if(!isStarted() && players.size() >= 6)
            return;

        GameController.getInstance().releasePlayer(Player, false);

        if(!isStarted())
            sendAll(MessageType.Lobby, new Lobby<>(Player, null));

        players.put(Player.getId(), Player);
        Player.enterMatch(this.Id);

        if(!isStarted()) {
            Player.SendMessage(MessageType.Lobby, new Lobby<>(players.values(), null));
        }
        else {
            final ArrayList<Player> playing = new ArrayList<>();
            playersOrder.forEach(id -> playing.add(players.get(id)));
            final Player ai;
            if((ai = players.get(-1)) != null) playing.add(ai);

            Player.SendMessage(MessageType.Match, new Game.Connection.Match<>(Id, Name, GameMap, playing));
        }
    }

    /**
     * Remove player from this match taking care of all cases
     *
     * @param Player Player to remove
     * @param Remove True if player has exited, false if player is going to lobby
     */
    public void removePlayer(Player Player, boolean Remove) {
        // If player is playing terminate match
        if (Player.isPlaying()) {
            sendAll(MessageType.GameState, new GameState<>(StateType.Abandoned, Player));
            GameController.getInstance().endMatch(this);
            return;
        }

        players.remove(Player.getId());

        if(Remove)
            GameController.getInstance().releasePlayer(Player, true);
        else
            GameController.getInstance().returnPlayer(Player);

        if(players.isEmpty())
            GameController.getInstance().endMatch(this);

        // If match hasn't already started notify other users
        if(!isStarted())
            players.forEach((id, p) -> p.SendMessage(MessageType.Lobby, new Lobby<>(null, Player)));
    }

    /**
     * Contains active players' id only
     */
    private final transient ArrayList<Integer> playersOrder = new ArrayList<>();

    /**
     * Game map
     */
    private final transient Map<Territory> map;

    /**
     * Current turn
     */
    private volatile transient Turn currentTurn;

    public boolean isStarted() { return currentTurn != null; }

    /**
     * Global matches counter
     */
    public static final AtomicInteger counter = new AtomicInteger(0);

    /**
     * Initialize a new match using requested map
     *
     * @param Id Match id from Match.counter
     * @param MapName Name of the map to use
     * @throws ClassNotFoundException Cannot find requested map
     */
    public Match(int Id, String Name, Maps MapName) throws ClassNotFoundException {
        super("Match-" + Id);

        // Set current match id
        this.Id = Id;
        this.Name = Name;
        this.GameMap = MapName;

        try {
            map = new Map<>(MapName, Territory.class);
            map.loadDecks();
        } catch (NoSuchFieldException e){
            throw new ClassNotFoundException("Match " + this.Id + ": Can not find specified map");
        }

        // Setup and start match message receiver
        messageHandlers.put(MessageType.Turn, (message) -> {
            if(!isStarted())
                initMatch();
            else if(message.Json.equals("\"Update\"")) // Sent from player who entered the match after start
                players.get(message.PlayerId).SendMessage(MessageType.MapUpdate, new MapUpdate<>(map.getTerritories()));
            else {
                // If a player notified end of his turn, go ahead with next player
                currentTurn.endTurn();
                currentTurn = new Turn(this, nextPlaying(this.currentTurn.getPlaying()), false);
            }
        });

        messageHandlers.put(MessageType.Chat, this::routeAll);

        messageHandlers.put(MessageType.GameState, (message) -> {
            final GameState<Player> gameState = (new Gson()).fromJson(message.Json, message.Type.getType());

            switch (gameState.state){
                case Abandoned: // Message received from user
                    // Winner null in Abandoned game state means player has closed the application,
                    // so remove player completely
                    removePlayer(players.get(message.PlayerId), gameState.winner == null);
                    break;
                case Winner:    // Message received from turn instance
                    routeAll(message);
                    GameController.getInstance().endMatch(this);
                    break;
            }
        });

        // Any other message is routed to current turn to handle game progress
        defaultHandler = message -> currentTurn.setIncoming(message);

        startExecutor();
    }

    /**
     * Initialize users in this match and starts the game
     *
     * @throws UnsupportedOperationException If match has already been initialized or if players number is incorrect
     */
    private void initMatch() throws UnsupportedOperationException {
        if (currentTurn != null)
            throw new UnsupportedOperationException("Match " + Id + ": Match has already been initialized.");

        if(players.size() < 2 || players.size() > 6)
            throw new UnsupportedOperationException(String.format("Not possible to start playing with %d users.", players.size()));

        final Player AI = players.size() == 2 ? Player.getAI() : null;
        if(AI != null)
            players.put(AI.getId(), AI);

        // Setup Players
        final Deck<Color> deckColor = new Deck<>(Color.values());
        players.forEach((id, player) -> {
            player.initMatch(deckColor.next(), this.Id, map.nextMission());
            playersOrder.add(player.getId());
        });

        if(AI != null)
            playersOrder.removeIf(i -> i == AI.getId());

        // Send all players initial setup containing all players and Mission
        players.forEach((id, player) -> {
            // If mission is to destroy an army not present assign 24 territories mission
            if (player.getMission().Type == Game.Map.Mission.MissionType.Destroy) {
                boolean change = true;
                for (Player p: players.values()) {
                    if(p.getColor() == player.getMission().Army && !p.equals(player))
                        change = false;
                }

                if(change)
                    player.getMission().changeToNumber();
            }

            player.SendMessage(MessageType.Match, new Game.Connection.Match<>(this.Id, this.Name, map.Id, players.values()));
        });

        // Start first setup turn
        System.out.println("Match " + this.Id + ": Started game with " + players.size() + " players.");
        this.currentTurn = new Turn(this, AI, true);
    }

    /**
     * Stop current match thread and returns players
     *
     * @return Players in this match
     */
    ObservableMap<Integer, Player> terminate() {
        stopExecutor();

        if(isStarted()) {
            currentTurn.endTurn();
            currentTurn = null;
        }

        return players;
    }

    private void sendAll(MessageType Type, Object Message) {
        players.forEach((id, p) -> p.SendMessage(Type, Message));
    }

    private void routeAll(Message Message) {
        players.forEach((id, p) -> p.RouteMessage(Message.Type + "#" + Message.Json));
    }

    /**
     * Get next player in turn orders
     *
     * @param lastPlaying Last player who played
     * @return Player who has to play now
     */
    private Player nextPlaying(Player lastPlaying) {
        if(lastPlaying == null)
            return players.get(playersOrder.get(0));

        int current = playersOrder.indexOf(lastPlaying.getId());

        // If current was last of the row, return first again
        if(current == (playersOrder.size() - 1))
            return players.get(playersOrder.get(0));

        // Else return next in this row
        return players.get(playersOrder.get(current + 1));
    }

    /**
     * Implementation of a complete turn of game for a player
     */
    private class Turn implements Runnable {
        /**
         * Currently active player
         */
        private final Player playing;

        Player getPlaying() { return this.playing; }

        /**
         * Match where this turn is taking place
         */
        private final Match match;

        /**
         * Queue for incoming messages
         */
        private final ArrayList<Message> incoming = new ArrayList<>();

        /**
         * Add new message to turn queue
         *
         * @param Message Message to add
         */
        void setIncoming(Message Message) {
            synchronized (incoming){
                incoming.add(Message);
                incoming.notify();
            }
        }

        /**
         * Deserializer for received messages
         */
        private final Gson gson;

        private final Thread _instance;

        /**
         * Instance and start new turn
         *
         * @param Match Match where this turn is taking place
         * @param Current Player who will play this turn
         * @param isSetup If set to true starts from territories choice
         */
        public Turn(Match Match, Player Current, boolean isSetup) {
            this.match = Match;
            this.playing = Current;

            this.gson = getGsonBuilder(null).create();

            if(isSetup)
                this._instance = new Thread(() -> Setup(Current));
            else
                this._instance = new Thread(this);


            _instance.setName("Match" + match.Id + "-Turn");
            _instance.start();
        }

        /**
         * Join turn thread
         */
        void endTurn() {
            try {
                //_instance.interrupt();
                synchronized (incoming){
                    incoming.notify();
                }
                _instance.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        /**
         * Waits for requested message from specified user and returns it deserialized
         *
         * @param Type Type of message expected
         * @param PlayerId Player from whom the message is expected. If set to -1 get message from any player
         * @return Deserialized message
         */
        private <T> T waitMessage(MessageType Type, int PlayerId) {
            Message received;

            // While correct message isn't received discard other messages
            while (true) {
                if(incoming.isEmpty())
                    try {
                        synchronized (incoming) {
                            incoming.wait();
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                if(incoming.isEmpty())
                    return null;

                received = incoming.remove(0);

                // If correct message is detected exit loop
                if(received.Type == Type)
                    if(PlayerId == -1)
                        break;
                    else if(received.PlayerId == PlayerId)
                        break;
            }

            // Deserialize received message object and return
            return gson.fromJson(received.Json, received.Type.getType());
        }

        /**
         * Waits for requested message from current user and returns it deserialized
         *
         * @param Type Type of message expected
         * @return Deserialized message
         */
        private <T> T waitMessage(MessageType Type) {
            return waitMessage(Type, playing.getId());
        }


        /**
         * Takes care of initial armies distribution and territories choosing turns
         */
        private void Setup(Player AI) {
            // Setup
            Player last = null;

            // Save last player id to trigger ai choice during initial phase
            int lastId = match.playersOrder.get(match.playersOrder.size() - 1);
            final ArrayList<String> toGo = match.map.getTerritoryIds();

            /* Territories choice */

            while (toGo.size() > 0){
                // Send next player positioning message with one army
                (last = match.nextPlaying(last)).SendMessage(MessageType.Positioning, new Positioning(1));

                // Get chosen territory
                MapUpdate<Territory> update = waitMessage(MessageType.MapUpdate, last.getId());
                if(update == null) return;

                // Update game map
                Territory toUpdate = update.Updated.get(0);
                toUpdate.addArmies(1);
                toUpdate.NewArmies = 0;
                toUpdate.setOwner(last);

                // Remove it from remaining territories
                toGo.remove(toUpdate.Id);

                // Send update to all players
                match.sendAll(MessageType.MapUpdate, new MapUpdate<>(toUpdate));

                // At the end of the row chose one for AI if only two players are in match
                if(AI != null && last.getId() == lastId){
                    // Get random territory from remaining
                    toUpdate = match.map.getTerritory(toGo.get((new Random()).nextInt(toGo.size())));
                    toUpdate.addArmies(3);
                    toUpdate.setOwner(AI);
                    toGo.remove(toUpdate.Id);
                    match.sendAll(MessageType.MapUpdate, new MapUpdate<>(toUpdate));
                }
            }

            /* Armies displacement */

            // Generate initial armies for each player
            final int startingArmies = 50 - (5 * playersOrder.size());

            // Send each player initial armies
            playersOrder.forEach(pId -> {
                Player p = match.players.get(pId);

                // Calculate remaining armies to send                 (               total     -   already placed Armies  )
                p.SendMessage(MessageType.Positioning, new Positioning(startingArmies - p.getTerritories().size()));
                // Send mission to player
                p.SendMessage(MessageType.Mission, new Game.Connection.Mission(p.getMission()));
            });

            // Wait for all players to return initial displacement
            for(int i = playersOrder.size(); i > 0; i--){
                MapUpdate<Territory> u = waitMessage(MessageType.MapUpdate, -1);
                if(u == null) return;

                u.Updated.forEach(t -> {
                    t.addArmies(t.NewArmies);
                    t.NewArmies = 0;
                });

                // Send displacement to all players
                sendAll(MessageType.MapUpdate, u);
            }

            // Notify end of setup when completed
            match.setIncoming(lastId, MessageType.Turn, "");
        }

        /**
         * Define battle result based on dice throwing and send map update
         *
         * @param battle Battle message
         */
        private boolean Battle(Battle<Territory> battle) {
            /* Battle phase */
            // Setup die
            final Random die = new Random(System.nanoTime());
            final ArrayList<Integer> atkDice = new ArrayList<>(), defDice = new ArrayList<>();

            // Throw die for every attacking army
            for(int i = 0; i < battle.atkArmies; i++) {
                atkDice.add(die.nextInt(6) + 1);
            }

            // Throw die for every defending army
            for(int i = 0; i < battle.defArmies; i++) {
                defDice.add(die.nextInt(6) + 1);
            }

            // Sort dice descending
            Collections.sort(atkDice);
            Collections.reverse(atkDice);
            Collections.sort(defDice);
            Collections.reverse(defDice);

            int lostAtk = 0, lostDef = 0;

            final int maxDice = battle.atkArmies > battle.defArmies ? battle.defArmies : battle.atkArmies;

            // Compare couples of dice to assign victory
            for (int i = 0; i < maxDice; i++) {
                if(defDice.get(i) >= atkDice.get(i))
                    lostAtk++;
                else
                    lostDef++;
            }

            // Remove defeated armies from attacker
            if(lostAtk > 0)
                battle.from.canRemoveArmies(lostAtk);

            // Remove defeated armies from defender, else if cannot remove all armies from
            // defender territory change ownership and move armies from attacker territory
            if(!battle.to.canRemoveArmies(lostDef)) {
                // If player has no more territories, notify defeat to the player and do housekeeping
                if(battle.to.getOwner().getTerritories().size() == 1) {
                    // If AI has been defeated simply remove it
                    if(battle.to.getOwner().getId() == -1)
                        match.players.remove(-1);
                    else { // Else do housekeeping and set player as witness for the match
                        final Player defeated = battle.to.getOwner();
                        defeated.SendMessage(MessageType.GameState, new GameState<>(StateType.Defeated, battle.to.getOwner()));
                        playersOrder.removeIf(id -> id == defeated.getId());

                        // Return cards of defeated player
                        final Cards c = waitMessage(MessageType.Cards, defeated.getId());
                        if (c == null) return false;
                        match.map.returnCards(c.combination);

                        // Pass user to witness mode
                        defeated.exitMatch();
                        defeated.enterMatch(match.Id);

                        // Check for destroy mission on other players to fix mission type if necessary
                        match.playersOrder.forEach(id -> {
                            final Player p = players.get(id);
                            if (!playing.equals(p))
                                if (p.getMission().Type == Mission.MissionType.Destroy && p.getMission().Army == defeated.getColor())
                                    p.getMission().changeToNumber();
                        });
                    }
                }

                // Move armies to attacker to conquered territory and update owner
                battle.from.canRemoveArmies(atkDice.size());
                battle.to.setOwner(battle.from.getOwner());
                battle.to.addArmies(atkDice.size() - defDice.size());
            }
            /* Battle phase end */

            /* Update */
            // Send update to all players
            final boolean hasMove = battle.to.getOwner().equals(battle.from.getOwner()) && battle.from.getArmies() > 1;
            final MapUpdate<Territory> result = new MapUpdate<>(atkDice, defDice, hasMove, Sounds.battleSoundSelector(lostAtk, lostDef), battle.from, battle.to);
            match.sendAll(MessageType.MapUpdate, result);

            // If attacker hasn't conquered the territory or no armies can be moved to it complete battle
            if(!hasMove) return true;

            // Else if there are some armies which can be moved to new territory
            // wait for response from attacker
            final MapUpdate<Territory> update = waitMessage(MessageType.MapUpdate, playing.getId());

            if(update == null) return false;

            // If no other move is performed complete battle
            // else if armies have been moved update game map
            if(!update.Updated.isEmpty())
                if(battle.from.canRemoveArmies(battle.to.NewArmies))
                    battle.to.addArmies(battle.to.NewArmies);

            // Send new placement to all players
            match.sendAll(MessageType.MapUpdate, update);

            return true;
        }

        @Override
        public void run() {
            /* Positioning phase */
            // Ask for a card combination to get more armies
            playing.SendMessage(MessageType.Cards, new Cards());

            // Calculate standard armies reinforcement
            int newArmies = (playing.getTerritories().size() / 3) >= 3 ? (playing.getTerritories().size() / 3) : 3;

            // Extra armies due to continent ownership
            newArmies += match.map.getAreasBonus(playing);

            // Wait for cards message to return from user
            final Cards redeemed = waitMessage(MessageType.Cards);
            if(redeemed == null) return;

            // If there is a combination check for validity
            if(redeemed.combination.size() != 0)
                newArmies += match.map.playCards(redeemed.combination);

            // Send new armies to player
            playing.SendMessage(MessageType.Positioning, new Positioning(newArmies));

            // Wait to get new armies displacement over player's territories
            final MapUpdate<Territory> newPlacement = waitMessage(MessageType.MapUpdate);
            if(newPlacement == null) return;

            // Update armies number in game map
            newPlacement.Updated.forEach(t -> {
                t.addArmies(t.NewArmies);
                t.NewArmies = 0;
            });

            // Send update To all Players
            match.sendAll(MessageType.MapUpdate, newPlacement);
            /* Positioning phase end */

            /* Attacking phase */
            // Send attack phase begin message
            playing.SendMessage(MessageType.Battle, new Battle<Territory>(null, null, 0));

            final int beforeAtkTerritories = playing.getTerritories().size();

            // Wait for all attack messages from player
            while (true) {
                // Get attack message from player
                Battle<Territory> newBattle = waitMessage(MessageType.Battle);
                if(newBattle == null) return;

                // If armies are zero end attack phase
                if(newBattle.atkArmies == 0) break;

                // If more than one army is present on defender territory ask player how many he want to use
                if(newBattle.to.getArmies() > 1) {
                    // Get defender player id
                    int defenderId = newBattle.to.getOwner().getId();

                    // If attacking AI territory use maximum number of defending armies
                    if(defenderId == -1)
                        newBattle.defArmies = 2;
                    else { // Else ask player
                        // Send defender the attack message
                        newBattle.to.getOwner().SendMessage(MessageType.Battle, newBattle);

                        // Wait for response
                        newBattle = waitMessage(MessageType.Battle, defenderId);
                        if(newBattle == null) return;
                    }
                }

                // Perform battle
                if(!Battle(newBattle)) return;

                // After each battle check if mission completed
                if(match.map.checkMission(playing, playing.getMission())){
                    // If player wins notify match object and return
                    match.setIncoming(playing.getId(),
                                      MessageType.GameState,
                                      gson.toJson(new GameState<>(StateType.Winner, playing), MessageType.GameState.getType()));
                    return;
                }
            }

            // If player has conquered at least one new territory send him a card
            if(beforeAtkTerritories < playing.getTerritories().size())
                playing.SendMessage(MessageType.Cards, new Cards(match.map.nextCard()));

            // Send empty update to request final move
            playing.SendMessage(MessageType.MapUpdate, new MapUpdate<>());
            final MapUpdate<Territory> endMove = waitMessage(MessageType.MapUpdate);
            if(endMove == null) return;

            // If final move is performed update map and players
            if(!endMove.Updated.isEmpty()){
                final Territory from = endMove.Updated.get(0), to = endMove.Updated.get(1);
                from.canRemoveArmies(to.NewArmies);
                to.addArmies(to.NewArmies);
                to.NewArmies = 0;

                match.sendAll(MessageType.MapUpdate, endMove);
            }

            // Notify end of turn when completed
            match.setIncoming(playing.getId(), MessageType.Turn, "");
        }
    }

    private GsonBuilder getGsonBuilder(GsonBuilder GsonBuilder) {
        GsonBuilder builder = GsonBuilder;
        if(builder == null)
            builder = new GsonBuilder();

        builder.registerTypeAdapter(Player.class, new PlayerDeserializer(this));
        builder.registerTypeAdapter(Territory.class, new TerritoryDeserializer(this.map));

        return builder;
    }

    private class PlayerDeserializer implements JsonDeserializer<Player> {

        private final Match match;

        public PlayerDeserializer(Match Match) {
            this.match = Match;
        }

        @Override
        public Player deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            return match.players.get(json.getAsJsonObject().get("id").getAsInt());
        }
    }

    private class TerritoryDeserializer implements JsonDeserializer<Territory> {

        private final Map<Territory> map;

        public TerritoryDeserializer(Map<Territory> Map) {
            this.map = Map;
        }

        @Override
        public Territory deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {

            final JsonObject jt = json.getAsJsonObject();
            final Territory t = map.getTerritory(jt.get("Id").getAsString());
            if(jt.has("NewArmies"))
                t.NewArmies = jt.get("NewArmies").getAsInt();

            return t;
        }
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof Match && ((Match)other).Id == this.Id;
    }
}
