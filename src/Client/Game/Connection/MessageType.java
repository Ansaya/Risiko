package Client.Game.Connection;

import Client.Game.Map.Territory;
import Client.Game.Player;
import Game.Connection.*;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;

/**
 * Type of possible message objects
 */
public enum MessageType implements TypeEnumerator {
    Chat(new TypeToken<Chat<Player>>(){}.getType()),
    Lobby(new TypeToken<Lobby<Player>>(){}.getType()),
    MatchLobby(new TypeToken<MatchLobby<Match<Player>>>(){}.getType()),
    Match(new TypeToken<Match<Player>>(){}.getType()),
    Mission(Game.Connection.Mission.class),
    Turn(String.class),
    Battle(new TypeToken<Battle<Territory>>(){}.getType()),
    Cards(Game.Connection.Cards.class),
    Positioning(Game.Connection.Positioning.class),
    MapUpdate(new TypeToken<MapUpdate<Territory>>(){}.getType()),
    GameState(new TypeToken<GameState<Player>>(){}.getType());

    MessageType(Type Class) { this.c = Class; }

    private Type c;

    @Override
    public Type getType() { return this.c; }
}
