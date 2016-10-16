package Game;

import Game.Connection.MessageType;

import java.util.ArrayList;

/**
 * Can receive messages
 */
public abstract class MessageReceiver {

    protected ArrayList<Message> queue = new ArrayList<>();

    /**
     * Set new message into the incoming buffer and notifies for handling
     * @param PlayerId Player from whom the message was received
     * @param Type Message type
     * @param Incoming Json string received
     */
    public void setIncoming(int PlayerId, MessageType Type, String Incoming) {
        this.setIncoming(new Message(PlayerId, Type, Incoming));
    }

    public void setIncoming(Message Message) {
        this.queue.add(Message);
        synchronized (queue) {
            this.queue.notify();
        }
    }

    /**
     * Wait for a new packet to come. Returns when a notification on queue is caught
     */
    protected void waitIncoming() {
        try {
            synchronized (queue) {
                this.queue.wait();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public class Message {
        public int PlayerId;

        public MessageType Type;

        public String Json;

        public Message(int PlayerId, MessageType Type, String Incoming) {
            this.PlayerId = PlayerId;
            this.Type = Type;
            this.Json = Incoming;
        }
    }
}
