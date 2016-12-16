package Game;

import java.util.HashMap;
import java.util.function.Consumer;

/**
 * Can receive messages
 */
public abstract class MessageReceiver<T> {

    private volatile boolean listen = false;

    protected final HashMap<T, Consumer<Message>> messageHandlers = new HashMap<>();

    protected Consumer<Message> defaultHandler = null;

    private final String name;

    public MessageReceiver(String Name) {
        this.name = Name;
    }

    public void startListen(){
        listen = true;
    }

    public void stopListen() {
        listen = false;
    }

    /**
     * Start new thread to process the message
     *
     * @param PlayerId Player who sent the message
     * @param Type Message type
     * @param Incoming Json string received
     */
    public void setIncoming(int PlayerId, T Type, String Incoming) {
        this.setIncoming(new Message(PlayerId, Type, Incoming));
    }

    /**
     * Start new thread to process the message
     *
     * @param Message Message to be processed
     */
    public void setIncoming(Message Message) {
        if(!listen)
            return;

        Thread action = null;

        if(messageHandlers.containsKey(Message.Type))
            action = new Thread(() -> messageHandlers.get(Message.Type).accept(Message));
        else if(defaultHandler != null)
            action = new Thread(() ->  defaultHandler.accept(Message));

        if(action == null)
            return;

        action.setName(name + "-" + Message.Type.toString() + " handler");
        action.setDaemon(true);
        action.start();
    }

    public class Message {
        public int PlayerId;

        public T Type;

        public String Json;

        public Message(int PlayerId, T Type, String Incoming) {
            this.PlayerId = PlayerId;
            this.Type = Type;
            this.Json = Incoming;
        }
    }
}
