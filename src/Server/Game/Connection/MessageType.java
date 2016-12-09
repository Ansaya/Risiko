package Server.Game.Connection;

import Server.Game.Map.Territory;
import Server.Game.Player;
import Game.Connection.*;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;

/**
 * Type of possible message objects
 */
public enum MessageType {
    Chat(new TypeToken<Chat<Player>>(){}.getType()),
    Lobby(new TypeToken<Lobby<Player>>(){}.getType()),
    Match(new TypeToken<Match<Player>>(){}.getType()),
    Turn(null),
    Attack(new TypeToken<Attack<Player>>(){}.getType()),
    Defense(new TypeToken<Defense<Player>>(){}.getType()),
    Cards(Cards.class),
    Positioning(Positioning.class),
    Moving(null),
    SpecialMoving(new TypeToken<SpecialMoving<Territory>>(){}.getType()),
    MapUpdate(new TypeToken<MapUpdate<Territory>>(){}.getType()),
    GameState(new TypeToken<GameState<Player>>(){}.getType());

    MessageType(Type Class) { this.c = Class; }

    private Type c;

    public Type getType() { return this.c; }
}