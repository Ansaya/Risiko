package Server.UI;

import Game.Map.Maps;
import Server.Game.Match;
import Server.Game.Player;
import com.jfoenix.controls.JFXButton;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.function.BiConsumer;

public class Controller implements Initializable {

    @FXML
    private TableView playersTable;

    @FXML
    private TableColumn<Player, Integer> playerIdColumn;

    @FXML
    private TableColumn<Player, String> playerUsernameColumn;

    @FXML
    private TableView matchesTable;

    @FXML
    private TableColumn<Match, Integer> matchIdColumn;

    @FXML
    private TableColumn<Match, String> matchNameColumn;

    @FXML
    private TableColumn<Match, Maps> matchGameMapColumn;

    @FXML
    private TableColumn<Match, Integer> matchPlayersColumn;

    @FXML
    private JFXButton startServerBtn;

    @FXML
    private JFXButton stopServerBtn;

    @FXML
    private TextArea consoleView;

    private final PrintStream stdOut = System.out;

    private final PrintStream stdErr = System.err;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        consoleView.setEditable(false);
        Console c = new Console(consoleView);
        PrintStream p = new PrintStream(c);

        System.setOut(p);
        System.setErr(p);

        playerIdColumn.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().getId()));
        playerUsernameColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getUsername()));

        matchIdColumn.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().Id));
        matchNameColumn.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().Name));
        matchGameMapColumn.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().GameMap));
        matchPlayersColumn.setCellValueFactory(data -> {
            final SimpleIntegerProperty size = new SimpleIntegerProperty();
            size.bind(Bindings.size(data.getValue().getPlayers()));

            return size.asObject();
        });
    }

    public void initGameController(BiConsumer<ObservableList<Player>, ObservableList<Match>> Initializer, Runnable Terminator) {
        startServerBtn.setOnMouseClicked(event -> Initializer.accept(playersTable.getItems(), matchesTable.getItems()));
        stopServerBtn.setOnMouseClicked(event -> Terminator.run());
    }

    public void resetStdOut() {
        System.setOut(stdOut);
        System.setErr(stdErr);
    }

    private static class Console extends OutputStream {
        private final TextArea output;

        public Console(TextArea Output) {
            this.output = Output;
        }

        @Override
        public void write(byte[] buffer, int offset, int length) throws IOException {
            final String text = new String (buffer, offset, length);
            Platform.runLater(() -> output.appendText(text));
        }

        @Override
        public void write(int b) throws IOException {
            write (new byte [] {(byte)b}, 0, 1);
        }
    }
}