package Game.Connection;

import Game.Color;
import Game.Player;

/**
 * Simplified user
 */
public class User {
    private int userId;

    public int getUserId() { return userId; }

    private String username;

    public String getUsername() { return username; }

    private Color color;

    public Color getColor() { return this.color; }

    public void setColor(Color Color) { this.color = Color; }

    public User(int UserId, String Username, Color Color) {
        this.userId = UserId;
        this.username = Username;
        this.color = Color;
    }

    public User(Player Player) {
        this(Player.getId(), Player.getName(), Player.getColor());
    }
}
