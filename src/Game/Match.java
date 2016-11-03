package Game;

import Game.Connection.*;
import Game.Map.*;
import Game.Map.Map;
import com.google.gson.Gson;

import java.util.*;

/**
 * Match object to manage game turns in a dedicated thread
 */
public class Match extends MessageReceiver {

    /**
     * Match id
     */
    private int id;

    public int getId() { return id; }

    /**
     * Players' list for this match (contains witnesses too)
     */
    private HashMap<Integer, Player> players = new HashMap<>();

    public HashMap<Integer, Player> getPlayers() { return players; }

    /**
     * Contains playing players' id only
     */
    private ArrayList<Integer> playersOrder = new ArrayList<>();

    /**
     * Game map
     */
    private Map gameMap = new Map();

    /**
     * Deck containing all territories cards plus two jolly
     */
    private DeckTerritory cards = new DeckTerritory();

    /**
     * Current turn
     */
    private Turn currentTurn;

    /**
     * Global matches counter
     */
    private static int counter = 0;

    /**
     * Instance a new match and starts the game
     *
     * @param Players Players who will play in this match
     */
    public Match(ArrayList<Player> Players) {
        if(Players.size() < 2 || Players.size() > 6)
            throw new UnsupportedOperationException(String.format("Not possible to start playing with %d users.", Players.size()));

        this.id = counter++;

        Color[] colors = Color.values();
        ArrayList<User> users = new ArrayList<>();

        for (int i = 0; i < Players.size(); i++) {
            Player p = Players.get(i);
            players.put(p.getId(), p);
            p.initMatch(colors[i], this.id);
            users.add(new User(p));
            playersOrder.add(p.getId());
        }

        System.out.println("Players in this match are " + players.size());

        // Send all user initial setup containing users and colors
        players.forEach((id, user) -> user.SendMessage(MessageType.Match, new Game.Connection.Match(users)));

        // Setup and start match message receiver

        listenersInit();
        startListen();

        // Start first setup turn
        this.currentTurn = new Turn(this, null, true);
    }

    /**
     * Initialize handlers for new messages and start message receiver
     */
    private void listenersInit() {
        messageHandlers.put(MessageType.Turn, (message) -> {
            // If a player notified end of his turn, go ahead with next player
            if(message.Json.equals("GoAhead")) {
                this.currentTurn.endTurn();
                this.currentTurn = new Turn(this, nextPlaying(this.currentTurn.getPlaying()), false);
            }

            if(message.Json.equals("Winner")){
                int winnerId = Integer.valueOf(message.Json.split("[-]")[0]);

                // Send all players winner of the game
                players.forEach((id, p) -> p.SendMessage(MessageType.GameState, new GameState(StateType.Winner, new User(players.get(winnerId)))));
            }
        });

        messageHandlers.put(MessageType.Chat, (message) -> {
            // Reroute message back to all players as MessageType-JsonSerializedMessage
            this.players.forEach((id, p) -> p.RouteMessage(message.Type + "-" + message.Json));
        });

        messageHandlers.put(MessageType.GameState, (message) -> {
            if(message.Json.equals(StateType.Abandoned.toString()))
                releasePlayer(message.PlayerId);
        });

        defaultHandler = (message) -> {
            // Any other message is routed to current turn to handle game progress
            this.currentTurn.setIncoming(message);
        };
    }

    /**
     * Release player from current match
     *
     * @param PlayerId Id of player to release
     */
    public void releasePlayer(int PlayerId) {
        Player p = players.get(PlayerId);

        // If player is playing abort match
        if(p.isPlaying()){
            // Handle match abort
            return;
        }

        // If player is only witness return it to lobby
        players.remove(PlayerId);
        GameController.getInstance().returnPlayer(p);
    }

    /**
     * Get next player in turn orders
     *
     * @param lastPlaying Last player who played
     * @return Player who has to play now
     */
    public Player nextPlaying(Player lastPlaying) {
        if(lastPlaying == null)
            return players.get(playersOrder.get(0));

        int current = playersOrder.indexOf(lastPlaying);

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
        private Player playing;

        public Player getPlaying() { return this.playing; }

        /**
         * Match where this turn is taking place
         */
        private Match match;

        /**
         * Queue for incoming messages
         */
        private ArrayList<Message> incoming = new ArrayList<>();

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
         * Waiter for incoming messages on the queue
         */
        public void waitIncoming() {
            try {
                synchronized (incoming) {
                    incoming.wait();
                }
            } catch (InterruptedException e) {}
        }

        /**
         * Deserializer for received messages
         */
        private Gson deserialize = new Gson();

        private Thread _instance;

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

            if(isSetup){
                this._instance = new Thread(() -> Setup(match.players.size() < 3));
                this._instance.start();
                return;
            }

            this._instance = new Thread(this);
            this._instance.start();
        }

        /**
         * Join turn thread
         */
        public void endTurn() {
            try {
                this._instance.join();
            } catch (InterruptedException e) {}
        }

        /**
         * Waits for requested message then returns it deserialized
         *
         * @param Type Type of message expected
         * @param PlayerId Player who the message is expected from. If set to -1 get message from any player
         * @return Deserialized message
         */
        private Object waitMessage(MessageType Type, int PlayerId) {
            Message received = null;

            // While correct message isn't received discard other messages
            while (true) {
                if(incoming.size() == 0)
                    waitIncoming();

                received = incoming.get(0);

                // If correct message is detected exit loop
                if(received.Type == Type)
                    if(PlayerId == -1)
                        break;
                    else if(received.PlayerId == PlayerId)
                        break;

                incoming.remove(0);
            }

            // Deserialize received message object
            Object obj = deserialize.fromJson(incoming.get(0).Json, incoming.get(0).Type.getMessageClass());

            // Remove message from queue
            incoming.remove(0);

            // Return deserialized object
            return obj;
        }

        /**
         * Send map update to all players
         *
         * @param Update Update to route
         */
        private void updateAll(MapUpdate Update) {
            match.players.forEach((id, p) -> p.SendMessage(MessageType.MapUpdate, Update));
        }

        /**
         * Define battle result based on dice throwing and send map update
         *
         * @param Attack Attack message received from attacker
         * @param Defense Defense message received from defender
         */
        private void Battle(Attack Attack, Defense Defense) {
            // Setup die
            Random die = new Random(System.nanoTime());
            ArrayList<Integer> atkDice = new ArrayList<>(), defDice = new ArrayList<>();

            // Throw die for every attacking army
            for(int i = 0; i < Attack.getArmies(); i++) {
                atkDice.add((int)(die.nextDouble() * 6 + 1));
            }

            // Throw die for every defending army
            for(int i = 0; i < Defense.getArmies(); i++) {
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

            Territory attacker = match.gameMap.getTerritories().get(Attack.getFrom().getTerritory());
            Territory defender = match.gameMap.getTerritories().get(Attack.getTo().getTerritory());

            // Remove defeated armies from attacker
            if(lostAtk > 0)
                attacker.canRemoveArmies(lostAtk);

            // If cannot remove all armies from defender territory change ownership and add necessary armies
            if(!defender.canRemoveArmies(lostDef)) {
                defender.setOwner(match.players.get(Attack.getFrom().getOwner().getId()));
                defender.addArmies(atkDice.size() - defDice.size());
            }

            // Create resulting map update after battle
            ArrayList<Territory> result = new ArrayList<>();
            result.add(attacker);
            if(lostDef != 0)
                result.add(defender);

            // Send update to all players
            updateAll(new MapUpdate(result));

            // If attacker hasn't conquered the territory or no more armies can be moved go ahead
            if(lostAtk != 0 || attacker.getArmies() == 1)
                return;

            // If there are some armies which can be moved to new territory
            int attakerId = Attack.getFrom().getOwner().getId();

            // Send special move and wait for response
            match.players.get(attakerId).SendMessage(MessageType.SpecialMoving, new SpecialMoving(Attack));
            SpecialMoving update = (SpecialMoving) waitMessage(MessageType.SpecialMoving, attakerId);

            // If no other move is performed go ahead
            if(update.getFrom() == null)
                return;

            // If armies have been moved update map
            Territory from = match.gameMap.getTerritories().get(update.getFrom().getTerritory()), to = match.gameMap.getTerritories().get(update.getTo().getTerritory());

            from.canRemoveArmies(update.getTo().getNewArmies());
            to.addArmies(update.getTo().getNewArmies());

            // Send new placement to all players
            updateAll(new MapUpdate(new ArrayList<>(Arrays.asList(from, to))));
        }

        /**
         * Takes care of initial armies distribution and territories choosing turns
         */
        private void Setup(boolean twoOnly) {
            // Setup
            Player last = null;

            // Create AI player without socket
            Player ai = new Player(this.match.id);

            // Save last player id to trigger ai choice during initial phase
            int lastId = match.playersOrder.get(match.playersOrder.size() - 1);
            ArrayList<Territories> toGo = new ArrayList<>(Arrays.asList(Territories.values()));
            toGo.remove(Territories.Jolly1);
            toGo.remove(Territories.Jolly2);

            /* Territories choice */

            while (toGo.size() > 0){
               // Send next player positioning message with one army
               (last = match.nextPlaying(last)).SendMessage(MessageType.Positioning, new Positioning(1));

                // Get chosen territory
                MapUpdate update = (MapUpdate) waitMessage(MessageType.MapUpdate, last.getId());

                // Update game map
                Territory toUpdate = match.gameMap.getTerritories().get(update.getUpdated().get(0).getTerritory());
                toUpdate.addArmies(1);
                toUpdate.setOwner(match.players.get(last.getId()));

                // Remove it from remaining territories
                toGo.remove(toUpdate.getTerritory());

                // Send update to all players
                updateAll(new MapUpdate(toUpdate));

                // At the end of the row chose one for ai if only two players are in match
                if(twoOnly && last.getId() == lastId){
                    // Get random territory from remaining territories
                    toUpdate = match.gameMap.getTerritories().get(toGo.get((new Random()).nextInt(toGo.size())));
                    toUpdate.addArmies(3);
                    toUpdate.setOwner(ai);
                    updateAll(new MapUpdate(toUpdate));
                    toGo.remove(toUpdate.getTerritory());
                }
            }

            /* Armies displacement */

            // Generate initial armies for each player
            int startingArmies = 50 - (5 * playersOrder.size());

            // Send player initial armies
            playersOrder.forEach(pId -> {
                Player p = match.players.get(pId);

                // Calculate remaining armies to send                 (    total      -   already placed armies   )
                p.SendMessage(MessageType.Positioning, new Positioning(startingArmies - p.getTerritories().size()));
            });

            // Wait for all players to return initial displacement
            for(int i = playersOrder.size(); i > 0; i--){
                MapUpdate u = (MapUpdate) waitMessage(MessageType.MapUpdate, -1);
                u.getUpdated().forEach(t -> match.gameMap.getTerritories().get(t.getTerritory()).addArmies(t.getNewArmies()));
            }

            // Notify end of setup when completed
            match.setIncoming(lastId, MessageType.Turn, "goAhead");
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
            Cards redeemed = (Cards) waitMessage(MessageType.Cards, playing.getId());

            // If there is a combination check for validity
            if(redeemed.getCombination().size() != 0)
                newArmies += match.cards.isCombinationValid(true, redeemed.getCombination());

            // Send new armies to player
            playing.SendMessage(MessageType.Positioning, new Positioning(newArmies));

            // Wait to get new armies displacement over player's territories
            MapUpdate newPlacement = (MapUpdate) waitMessage(MessageType.MapUpdate, playing.getId());

            // Update armies number in game map
            newPlacement.getUpdated().forEach((t) -> match.gameMap.getTerritories().get(t.getTerritory()).addArmies(t.getNewArmies()));

            // Send update to all players
            updateAll(newPlacement);

            // Ask player to attack
            playing.SendMessage(MessageType.Attack, new Attack(null, null, 0));

            int beforeAtkTerritories = playing.getTerritories().size();

            // Wait for all attack messages from player
            while (true) {
                // Get attack message from player
                Attack newAttack = (Attack) waitMessage(MessageType.Attack, playing.getId());

                // If armies are zero end attack phase
                if(newAttack.getArmies() == 0)
                    break;

                // Standard defense set to one army
                Defense defense = new Defense(newAttack.getFrom(), newAttack.getTo(), 1);

                // If more than one army is present on defender territory ask player how many he want to use
                if(newAttack.getTo().getArmies() > 1) {
                    // Get defender player id
                    int defenderId = newAttack.getTo().getOwner().getId();

                    // Send defender the attack message
                    match.players.get(defenderId).SendMessage(MessageType.Attack, newAttack);

                    // Wait for response
                    defense = (Defense) waitMessage(MessageType.Defense, defenderId);
                }

                // Perform battle
                Battle(newAttack, defense);

                // After each battle check if mission completed
                if(Mission.Completed(playing)){
                    match.setIncoming(playing.getId(), MessageType.Turn, playing.getId() + "-Winner");
                }
            }

            // If player has conquered at least one new territory send him a card
            if(beforeAtkTerritories < playing.getTerritories().size())
                playing.SendMessage(MessageType.Cards, new Cards(match.cards.next()));

            // Implement end turn moving

            // Notify end of turn when completed
            match.setIncoming(playing.getId(), MessageType.Turn, "goAhead");
        }
    }
}
