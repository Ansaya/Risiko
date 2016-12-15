package Game;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.Consumer;

/**
 * Can receive messages
 */
public abstract class MessageReceiver<T> implements Runnable {

    private volatile boolean listen = false;

    protected final HashMap<T, Consumer<Message>> messageHandlers = new HashMap<>();

    protected Consumer<Message> defaultHandler = null;

    private final ArrayList<Thread> activeActions = new ArrayList<>();

    private final Thread _instance = new Thread(this);

    protected void startListen(String Name) {
        listen = true;
        _instance.setName(Name);
        _instance.start();
    }

    protected void stopListen() {
        if(!listen)
            return;

        listen = false;

        activeActions.forEach(a -> {
            try {
                a.join();
            } catch (Exception e) {}
        });

        try{
            synchronized (activeActions) {
                activeActions.notify();
            }
            _instance.join();
        }catch (Exception e){}
    }

    /**
     * Start new thread To process the message
     * @param PlayerId Player From whom the message was received
     * @param Type Message type
     * @param Incoming Json string received
     */
    public void setIncoming(int PlayerId, T Type, String Incoming) {
        this.setIncoming(new Message(PlayerId, Type, Incoming));
    }

    /**
     * Start new thread To process the message
     *
     * @param Message Message To be processed
     */
    public void setIncoming(Message Message) {
        Thread action = null;

        if(messageHandlers.containsKey(Message.Type))
            action = new Thread(() -> {
                messageHandlers.get(Message.Type).accept(Message);
                synchronized (activeActions){
                    activeActions.notify();
                }
            });
        else if(defaultHandler != null)
            action = new Thread(() -> {
                defaultHandler.accept(Message);
                synchronized (activeActions){
                    activeActions.notify();
                }
            });

        action.setName(_instance.getName() + "-" + Message.Type.toString() + " handler");

        activeActions.add(action);

        action.start();
    }

    /**
     * Wait for a new packet To come. Returns when a notification on queue is caught
     */
    private void waitIncoming() {
        try {
            synchronized (activeActions) {
                activeActions.wait();
            }
        } catch (Exception e) {}
    }

    @Override
    public void run() {
        while (listen) {

            try {
                // If queue is empty wait for notification of new packet
                if(activeActions.isEmpty())
                    waitIncoming();

                activeActions.remove(0).join();

            }catch (Exception e) {
                if(!listen)
                    break;
            }
        }
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
