package Game.Connection;

import Game.User;

/**
 * Chat message packet
 */
public class Chat {

    private String message;

    public String getMessage() { return this.message; }

    private String sender;

    public String getSender() { return this.sender; }

    public  Chat(String Sender, String Message) {
        this.sender = Sender;
        this.message = Message;
    }

    public Chat(User Sender, String Message) {
        this(Sender.getName(), Message);
    }
}
