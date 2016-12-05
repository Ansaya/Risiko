package Game.Connection;

/**
 * Chat message packet
 */
public class Chat<T> {

    public final String message;

    public final T sender;

    public Chat(T Sender, String Message) {
        this.sender = Sender;
        this.message = Message;
    }
}
