package Client.Game.Observables;

import Game.Map.Army.Color;
import Game.Player;
import Server.Game.Map.Territory;
import com.jfoenix.controls.datamodels.treetable.RecursiveTreeObject;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.util.ArrayList;

/**
 * Observable class of simplified user
 */
public class ObservableUser extends RecursiveTreeObject<ObservableUser> implements Player {
    public final IntegerProperty Id = new SimpleIntegerProperty(-1);

    public int getId() {
        return Id.get();
    }

    public final StringProperty Username = new SimpleStringProperty("");

    public String getUsername() { return Username.get(); }

    public volatile Color Color;

    public Color getColor() {
        return Color;
    }

    public final transient ObservableList<ObservableTerritory> Territories = FXCollections.observableArrayList(new ArrayList<ObservableTerritory>());

    public ObservableUser(int userId, String username, Color Color) {
        this.Id.set(userId);
        this.Username.set(username);
        if(Color != null)
            this.Color = Color;
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof  ObservableUser && this.Id.get() == ((ObservableUser)other).Id.get();
    }
}