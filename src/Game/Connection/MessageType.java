package Game.Connection;

/**
 * Type of possible message objects
 */
public enum MessageType {
    Chat(Game.Connection.Chat.class),
    Lobby(Lobby.class),
    Match(Game.Connection.Match.class),
    Turn(null),
    Attack(Game.Connection.Attack.class),
    Defense(Game.Connection.Defense.class),
    Cards(Game.Connection.Cards.class),
    Positioning(Game.Connection.Positioning.class),
    Moving(null),
    SpecialMoving(Game.Connection.SpecialMoving.class),
    MapUpdate(Game.Connection.MapUpdate.class),
    GameState(Game.Connection.GameState.class);

    MessageType(Class Class) { this.c = Class; }

    private Class c;

    public Class getMessageClass() { return this.c; }
}
