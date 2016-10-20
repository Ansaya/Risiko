package Client.Observables;

import Game.Connection.User;
import com.jfoenix.controls.datamodels.treetable.RecursiveTreeObject;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * Observable class of simplified user
 */
public class ObservableUser extends RecursiveTreeObject<ObservableUser> {
    public StringProperty UserId;

    public StringProperty Username;

    public ObservableUser(int userId, String username) {
        this.UserId = new SimpleStringProperty(String.valueOf(userId));
        this.Username = new SimpleStringProperty(username);
    }

    public ObservableUser(User User) {

        this(User.getUserId(), User.getUsername());
    }
}