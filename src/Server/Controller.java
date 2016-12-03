package Server;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextArea;
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
    private TreeTableView matchesTable;

    @FXML
    private TextArea consoleView;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        consoleView.setEditable(false);
        Console c = new Console(consoleView);

        System.setOut(new PrintStream(c));
    }

    private static class Console extends OutputStream {
        private final TextArea output;

        public Console(TextArea Output) {
            this.output = Output;
        }

        @Override
        public void write(byte[] buffer, int offset, int length) throws IOException
        {
            final String text = new String (buffer, offset, length);
            Platform.runLater(() -> output.appendText(text));
        }

        @Override
        public void write(int b) throws IOException
        {
            write (new byte [] {(byte)b}, 0, 1);
        }
    }
}