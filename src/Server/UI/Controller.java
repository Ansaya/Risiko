package Server.UI;

import Game.Map.Army.Color;
import Server.Game.GameController;
import Server.Game.Match;
import Server.Game.Player;
import javafx.application.Platform;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextArea;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.URL;
import java.util.ResourceBundle;

public class Controller implements Initializable {

    @FXML
    private TreeTableView playersTable;

    @FXML
    private TreeTableColumn<Player, Integer> playerIdColumn;

    @FXML
    private TreeTableColumn<Player, String> playerUsernameColumn;

    @FXML
    private TreeTableView matchesTable;

    @FXML
    protected TreeTableColumn<Match, Integer> matchIdColumn;

    @FXML
    private TreeTableColumn<Match, Integer> matchUsersColumn;

    @FXML
    private TreeTableColumn<Match, String> matchGameMapColumn;

    @FXML
    private TextArea consoleView;

    private final PrintStream stdOut = System.out;

    private final PrintStream stdErr = System.err;

    private final TreeItem<Match> matches = new TreeItem<>(new Match());

    private final TreeItem<Player> players = new TreeItem<>(Player.getAI(-1, Color.RED));

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        consoleView.setEditable(false);
        Console c = new Console(consoleView);
        PrintStream p = new PrintStream(c);

        System.setOut(p);
        System.setErr(p);

        playerIdColumn.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getValue().id).asObject());
        playerUsernameColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getValue().username));
        playersTable.setRoot(players);

        matchIdColumn.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getValue().id).asObject());
        matchUsersColumn.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getValue().getPlayers().size()).asObject());
        matchGameMapColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getValue().GameMap));
        matchesTable.setRoot(matches);
    }

    public void initGameController() {
        GameController.getInstance().init(players, matches);
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