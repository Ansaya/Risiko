package Server.Game;

import Game.Color;
import Game.Connection.*;
import Game.Map.*;
import Game.Map.Mission;
import Server.Game.Map.DeckTerritory;
import Server.Game.Map.Map;
import Game.Connection.Battle;
import Game.Connection.GameState;
import Game.MessageReceiver;
import Game.StateType;
import Server.Game.Connection.MessageType;
import Server.Game.Map.Territory;
import com.google.gson.Gson;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Match object To manage game turns in a dedicated thread
 */
public class Match extends MessageReceiver<MessageType> {

    /**
     * Match id
     */
    public final int id;

    /**
     * Players' list for this match (contains witnesses too)
     */
    private final HashMap<Integer, Player> players = new HashMap<>();

    public HashMap<Integer, Player> getPlayers() { return players; }

    /**
     * Contains playing Players' id only
     */
    private final ArrayList<Integer> playersOrder = new ArrayList<>();

    /**
     * Game map
     */
    private final Map gameMap = new Map();

    /**
     * Deck containing all territories cards plus two jolly
     */
    private final DeckTerritory cards = new DeckTerritory();

    /**
     * Current turn
     */
    private volatile Turn currentTurn;

    /**
     * Global matches counter (0 not allowed)
     */
    public static final AtomicInteger counter = new AtomicInteger(0);

    /**
     * Instance a new match and starts the game
     *
     * @param Players Players who will play in this match
     */
    public Match(int Id, ArrayList<Player> Players) {
        if(Players.size() < 2 || Players.size() > 6)
            throw new UnsupportedOperationException(String.format("Not possible To start playing with %d users.", Players.size()));

        // Set current match id
        this.id = Id;

        // Setup Players
        final Color[] colors = Color.values();
        final ArrayList<Mission> missions = new ArrayList<>(Arrays.asList(Mission.values()));
        // Remove destroy missions relative to unused armies
        missions.removeIf(m -> {
            if(m.Type == Mission.MissionType.Destroy){
                return m.Army.ordinal() > Players.size();
            }
            return false;
        });
        final Random rnd = new Random();
        final AtomicInteger i = new AtomicInteger(0);
        Players.forEach(p -> {
            // Initialize player with color, match id and Mission
            Mission mission = missions.remove(rnd.nextInt(missions.size() - 1));
            // If mission is to destroy an army not present return 24 territories as specified in original game mission
            if(mission.Type == Mission.MissionType.Destroy)
                if(mission.Army.ordinal() > Players.size())
                    mission = Mission.Territory24;

            p.initMatch(colors[i.getAndIncrement()], this.id, mission);
            players.put(p.id, p);
            playersOrder.add(p.id);
        });

        // Send all players initial setup containing all players and Mission
        players.forEach((id, player) -> player.SendMessage(MessageType.Match, new Game.Connection.Match<>(Players)));

        // Setup and start match message receiver
        listenersInit();
        startListen("Match " + this.id);

        // Start first setup turn
        System.out.println("Match " + this.id + ": Started game with " + players.size() + " Players.");
        this.currentTurn = new Turn(this, null, true);
    }

    /**
     * Stop current match thread and returns Players To GameController
     */
    void terminate() {
        currentTurn.endTurn();
        stopListen();

        players.forEach((id, p) -> {
            p.exitMatch();
            GameController.getInstance().returnPlayer(p);
        });
        players.clear();
    }

    /**
     * Initialize handlers for new messages and start message receiver
     */
    private void listenersInit() {
        messageHandlers.put(MessageType.Turn, (message) -> {
            // If a player notified end of his turn, go ahead with next player
            currentTurn.endTurn();
            currentTurn = new Turn(this, nextPlaying(this.currentTurn.getPlaying()), false);
        });

        messageHandlers.put(MessageType.Chat, this::routeAll);

        messageHandlers.put(MessageType.GameState, (message) -> {
            final GameState<Player> gameState = (new Gson()).fromJson(message.Json, message.Type.getType());

            switch (gameState.state){
                case Abandoned: // Message received From user
                    final Player p = players.get(message.PlayerId);

                    // Winner null in Abandoned game state means player has closed the application, so no need To return To lobby
                    if(gameState.winner == null)
                        players.remove(message.PlayerId);

                    // If player was playing in this match, abort match
                    if(p.isPlaying()) {
                        sendAll(MessageType.GameState, new GameState<>(StateType.Abandoned, p));
                        message.PlayerId = this.id;
                        GameController.getInstance().setIncoming(message);
                    }
                    else { // Else return player To lobby
                        if(players.remove(p.id, p))
                            GameController.getInstance().returnPlayer(p);
                    }

                    break;
                case Winner:    // Message received From turn instance
                    routeAll(message);
                    message.PlayerId = this.id;
                    GameController.getInstance().setIncoming(message);
                    break;
                case Defeated:  // Message received From turn instance
                    final Player defeated = players.get(gameState.winner.id);

                    // Report defeat To user
                    defeated.RouteMessage(MessageType.GameState.name() + "#" + message.Json);

                    // Pass user To witness mode
                    defeated.exitMatch();
                    defeated.witnessMatch(this.id);
                    break;
            }
        });

        // Any other message is routed to current turn to handle game progress
        defaultHandler = (message) -> currentTurn.setIncoming(message);
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
     * @return Player who has To play now
     */
    private Player nextPlaying(Player lastPlaying) {
        if(lastPlaying == null)
            return players.get(playersOrder.get(0));

        int current = playersOrder.indexOf(lastPlaying.id);

        // If current was last of the row, return first again
        if(current == (playersOrder.size() - 1))
            return players.get(playersOrder.get(0));

        // Else return next in this row
        return players.get(playersOrder.get(current + 1));
    }

    /**
     * Check if specified player has completed his Mission
     *
     * @param Player Player To check Mission for
     * @return True if Mission accomplished, false if not
     */
    private boolean checkMission(Player Player) {
        final Mission mission = Player.getMission();
        if(mission == null)
            return false;

        switch (mission.Type){
            case Conquer:
                final ArrayList<Territories> ToConquer = mission.getToConquer();
                Player.getTerritories().forEach(t -> ToConquer.removeIf(te -> te == t.Territory));
                return ToConquer.isEmpty();
            case Destroy:
                for (Player p:players.values()) {
                    if(p.getColor() == mission.Army)
                        return false;
                }
                return true;
            case Number:
                if(Player.getTerritories().size() >= mission.Number) {
                    if (mission == Mission.Territory18Two) {
                        for (Territory t: Player.getTerritories()) {
                            if(t.getArmies() < 2)
                                return false;
                        }
                    }
                    return true;
                }
                return false;
            case Special:
                final ArrayList<Territories> toConquer = mission.getToConquer();
                Player.getTerritories().forEach(t -> toConquer.removeIf(te -> te == t.Territory));
                return toConquer.isEmpty() && Continent.dominatedContinents(Player.getTerritories()).size() >= mission.Number;
        }

        return false;
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
         * Add new message To turn queue
         *
         * @param Message Message To add
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
        private final Gson gson = new Gson();

        private final Thread _instance;

        /**
         * Instance and start new turn
         *
         * @param Match Match where this turn is taking place
         * @param Current Player who will play this turn
         * @param isSetup If set To true starts From territories choice
         */
        public Turn(Match Match, Player Current, boolean isSetup) {
            this.match = Match;
            this.playing = Current;

            if(isSetup)
                this._instance = new Thread(() -> Setup(match.players.size() < 3));
            else
                this._instance = new Thread(this);


            _instance.setName("Match" + match.id + "-Turn");
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
            } catch (Exception e) {}
        }

        /**
         * Waits for requested message From specified user and returns it deserialized
         *
         * @param Type Type of message expected
         * @param PlayerId Player who the message is expected From. If set To -1 get message From any player
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
                    } catch (Exception e) {}

                if(incoming.isEmpty())
                    return null;

                received = incoming.get(0);
                incoming.remove(0);

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
         * Waits for requested message From current user and returns it deserialized
         *
         * @param Type Type of message expected
         * @return Deserialized message
         */
        private <T> T waitMessage(MessageType Type) {
            return waitMessage(Type, playing.id);
        }


        /**
         * Takes care of initial Armies distribution and territories choosing turns
         */
        private void Setup(boolean twoOnly) {
            // Setup
            Player last = null;

            // Create AI player without socket
            final Player ai = Player.getAI(this.match.id, Color.BLUE);

            // Save last player id To trigger ai choice during initial phase
            int lastId = match.playersOrder.get(match.playersOrder.size() - 1);
            final ArrayList<Territories> toGo = new ArrayList<>(Arrays.asList(Territories.values()));
            toGo.remove(Territories.Jolly1);
            toGo.remove(Territories.Jolly2);

            /* Territories choice */

            while (toGo.size() > 0){
                // Send next player positioning message with one army
                (last = match.nextPlaying(last)).SendMessage(MessageType.Positioning, new Positioning(1));

                // Get chosen Territory
                MapUpdate<Territory> update = waitMessage(MessageType.MapUpdate, last.id);
                if(update == null)
                    return;

                // Update game map
                Territory toUpdate = match.gameMap.territories.get(update.updated.get(0).Territory);
                toUpdate.addArmies(1);
                toUpdate.setOwner(last);

                // Remove it From remaining territories
                toGo.remove(toUpdate.Territory);

                // Send update To all Players
                match.sendAll(MessageType.MapUpdate, new MapUpdate<>(toUpdate));

                // At the end of the row chose one for AI if only two players are in match
                if(twoOnly && last.id == lastId){
                    // Get random territory from remaining
                    toUpdate = match.gameMap.territories.get(toGo.get((new Random()).nextInt(toGo.size())));
                    toUpdate.addArmies(3);
                    toUpdate.setOwner(ai);
                    toGo.remove(toUpdate.Territory);
                    match.sendAll(MessageType.MapUpdate, new MapUpdate<>(toUpdate));
                }
            }

            /* Armies displacement */

            // Generate initial Armies for each player
            final int startingArmies = 50 - (5 * playersOrder.size());

            // Send each player initial armies
            playersOrder.forEach(pId -> {
                Player p = match.players.get(pId);

                // Calculate remaining Armies to send                 (    total      -   already placed Armies   )
                p.SendMessage(MessageType.Positioning, new Positioning(startingArmies - p.getTerritories().size()));
                // Send mission to player
                p.SendMessage(MessageType.Mission, new Game.Connection.Mission(p.getMission()));
            });

            // Wait for all Players To return initial displacement
            ArrayList<Territory> finalUpdate = new ArrayList<>();
            for(int i = playersOrder.size(); i > 0; i--){
                MapUpdate<Territory> u = waitMessage(MessageType.MapUpdate, -1);
                if(u == null)
                    return;

                u.updated.forEach(t -> {
                    Territory tr = match.gameMap.territories.get(t.Territory);
                    tr.addArmies(t.NewArmies);
                    finalUpdate.add(tr);
                });
            }

            // Send global initial displacement To all Players
            sendAll(MessageType.MapUpdate, new MapUpdate<>(finalUpdate));

            // Notify end of setup when completed
            match.setIncoming(lastId, MessageType.Turn, "");
        }

        /**
         * Define battle result based on dice throwing and send map update
         *
         * @param Battle Battle message
         */
        private boolean Battle(Battle<Territory> Battle) {
            /* Battle phase */
            // Setup die
            final Random die = new Random(System.nanoTime());
            final ArrayList<Integer> atkDice = new ArrayList<>(), defDice = new ArrayList<>();

            // Throw die for every attacking army
            for(int i = 0; i < Battle.atkArmies; i++) {
                atkDice.add(die.nextInt(6) + 1);
            }

            // Throw die for every defending army
            for(int i = 0; i < Battle.defArmies; i++) {
                defDice.add(die.nextInt(6) + 1);
            }

            // Sort dice descending
            Collections.sort(atkDice);
            Collections.reverse(atkDice);
            Collections.sort(defDice);
            Collections.reverse(defDice);

            int lostAtk = 0, lostDef = 0;

            final int maxDice = Battle.atkArmies > Battle.defArmies ? Battle.defArmies : Battle.atkArmies;

            // Compare couples of dice To assign victory
            for (int i = 0; i < maxDice; i++) {
                if(defDice.get(i) >= atkDice.get(i))
                    lostAtk++;
                else
                    lostDef++;
            }

            // Get territories To update From game map
            final Territory attacker = match.gameMap.territories.get(Battle.from.Territory);
            final Territory defender = match.gameMap.territories.get(Battle.to.Territory);

            // Remove defeated Armies From attacker
            if(lostAtk > 0)
                attacker.canRemoveArmies(lostAtk);

            // Remove defeated Armies From defender, else if cannot remove all Armies From
            // defender Territory change ownership and add move armies From attacker territory
            if(!defender.canRemoveArmies(lostDef)) {
                // If player has no more territories, notify defeat to match object
                if(defender.getOwner().getTerritories().size() == 1 && defender.getOwner().id != -1)
                    match.setIncoming(defender.getOwner().id,
                                      MessageType.GameState,
                                      gson.toJson(new GameState<>(StateType.Defeated, defender.getOwner()), MessageType.GameState.getType()));

                // Move armies to attacker to conquered territory and update owner
                attacker.canRemoveArmies(atkDice.size());
                defender.setOwner(attacker.getOwner());
                defender.addArmies(atkDice.size() - defDice.size());
            }
            /* Battle phase end */

            /* Update */
            final MapUpdate<Territory> result = new MapUpdate<>(attacker, defender);
            // If attacker hasn't conquered the territory or no armies can be moved to it complete battle
            if(!defender.getOwner().equals(attacker.getOwner()) || attacker.getArmies() == 1) {
                // Send update to all players
                match.sendAll(MessageType.MapUpdate, result);
                return true;
            }

            // Else if there are some armies which can be moved to new territory
            // send special move to current player and wait for response
            playing.SendMessage(MessageType.SpecialMoving, new SpecialMoving<>(attacker, defender));
            final SpecialMoving<Territory> update = waitMessage(MessageType.SpecialMoving, playing.id);

            if(update == null)
                return false;

            // If no other move is performed complete battle
            // else if armies have been moved update game map
            if(update.From != null)
                if(attacker.canRemoveArmies(update.To.NewArmies))
                    defender.addArmies(update.To.NewArmies);

            // Send new placement to all players
            match.sendAll(MessageType.MapUpdate, result);

            return true;
        }

        @Override
        public void run() {
            /* Positioning phase */
            // Ask for a card combination To get more Armies
            playing.SendMessage(MessageType.Cards, new Cards());

            // Calculate standard Armies reinforcement
            int newArmies = (playing.getTerritories().size() / 3) >= 3 ? (playing.getTerritories().size() / 3) : 3;

            // Extra Armies due To continent ownership
            newArmies += Continent.bonusArmies(playing.getTerritories());

            // Wait for Cards message To return From user
            final Cards redeemed = waitMessage(MessageType.Cards);
            if(redeemed == null)
                return;

            // If there is a combination check for validity
            if(redeemed.combination.size() != 0)
                newArmies += match.cards.isCombinationValid(true, redeemed.combination);

            // Send new Armies To player
            playing.SendMessage(MessageType.Positioning, new Positioning(newArmies));

            // Wait To get new Armies displacement over player's territories
            final MapUpdate<Territory> newPlacement = waitMessage(MessageType.MapUpdate);
            if(newPlacement == null)
                return;

            // Update Armies number in game map
            newPlacement.updated.forEach((t) -> {
                match.gameMap.territories.get(t.Territory).addArmies(t.NewArmies);
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
                // Get attack message From player
                Battle<Territory> newBattle = waitMessage(MessageType.Battle);
                if(newBattle == null)
                    return;

                // If armies are zero end attack phase
                if(newBattle.atkArmies == 0)
                    break;

                // If more than one army is present on defender territory ask player how many he want to use
                if(newBattle.to.getArmies() > 1) {
                    // Get defender player id
                    int defenderId = newBattle.to.getOwner().id;

                    // If attacking AI territory use maximum number of defending armies
                    if(defenderId == -1)
                        newBattle.defArmies = 2;
                    else { // Else ask player
                        // Send defender the attack message
                        match.players.get(defenderId).SendMessage(MessageType.Battle, newBattle);

                        // Wait for response
                        newBattle = waitMessage(MessageType.Battle, defenderId);
                        if(newBattle == null)
                            return;
                    }
                }

                // Perform battle
                if(!Battle(newBattle))
                    return;

                // After each battle check if Mission completed
                if(match.checkMission(playing)){
                    // If player wins notify match object and return
                    match.setIncoming(playing.id,
                                      MessageType.GameState,
                                      gson.toJson(new GameState<>(StateType.Winner, playing), MessageType.GameState.getType()));
                    return;
                }
            }

            // If player has conquered at least one new Territory send him a card
            if(beforeAtkTerritories < playing.getTerritories().size())
                playing.SendMessage(MessageType.Cards, new Cards(match.cards.next()));

            // Send empty positioning to request final move
            playing.SendMessage(MessageType.SpecialMoving, new SpecialMoving<>(null, null));
            final SpecialMoving<Territory> endMove = waitMessage(MessageType.SpecialMoving);
            if(endMove == null)
                return;

            // If final move is performed update map and players
            if(endMove.From != null){
                Territory to = null, from;
                if((from = match.gameMap.territories.get(endMove.From.Territory)).canRemoveArmies(endMove.To.NewArmies))
                    (to = match.gameMap.territories.get(endMove.To.Territory)).addArmies(endMove.To.NewArmies);

                match.sendAll(MessageType.MapUpdate, new MapUpdate<>(from, to));
            }

            // Notify end of turn when completed
            match.setIncoming(playing.id, MessageType.Turn, "");
        }
    }
}
