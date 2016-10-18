package Game;

import Game.Connection.MessageType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.Consumer;

/**
 * Can receive messages
 */
public abstract class MessageReceiver implements Runnable {

    private boolean listen = false;

    private ArrayList<Message> queue = new ArrayList<>();

    protected HashMap<MessageType, Consumer<Message>> messageHandlers = new HashMap<>();

    protected Consumer<Message> defaultHandler = null;

    private Thread _instance;

    protected void startListen() {
        listen = true;
        _instance = new Thread(this);
        _instance.start();
    }

    protected void stopListen() {
        if(!listen)
            return;

        listen = false;

        try{
            _instance.interrupt();
            _instance.join();
        }catch (Exception e){}
    }

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
    private void waitIncoming() {
        try {
            synchronized (queue) {
                this.queue.wait();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        while (listen) {

            try {
                // If queue is empty wait for notification of new packet
                if(queue.isEmpty())
                    waitIncoming();

                Message message = queue.get(0);

                if(messageHandlers.containsKey(message.Type))
                    messageHandlers.get(message.Type).accept(message);
                else if(defaultHandler != null)
                    defaultHandler.accept(message);

                // Remove packet from queue
                this.queue.remove(0);

            }catch (Exception e) {
                e.printStackTrace();
            }
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
