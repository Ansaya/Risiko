package Client.Game.Connection;

import Client.Game.Observables.ObservableTerritory;
import Client.Game.Observables.ObservableUser;
import Game.Connection.*;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;

/**
 * Type of possible message objects
 */
public enum MessageType {
    Chat(new TypeToken<Chat<ObservableUser>>(){}.getType()),
    Lobby(new TypeToken<Lobby<ObservableUser>>(){}.getType()),
    Match(new TypeToken<Match<ObservableUser>>(){}.getType()),
    Battle(new TypeToken<Battle<ObservableTerritory>>(){}.getType()),
    Cards(Cards.class),
    Positioning(Positioning.class),
    SpecialMoving(new TypeToken<SpecialMoving<ObservableTerritory>>(){}.getType()),
    MapUpdate(new TypeToken<MapUpdate<ObservableTerritory>>(){}.getType()),
    GameState(new TypeToken<GameState<ObservableUser>>(){}.getType());

    MessageType(Type Class) { this.c = Class; }

    private Type c;

    public Type getType() { return this.c; }
}
