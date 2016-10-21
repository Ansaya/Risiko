package Client.Observables;

import Game.Connection.User;
import com.jfoenix.controls.datamodels.treetable.RecursiveTreeObject;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * Observable class of simplified user
 */
public class ObservableUser extends RecursiveTreeObject<ObservableUser> {
    public IntegerProperty UserId;

    public StringProperty Username;

    public ObservableUser(int userId, String username) {
        this.UserId = new SimpleIntegerProperty(userId);
        this.Username = new SimpleStringProperty(username);
    }

    public ObservableUser(User User) {

        this(User.getUserId(), User.getUsername());
    }
}