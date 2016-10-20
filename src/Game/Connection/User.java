package Game.Connection;

import Game.Player;

/**
 * Simplified user
 */
public class User {
    private int userId;

    public int getUserId() { return userId; }

    private String username;

    public String getUsername() { return username; }

    public User(int UserId, String Username) {
        this.userId = UserId;
        this.username = Username;
    }

    public User(Player Player) {
        this(Player.getId(), Player.getName());
    }
}
