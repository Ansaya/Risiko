package Client.Observables;

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

    public StringProperty color = new SimpleStringProperty("");

    public IntegerProperty territories = new SimpleIntegerProperty(0);

    public ObservableUser(int userId, String username, String Color) {
        this.id.set(userId);
        this.username.set(username);
        if(Color != null)
            this.color.set(Color);
    }

    @Override
    public boolean equals(Object o) {
        if(o.getClass() != ObservableUser.class)
            return false;

        return this.id.get() == ((ObservableUser)o).id.get();
    }
}