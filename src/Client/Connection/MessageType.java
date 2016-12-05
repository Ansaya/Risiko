package Client.Connection;

/**
 * Type of possible message objects
 */
public enum MessageType {
    Chat(Chat.class),
    Lobby(Lobby.class),
    Match(Match.class),
    Turn(null),
    Attack(Attack.class),
    Defense(Defense.class),
    Cards(Game.Connection.Cards.class),
    Positioning(Game.Connection.Positioning.class),
    Moving(null),
    SpecialMoving(SpecialMoving.class),
    MapUpdate(MapUpdate.class),
    GameState(GameState.class);

    MessageType(Class Class) { this.c = Class; }

    private Class c;

    public Class getMessageClass() { return this.c; }
}
