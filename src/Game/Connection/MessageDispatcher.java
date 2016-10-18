package Game.Connection;

import Game.GameController;

import java.util.ArrayList;

/**
 * Global message dispatcher
 */
public class MessageDispatcher implements Runnable {
    private static MessageDispatcher _instance = new MessageDispatcher();

    public static MessageDispatcher getInstance() {
        return _instance;
    }

    private boolean listen = false;

    private ArrayList<String> packetsQueue = new ArrayList<>();

    private Thread _threadInstance;

    private MessageDispatcher() {}

    public void init() {
        listen = true;
        this._threadInstance = new Thread(this);
        this._threadInstance.start();
    }

    /**
     * Add a packet to the queue
     *
     * @param Packet Packet to add
     */
    public void setIncoming(String Packet) {
        this.packetsQueue.add(Packet);
        synchronized (packetsQueue) {
            this.packetsQueue.notify();
        }
    }

    /**
     * Shutdown message dispatcher (cannot set it up again)
     */
    public void terminate() {
        if(!listen)
            return;

        listen = false;
        try {
            this._threadInstance.interrupt();
            this._threadInstance.join();
            this._threadInstance = null;
        }catch (Exception e) {}
    }

    @Override
    public void run() {
        while (listen){
            try {
                // If queue is empty wait for new packet before proceeding
                if(this.packetsQueue.isEmpty()) {
                    System.out.println("MessageDispatcher: waiting...");
                    synchronized (packetsQueue) {
                        this.packetsQueue.wait();
                    }
                }

                System.out.println("Packet: " + packetsQueue.get(0));

                // Deserialize packet received as MatchId-PlayerId-MessageType-JsonSerializedObject
                String[] infos = packetsQueue.get(0).split("[-]");

                int matchId = Integer.parseInt(infos[0]);
                int playerId = Integer.parseInt(infos[1]);
                MessageType type = MessageType.valueOf(infos[2]);

                // If match's id is zero the player is in the lobby
                if(matchId == 0) {
                    GameController.getInstance().setIncoming(playerId, type, infos[3]);
                    System.out.println("Sent to GC");
                }
                else {
                    // Route message to correct match event dispatcher
                    GameController.getInstance().getMatch(matchId).setIncoming(playerId, type, infos[3]);
                    System.out.println("Sent to match " + matchId);
                }

                // Remove packet from queue
                packetsQueue.remove(0);

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
