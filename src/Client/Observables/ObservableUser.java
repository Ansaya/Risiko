package Client.Observables;

import Game.Connection.User;
import Game.Map.Territories;
import com.jfoenix.controls.datamodels.treetable.RecursiveTreeObject;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * Observable class of simplified user
 */
public class ObservableUser extends RecursiveTreeObject<ObservableUser> {
    public IntegerProperty id;

    public StringProperty username;

    public StringProperty color;

    public IntegerProperty territories = new SimpleIntegerProperty(0);

    public ObservableUser(int userId, String username, String Color) {
        this.id = new SimpleIntegerProperty(userId);
        this.username = new SimpleStringProperty(username);
        if(Color != null)
            this.color = new SimpleStringProperty(Color);
    }

    public ObservableUser(User User) {
        this(User.getUserId(), User.getUsername(), User.getColor() != null ? User.getColor().toString() : null);
    }
}