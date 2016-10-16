package Game.Connection;

/**
 * Type of possible message objects
 */
public enum MessageType {
    Chat,
    Turn,
    Attack,
    Defense,
    Cards,
    Positioning,
    Moving,
    SpecialMoving,
    MapUpdate,
    GameState;
}
