package Client;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXDialog;
import com.jfoenix.controls.JFXDialogLayout;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.io.IOException;

public class Main extends Application {

    private static Stage window;

    public static void toMatch() {
        FXMLLoader loader = new FXMLLoader();
        Parent root = null;
        try {
            root = (Parent) loader.load(Main.class.getResource("match.fxml").openStream());
            root.getStylesheets().add(Main.class.getResource("map.css").toExternalForm());
            root.getStylesheets().add(Main.class.getResource("chat.css").toExternalForm());
        }catch (IOException e) {
            e.printStackTrace();
        }

        MatchController newMatch = loader.getController();
        newMatch.setParent((StackPane) root);

        window.setTitle("Risiko - Match");
        window.setScene(new Scene(root, window.getWidth(), window.getHeight()));
        window.show();
    };

    public static void toLobby() {
        FXMLLoader loader = new FXMLLoader();
        Parent root = null;
        try {
            root = (Parent) loader.load(Main.class.getResource("lobby.fxml").openStream());
            root.getStylesheets().add(Main.class.getResource("chat.css").toExternalForm());
        }catch (IOException e) {
            e.printStackTrace();
        }

        LobbyController lobby = loader.getController();
        lobby.setParent((StackPane) root);

        window.setTitle("Risiko - Lobby");
        window.setResizable(true);
        window.setX(window.getX() - 538.0);
        window.setY(window.getY() - 125.0);
        window.setMinWidth(1067.0);
        window.setMinHeight(600.0);
        window.setScene(new Scene(root, 1366, 768));
        window.show();
    };

    public static void toLogin() {
        FXMLLoader loader = new FXMLLoader();
        Parent root = null;
        try {
            root = (Parent) loader.load(Main.class.getResource("login.fxml").openStream());
        }catch (IOException e) {
            e.printStackTrace();
        }

        LoginController login = loader.getController();
        login.setParent((StackPane) root);

        window.setTitle("Risiko - Login");
        window.setWidth(250.0);
        window.setHeight(300.0);
        window.setMinWidth(250.0);
        window.setMinHeight(300.0);
        window.setScene(new Scene(root, 250, 300));
        window.setResizable(false);
        window.show();
    };

    public static JFXDialog getDialog(String Heading, String Body, String BtnText) {
        JFXDialog dialog = new JFXDialog();

        JFXDialogLayout layout = new JFXDialogLayout();
        layout.setHeading(new Label(Heading));
        layout.setBody(new Label(Body));
        if(BtnText != null){
            JFXButton btn = new JFXButton(BtnText);
            btn.setButtonType(JFXButton.ButtonType.RAISED);
            btn.setStyle("-fx-background-color: #44B449");
            btn.addEventFilter(MouseEvent.MOUSE_CLICKED, (e) -> dialog.close());
            layout.setActions(btn);
        }
        dialog.setContent(layout);

        return dialog;
    }

    @Override
    public void start(Stage primaryStage) throws Exception{
        this.window = primaryStage;

        toLogin();
    }


    public static void main(String[] args) {

        launch(args);
    }

    @Override
    public void stop() throws Exception {
        ServerTalk.getInstance().StopConnection(true);

        super.stop();
    }
}
