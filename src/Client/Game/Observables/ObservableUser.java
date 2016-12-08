package Client.Game.Observables;

import Game.Color;
import com.jfoenix.controls.datamodels.treetable.RecursiveTreeObject;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * Observable class of simplified user
 */
public class ObservableUser extends RecursiveTreeObject<ObservableUser> {
    public IntegerProperty id = new SimpleIntegerProperty(-1);

    public StringProperty username = new SimpleStringProperty("");

    public volatile Color color;

    public IntegerProperty territories = new SimpleIntegerProperty(0);

    public ObservableUser(int userId, String username, Color Color) {
        this.id.set(userId);
        this.username.set(username);
        if(Color != null)
            this.color = Color;
    }

    @Override
    public boolean equals(Object o) {
        if(o == null)
            return false;

        if(o.getClass() != ObservableUser.class)
            return false;

        return this.id.get() == ((ObservableUser)o).id.get();
    }
}