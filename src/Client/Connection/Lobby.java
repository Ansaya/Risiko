package Client.Connection;

import Client.Observables.ObservableUser;
import java.util.ArrayList;

/**
 * Created by fiore on 05/12/2016.
 */
public class Lobby {
    public final ArrayList<ObservableUser> toAdd = new ArrayList<>();

    public final ArrayList<ObservableUser> toRemove = new ArrayList<>();
}
