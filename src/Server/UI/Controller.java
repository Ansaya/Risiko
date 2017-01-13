package Server.UI;

import Game.Map.Maps;
import Server.Game.GameController;
import Server.Game.Match;
import Server.Game.Player;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.URL;
import java.util.ResourceBundle;

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
    private TableColumn<Match, Integer> matchNameColumn;

    @FXML
    private TableColumn<Match, Maps> matchGameMapColumn;

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
        matchNameColumn.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().getPlayers().size()));
        matchGameMapColumn.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().GameMap));
    }

    public void initGameController() {
        GameController.getInstance().init(playersTable.getItems(), matchesTable.getItems());
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