package Game;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.Consumer;

/**
 * Can receive messages
 */
public abstract class MessageReceiver<T> {

    private transient volatile boolean execute = false;

    private transient final ArrayList<Message> queue = new ArrayList<>();

    protected transient final HashMap<T, Consumer<Message>> messageHandlers = new HashMap<>();

    protected transient Consumer<Message> defaultHandler = null;

    private transient final String name;

    private transient volatile Thread _instance;

    public MessageReceiver(String Name) {
        this.name = Name;
    }

    public void startExecutor(){
        System.out.println(name + ": Executor start request.");
        if(execute)
            return;

        execute = true;
        _instance = new Thread(this::executor, name + "-Executor");
        _instance.start();
    }

    public void stopExecutor() {
        System.out.println(name + ": Executor stop request.");
        if(!execute)
            return;

        execute = false;
        synchronized (queue){
            queue.notify();
        }

        try {
            _instance.join();
        } catch (Exception e) {}
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
        synchronized (queue){
            queue.add(Message);
            queue.notify();
        }
    }

    private void executor() {
        while (execute) {
            try {
                if (queue.isEmpty())
                    synchronized (queue) { queue.wait(); }

                Message m;

                synchronized (queue) {
                    m = queue.remove(0);
                }
                Thread action;

                if (messageHandlers.containsKey(m.Type))
                    action = new Thread(() -> messageHandlers.get(m.Type).accept(m));
                else if (defaultHandler != null)
                    action = new Thread(() -> defaultHandler.accept(m));
                else
                    continue;

                action.setName(name + "-" + m.Type.toString() + " handler");
                action.setDaemon(true);
                action.start();
            } catch (Exception e){
                if(!execute) break;
            }
        }
        System.out.println(name + ": Executor stopped.");
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
