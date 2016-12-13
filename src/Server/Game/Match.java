package Server.Game;

import Game.Color;
import Game.Connection.*;
import Game.Map.*;
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
 * Match object to manage game turns in a dedicated thread
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
            throw new UnsupportedOperationException(String.format("Not possible to start playing with %d users.", Players.size()));

        // Set current match id
        this.id = Id;

        // Setup Players
        final Color[] colors = Color.values();
        final ArrayList<Mission> missions = new ArrayList<>(Arrays.asList(Mission.values()));
        final Random rnd = new Random();
        final AtomicInteger i = new AtomicInteger(0);
        Players.forEach(p -> {
            // Initialize player with color, match id and mission
            p.initMatch(colors[i.getAndIncrement()], this.id, missions.remove(rnd.nextInt(missions.size() - 1)));
            players.put(p.id, p);
            playersOrder.add(p.id);
        });

        // Send all players initial setup containing all players and mission
        players.forEach((id, player) -> player.SendMessage(MessageType.Match, new Game.Connection.Match<>(Players, player.getMission())));

        // Setup and start match message receiver
        listenersInit();
        startListen("Match " + this.id);

        // Start first setup turn
        System.out.println("Match " + this.id + ": Started game with " + players.size() + " Players.");
        this.currentTurn = new Turn(this, null, true);
    }

    /**
     * Stop current match thread and returns Players to GameController
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

        messageHandlers.put(MessageType.Chat, (message) -> routeAll(message));

        messageHandlers.put(MessageType.GameState, (message) -> {
            final GameState<Player> gameState = (new Gson()).fromJson(message.Json, message.Type.getType());

            switch (gameState.state){
                case Abandoned: // Message received from user
                    final Player p = players.get(message.PlayerId);

                    // Winner null in Abandoned game state means player has closed the application, so no need to return to lobby
                    if(gameState.winner == null)
                        players.remove(message.PlayerId);

                    // If player was playing in this match, abort match
                    if(p.isPlaying()) {
                        sendAll(MessageType.GameState, new GameState<>(StateType.Abandoned, p));
                        message.PlayerId = this.id;
                        GameController.getInstance().setIncoming(message);
                    }
                    else { // Else return player to lobby
                        if(players.remove(p.id, p))
                            GameController.getInstance().returnPlayer(p);
                    }

                    break;
                case Winner:    // Message received from turn instance
                    routeAll(message);
                    message.PlayerId = this.id;
                    GameController.getInstance().setIncoming(message);
                    break;
                case Defeated:  // Message received from turn instance
                    final Player defeated = players.get(gameState.winner.id);

                    // Report defeat to user
                    defeated.RouteMessage(MessageType.GameState.name() + "#" + message.Json);

                    // Pass user to witness mode
                    defeated.exitMatch();
                    defeated.witnessMatch(this.id);
                    break;
            }
        });

        defaultHandler = (message) -> {
            // Any other message is routed to current turn to handle game progress
            this.currentTurn.setIncoming(message);
        };
    }

    void sendAll(MessageType Type, Object Message) {
        players.forEach((id, p) -> p.SendMessage(Type, Message));
    }

    void routeAll(Message Message) {
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

        int current = playersOrder.indexOf(lastPlaying.id);

        // If current was last of the row, return first again
        if(current == (playersOrder.size() - 1))
            return players.get(playersOrder.get(0));

        // Else return next in this row
        return players.get(playersOrder.get(current + 1));
    }

    /**
     * Check if specified player has completed his mission
     *
     * @param Player Player to check mission for
     * @return True if mission accomplished, false if not
     */
    private boolean checkMission(Player Player) {
        final Mission mission = Player.getMission();
        if(mission == null)
            return false;

        switch (mission.Type){
            case Conquer:
                final ArrayList<Territories> ToConquer = mission.getToConquer();
                ToConquer.removeIf(t -> Player.getTerritories().contains(t));
                return ToConquer.isEmpty();
            case Destroy:
                return !players.containsValue(mission.Army);
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
                toConquer.removeIf(t -> Player.getTerritories().contains(t));
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
        private final Gson gson = new Gson();

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
                _instance.interrupt();
                _instance.join();
            } catch (Exception e) {}
        }

        /**
         * Waits for requested message from specified user and returns it deserialized
         *
         * @param Type Type of message expected
         * @param PlayerId Player who the message is expected from. If set to -1 get message from any player
         * @return Deserialized message
         */
        private <T> T waitMessage(MessageType Type, int PlayerId) {
            Message received;

            // While correct message isn't received discard other messages
            while (true) {
                if(incoming.size() == 0)
                    try {
                        synchronized (incoming) {
                            incoming.wait();
                        }
                    } catch (Exception e) {}

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
         * Waits for requested message from current user and returns it deserialized
         *
         * @param Type Type of message expected
         * @return Deserialized message
         */
        private <T> T waitMessage(MessageType Type) {
            return waitMessage(Type, playing.id);
        }

        /**
         * Define battle result based on dice throwing and send map update
         *
         * @param Battle Battle message
         */
        private void Battle(Battle<Territory> Battle) {
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

            // Compare couples of dice to assign victory
            for (int i = 0; i < Battle.defArmies; i++) {
                if(defDice.get(i) >= atkDice.get(i))
                    lostAtk++;
                else
                    lostDef++;
            }

            // Get territories to update from game map
            final Territory attacker = match.gameMap.territories.get(Battle.from.territory);
            final Territory defender = match.gameMap.territories.get(Battle.to.territory);

            // Remove defeated Armies from attacker
            if(lostAtk > 0)
                attacker.canRemoveArmies(lostAtk);

            // Remove defeated Armies from defender, else if cannot remove all Armies from
            // defender Territory change ownership and add necessary Armies
            if(!defender.canRemoveArmies(lostDef)) {
                // If player has no more territories, notify defeat to match object
                if(defender.owner.getTerritories().size() == 1)
                    match.setIncoming(defender.owner.id,
                                      MessageType.GameState,
                                      gson.toJson(new GameState<>(StateType.Defeated, defender.owner), MessageType.GameState.getType()));

                // Update conquered Territory information
                defender.owner = match.players.get(Battle.from.owner.id);
                defender.addArmies(atkDice.size() - defDice.size());
            }
            /* Battle phase end */

            /* Update */
            // If attacker hasn't conquered the Territory or no Armies can be moved to it complete battle
            if(lostAtk != 0 || attacker.getArmies() == 1) {
                // Create map update after battle result
                final MapUpdate<Territory> result = new MapUpdate<>(attacker);
                if(lostDef != 0)
                    result.updated.add(defender);

                // Send update to all Players
                match.sendAll(MessageType.MapUpdate, result);
                return;
            }

            // Else if there are some Armies which can be moved to new Territory
            // send special move to current player and wait for response
            playing.SendMessage(MessageType.SpecialMoving, new SpecialMoving<>(Battle));
            final SpecialMoving<Territory> update = waitMessage(MessageType.SpecialMoving, playing.id);

            // If no other move is performed complete battle
            if(update.from == null)
                return;

            // Else if Armies have been moved update game map
            attacker.canRemoveArmies(update.to.newArmies);
            defender.addArmies(update.to.newArmies);

            // Send new placement to all Players
            match.sendAll(MessageType.MapUpdate, new MapUpdate<>(attacker, defender));
        }

        /**
         * Takes care of initial Armies distribution and territories choosing turns
         */
        private void Setup(boolean twoOnly) {
            // Setup
            Player last = null;

            // Create AI player without socket
            final Player ai = Player.getAI(this.match.id, Color.BLUE);

            // Save last player id to trigger ai choice during initial phase
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

                // Update game map
                Territory toUpdate = match.gameMap.territories.get(update.updated.get(0).territory);
                toUpdate.addArmies(1);
                toUpdate.owner = match.players.get(last.id);

                // Remove it from remaining territories
                toGo.remove(toUpdate.territory);

                // Send update to all Players
                match.sendAll(MessageType.MapUpdate, new MapUpdate<>(toUpdate));

                // At the end of the row chose one for ai if only two Players are in match
                if(twoOnly && last.id == lastId){
                    // Get random Territory from remaining territories
                    toUpdate = match.gameMap.territories.get(toGo.get((new Random()).nextInt(toGo.size())));
                    toUpdate.addArmies(3);
                    toUpdate.owner = ai;
                    toGo.remove(toUpdate.territory);
                    match.sendAll(MessageType.MapUpdate, new MapUpdate<>(toUpdate));
                }
            }

            /* Armies displacement */

            // Generate initial Armies for each player
            final int startingArmies = 50 - (5 * playersOrder.size());

            // Send player initial Armies
            playersOrder.forEach(pId -> {
                Player p = match.players.get(pId);

                // Calculate remaining Armies to send                 (    total      -   already placed Armies   )
                p.SendMessage(MessageType.Positioning, new Positioning(startingArmies - p.getTerritories().size()));
            });

            // Wait for all Players to return initial displacement
            ArrayList<Territory> finalUpdate = new ArrayList<>();
            for(int i = playersOrder.size(); i > 0; i--){
                MapUpdate<Territory> u = waitMessage(MessageType.MapUpdate, -1);
                u.updated.forEach(t -> {
                    Territory tr = match.gameMap.territories.get(t.territory);
                    tr.addArmies(t.newArmies);
                    finalUpdate.add(tr);
                });
            }

            // Send global initial displacement to all Players
            sendAll(MessageType.MapUpdate, new MapUpdate<>(finalUpdate));

            // Notify end of setup when completed
            match.setIncoming(lastId, MessageType.Turn, "");
        }

        @Override
        public void run() {
            /* Positioning phase */
            // Ask for a card combination to get more Armies
            playing.SendMessage(MessageType.Cards, new Cards());

            // Calculate standard Armies reinforcement
            int newArmies = (playing.getTerritories().size() / 3) >= 3 ? (playing.getTerritories().size() / 3) : 3;

            // Extra Armies due to continent ownership
            newArmies += Continent.bonusArmies(playing.getTerritories());

            // Wait for Cards message to return from user
            final Cards redeemed = waitMessage(MessageType.Cards);

            // If there is a combination check for validity
            if(redeemed.combination.size() != 0)
                newArmies += match.cards.isCombinationValid(true, redeemed.combination);

            // Send new Armies to player
            playing.SendMessage(MessageType.Positioning, new Positioning(newArmies));

            // Wait to get new Armies displacement over player's territories
            final MapUpdate<Territory> newPlacement = waitMessage(MessageType.MapUpdate);

            // Update Armies number in game map
            newPlacement.updated.forEach((t) -> match.gameMap.territories.get(t.territory).addArmies(t.newArmies));

            // Send update to all Players
            match.sendAll(MessageType.MapUpdate, newPlacement);
            /* Positioning phase end */

            /* Attacking phase */
            // Ask player to attack
            playing.SendMessage(MessageType.Battle, new Battle<Player>(null, null, 0));

            final int beforeAtkTerritories = playing.getTerritories().size();

            // Wait for all attack messages from player
            while (true) {
                // Get attack message from player
                Battle<Territory> newBattle = waitMessage(MessageType.Battle);

                // If Armies are zero end attack phase
                if(newBattle.atkArmies == 0)
                    break;

                // If more than one army is present on defender Territory ask player how many he want to use
                if(newBattle.to.getArmies() > 1) {
                    // Get defender player id
                    int defenderId = newBattle.to.owner.id;

                    // Send defender the attack message
                    match.players.get(defenderId).SendMessage(MessageType.Battle, newBattle);

                    // Wait for response
                    newBattle = waitMessage(MessageType.Battle, defenderId);
                }

                // Perform battle
                Battle(newBattle);

                // After each battle check if mission completed
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

            // Implement end turn moving
            final MapUpdate<Territory> endMove = waitMessage(MessageType.MapUpdate);

            if(!endMove.updated.isEmpty()){
                match.sendAll(MessageType.MapUpdate, endMove);
            }

            // Notify end of turn when completed
            match.setIncoming(playing.id, MessageType.Turn, "");
        }
    }
}
