package Server.Game;

import Game.Color;
import Game.Connection.*;
import Game.Map.*;
import Server.Game.Map.DeckTerritory;
import Server.Game.Map.Map;
import Game.Connection.Attack;
import Game.Connection.Defense;
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
     * Contains playing players' id only
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

        // Setup players
        final Color[] colors = Color.values();
        final AtomicInteger i = new AtomicInteger(0);
        Players.forEach(p -> {
            p.initMatch(colors[i.getAndIncrement()], this.id);
            players.put(p.id, p);
            playersOrder.add(p.id);
        });

        // Send all user initial setup containing users and colors
        players.forEach((id, user) -> user.SendMessage(MessageType.Match, new Game.Connection.Match<>(Players)));

        // Setup and start match message receiver
        listenersInit();
        startListen("Match " + this.id);

        // Start first setup turn
        System.out.println("Match " + this.id + ": Started game with " + players.size() + " players.");
        this.currentTurn = new Turn(this, null, true);
    }

    /**
     * Stop current match thread and returns players to GameController
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
                default:
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
     * Implementation of a complete turn of game for a player
     */
    private class Turn implements Runnable {
        /**
         * Currently active player
         */
        private final Player playing;

        public Player getPlaying() { return this.playing; }

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
        public void setIncoming(Message Message) {
            synchronized (incoming){
                incoming.add(Message);
                incoming.notify();
            }
        }

        /**
         * Deserializer for received messages
         */
        private final Gson deserialize = new Gson();

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
        public void endTurn() {
            try {
                _instance.interrupt();
                _instance.join();
            } catch (Exception e) {}
        }

        /**
         * Waits for requested message then returns it deserialized
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
            return deserialize.fromJson(received.Json, received.Type.getType());
        }

        /**
         * Define battle result based on dice throwing and send map update
         *
         * @param Attack Attack message received from attacker
         * @param Defense Defense message received from defender
         */
        private void Battle(Attack<Territory> Attack, Defense<Territory> Defense) {
            // Setup die
            Random die = new Random(System.nanoTime());
            ArrayList<Integer> atkDice = new ArrayList<>(), defDice = new ArrayList<>();

            // Throw die for every attacking army
            for(int i = 0; i < Attack.armies; i++) {
                atkDice.add((int)(die.nextDouble() * 6 + 1));
            }

            // Throw die for every defending army
            for(int i = 0; i < Defense.armies; i++) {
                defDice.add((int)(die.nextDouble() * 6 + 1));
            }

            // Sort dice descending
            Collections.sort(atkDice);
            Collections.reverse(atkDice);
            Collections.sort(defDice);
            Collections.reverse(defDice);

            int lostAtk = 0, lostDef = 0;

            // Compare couples of dice to assign victory
            for (int i = 0; i < defDice.size();i++) {
                if(defDice.get(i) >= atkDice.get(i))
                    lostAtk++;
                else
                    lostDef++;
            }

            final Territory attacker = match.gameMap.territories.get(Attack.from.territory);
            final Territory defender = match.gameMap.territories.get(Attack.to.territory);

            // Remove defeated armies from attacker
            if(lostAtk > 0)
                attacker.canRemoveArmies(lostAtk);

            // If cannot remove all armies from defender territory change ownership and add necessary armies
            if(!defender.canRemoveArmies(lostDef)) {
                defender.owner = match.players.get(Attack.from.owner.id);
                defender.addArmies(atkDice.size() - defDice.size());
            }

            // Create resulting map update after battle
            ArrayList<Territory> result = new ArrayList<>();
            result.add(attacker);
            if(lostDef != 0)
                result.add(defender);

            // Send update to all players
            match.sendAll(MessageType.MapUpdate, new MapUpdate<>(result));

            // If attacker hasn't conquered the territory or no more armies can be moved go ahead
            if(lostAtk != 0 || attacker.getArmies() == 1)
                return;

            // If there are some armies which can be moved to new territory
            final int attakerId = Attack.from.owner.id;

            // Send special move and wait for response
            match.players.get(attakerId).SendMessage(MessageType.SpecialMoving, new SpecialMoving<>(Attack, Attack.from.getArmies() - Attack.armies - 1));
            final SpecialMoving<Territory> update = waitMessage(MessageType.SpecialMoving, attakerId);

            // If no other move is performed go ahead
            if(update.from == null)
                return;

            // If armies have been moved update map
            final Territory from = match.gameMap.territories.get(update.from.territory), to = match.gameMap.territories.get(update.to.territory);

            from.canRemoveArmies(update.to.newArmies);
            to.addArmies(update.to.newArmies);

            // Send new placement to all players
            match.sendAll(MessageType.MapUpdate, new MapUpdate<>(new ArrayList<>(Arrays.asList(from, to))));
        }

        /**
         * Takes care of initial armies distribution and territories choosing turns
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

                // Get chosen territory
                MapUpdate<Territory> update = waitMessage(MessageType.MapUpdate, last.id);

                // Update game map
                Territory toUpdate = match.gameMap.territories.get(update.updated.get(0).territory);
                toUpdate.addArmies(1);
                toUpdate.owner = match.players.get(last.id);

                // Remove it from remaining territories
                toGo.remove(toUpdate.territory);

                // Send update to all players
                match.sendAll(MessageType.MapUpdate, new MapUpdate<>(toUpdate));

                // At the end of the row chose one for ai if only two players are in match
                if(twoOnly && last.id == lastId){
                    // Get random territory from remaining territories
                    toUpdate = match.gameMap.territories.get(toGo.get((new Random()).nextInt(toGo.size())));
                    toUpdate.addArmies(3);
                    toUpdate.owner = ai;
                    toGo.remove(toUpdate.territory);
                    match.sendAll(MessageType.MapUpdate, new MapUpdate<>(toUpdate));
                }
            }

            /* armies displacement */

            // Generate initial armies for each player
            final int startingArmies = 50 - (5 * playersOrder.size());

            // Send player initial armies
            playersOrder.forEach(pId -> {
                Player p = match.players.get(pId);

                // Calculate remaining armies to send                 (    total      -   already placed armies   )
                p.SendMessage(MessageType.Positioning, new Positioning(startingArmies - p.getTerritories().size()));
            });

            // Wait for all players to return initial displacement
            ArrayList<Territory> finalUpdate = new ArrayList<>();
            for(int i = playersOrder.size(); i > 0; i--){
                MapUpdate<Territory> u = waitMessage(MessageType.MapUpdate, -1);
                u.updated.forEach(t -> {
                    Territory tr = match.gameMap.territories.get(t.territory);
                    tr.addArmies(t.newArmies);
                    finalUpdate.add(tr);
                });
            }

            sendAll(MessageType.MapUpdate, new MapUpdate<>(finalUpdate));

            // Notify end of setup when completed
            match.setIncoming(lastId, MessageType.Turn, "");
        }

        @Override
        public void run() {
            // Implement turn phases

            // Ask for a card combination to get more armies
            playing.SendMessage(MessageType.Cards, new Cards());

            // Calculate standard armies reinforcement
            int newArmies = (playing.getTerritories().size() / 3) >= 3 ? (playing.getTerritories().size() / 3) : 3;

            // Extra armies due to continent ownership
            newArmies += Continent.bonusArmies(playing.getTerritories());

            // Wait for Cards message to return from user
            final Cards redeemed = waitMessage(MessageType.Cards, playing.id);

            // If there is a combination check for validity
            if(redeemed.combination.size() != 0)
                newArmies += match.cards.isCombinationValid(true, redeemed.combination);

            // Send new armies to player
            playing.SendMessage(MessageType.Positioning, new Positioning(newArmies));

            // Wait to get new armies displacement over player's territories
            final MapUpdate<Territory> newPlacement = waitMessage(MessageType.MapUpdate, playing.id);

            // Update armies number in game map
            newPlacement.updated.forEach((t) -> match.gameMap.territories.get(t.territory).addArmies(t.newArmies));

            // Send update to all players
            match.sendAll(MessageType.MapUpdate, newPlacement);

            // Ask player to attack
            playing.SendMessage(MessageType.Attack, new Attack<Player>(null, null, 0));

            final int beforeAtkTerritories = playing.getTerritories().size();

            // Wait for all attack messages from player
            while (true) {
                // Get attack message from player
                Attack<Territory> newAttack = waitMessage(MessageType.Attack, playing.id);

                // If armies are zero end attack phase
                if(newAttack.armies == 0)
                    break;

                // Standard defense set to one army
                Defense<Territory> defense = new Defense<>(newAttack.from, newAttack.to, 1);

                // If more than one army is present on defender territory ask player how many he want to use
                if(newAttack.to.getArmies() > 1) {
                    // Get defender player id
                    int defenderId = newAttack.to.owner.id;

                    // Send defender the attack message
                    match.players.get(defenderId).SendMessage(MessageType.Attack, newAttack);

                    // Wait for response
                    defense = waitMessage(MessageType.Defense, defenderId);
                }

                // Perform battle
                Battle(newAttack, defense);

                // After each battle check if mission completed
                if(Mission.Completed(playing)){
                    match.setIncoming(playing.id, MessageType.Turn, playing.id + "-Winner");
                }
            }

            // If player has conquered at least one new territory send him a card
            if(beforeAtkTerritories < playing.getTerritories().size())
                playing.SendMessage(MessageType.Cards, new Cards(match.cards.next()));

            // Implement end turn moving

            // Notify end of turn when completed
            match.setIncoming(playing.id, MessageType.Turn, "");
        }
    }
}
