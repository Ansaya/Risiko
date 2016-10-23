package Game.Connection;

import Game.Player;

/**
 * Chat message packet
 */
public class Chat {

    private String message;

    public String getMessage() { return this.message; }

    private User sender;

    public User getSender() { return this.sender; }

    public Chat(User Sender, String Message) {
        this.sender = Sender;
        this.message = Message;
    }

    public Chat(Player Sender, String Message) {
        this(new User(Sender), Message);
    }
}
