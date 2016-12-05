package Client.Connection;

import Client.Observables.ObservableUser;

/**
 * Created by fiore on 05/12/2016.
 */
public class Chat {

    public final String message;

    public final ObservableUser sender;

    public Chat(ObservableUser Sender, String Message) {
        this.sender = Sender;
        this.message = Message;
    }
}
