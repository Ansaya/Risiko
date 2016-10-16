package Game.Connection;

import Game.GameController;

/**
 * Global message dispatcher
 */
public class MessageDispatcher implements Runnable {
    private static MessageDispatcher _instance = new MessageDispatcher();

    public static MessageDispatcher getInstance() {
        return _instance;
    }

    private String headerWjson;

    public void setIncoming(String Packet) {
        this.headerWjson = Packet;
        this.headerWjson.notify();
    }

    private MessageDispatcher() {
    }

    @Override
    public void run() {
        while (true){
            try {
                this.headerWjson.wait();

                // Deserialize packet received as MatchId-PlayerId-MessageType-JsonSerializedObject
                String[] infos = headerWjson.split("[-]");

                // If match's id is zero the player is in the lobby
                if(infos[0] == "0")
                    GameController.getInstance().setIncoming(Integer.valueOf(infos[1]), MessageType.valueOf(infos[2]), infos[3]);

                // Route message to correct match event dispatcher
                GameController.getInstance().getMatch(Integer.parseInt(infos[0])).setIncoming(Integer.parseInt(infos[1]), MessageType.valueOf(infos[2]), infos[3]);

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
