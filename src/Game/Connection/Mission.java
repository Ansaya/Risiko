package Game.Connection;

/**
 * Message used to send mission to player
 */
public class Mission {
    public final Game.Map.Mission Mission;

    public Mission(Game.Map.Mission Mission) {
        this.Mission = Mission;
    }
}
