package Game;

import Game.Connection.MessageType;

/**
 * Can receive messages
 */
public abstract class MessageReceiver {

    protected int incomingPlayerId;

    protected MessageType incomingType;

    protected String incomingJson;

    /**
     * Set new message into the incoming buffer and notifies for handling
     * @param PlayerId Player from whom the message was received
     * @param Type Message type
     * @param Incoming Json string received
     */
    public void setIncoming(int PlayerId, MessageType Type, String Incoming) {
        this.incomingPlayerId = PlayerId;
        this.incomingType = Type;
        this.incomingJson = Incoming;
        this.incomingJson.notify();
    }
}
