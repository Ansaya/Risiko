package Game;

import Game.Connection.MessageType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

/**
 * Can receive messages
 */
public abstract class MessageReceiver implements Runnable {

    private AtomicBoolean listen = new AtomicBoolean(false);

    protected HashMap<MessageType, Consumer<Message>> messageHandlers = new HashMap<>();

    protected Consumer<Message> defaultHandler = null;

    private ArrayList<Thread> activeActions = new ArrayList<>();

    private Thread _instance;

    protected void startListen() {
        listen.set(true);
        _instance = new Thread(this);
        _instance.start();
    }

    protected void stopListen() {
        if(!listen.get())
            return;

        listen.set(false);

        try{
            _instance.interrupt();
            _instance.join();
        }catch (Exception e){}
    }

    /**
     * Start new thread to process the message
     * @param PlayerId Player from whom the message was received
     * @param Type Message type
     * @param Incoming Json string received
     */
    public void setIncoming(int PlayerId, MessageType Type, String Incoming) {
        this.setIncoming(new Message(PlayerId, Type, Incoming));
    }

    /**
     * Start new thread to process the message
     *
     * @param Message Message to be processed
     */
    public void setIncoming(Message Message) {
        Thread action = null;

        if(messageHandlers.containsKey(Message.Type))
            action = new Thread(() -> messageHandlers.get(Message.Type).accept(Message));
        else if(defaultHandler != null)
            action = new Thread(() -> defaultHandler.accept(Message));

        activeActions.add(action);

        action.start();
    }

    /**
     * Wait for a new packet to come. Returns when a notification on queue is caught
     */
    private void waitIncoming() {
        try {
            synchronized (activeActions) {
                this.activeActions.wait();
            }
        } catch (Exception e) {}
    }

    @Override
    public void run() {
        while (listen.get()) {

            try {
                // If queue is empty wait for notification of new packet
                if(activeActions.isEmpty())
                    waitIncoming();

                activeActions.get(0).join();

                activeActions.remove(0);

            }catch (Exception e) {}
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
