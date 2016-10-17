package Game.Connection;

import Game.Player;
import java.util.ArrayList;

/**
 * Lobby packet
 */
public class Lobby {

    private ArrayList<User> toAdd = new ArrayList<>();

    public ArrayList<User> getToAdd() { return toAdd; }

    private ArrayList<User> toRemove = new ArrayList<>();

    public ArrayList<User> getToRemove() { return toRemove; }

    public Lobby(ArrayList<Player> ToAdd, ArrayList<Player> ToRemove) {
        if(ToAdd != null)
            ToAdd.forEach((p) -> toAdd.add(new User(p.getId(), p.getName())));

        if(ToRemove != null)
            ToRemove.forEach((p) -> toRemove.add(new User(p.getId(), p.getName())));
    }

    public Lobby(Player ToAdd, Player ToRemove) {
        if(ToAdd != null)
            toAdd.add(new User(ToAdd.getId(), ToAdd.getName()));

        if(ToRemove != null)
            toRemove.add(new User(ToRemove.getId(), ToRemove.getName()));
    }

    public class User {
        public int UserId;

        public String Username;

        public User(int UserId, String Username) {
            this.UserId = UserId;
            this.Username = Username;
        }
    }
}
