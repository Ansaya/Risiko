package Client.UI.ChatBox;

import Client.Game.Player;
import Client.Main;
import Game.Connection.Chat;
import Game.Sounds.Sounds;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXTextField;
import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TitledPane;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

import java.util.function.Consumer;

/**
 * Created by fiore on 10/01/2017.
 */
public class ChatBox extends TitledPane {

    private final VBox chatContainer;

    private final ScrollPane chatView;

    private final JFXTextField chatInput;

    private final JFXButton chatSendBtn;

    private volatile int lastSender = -1;

    private final int userId;

    private volatile boolean scrollToEnd = false;

    public ChatBox(int UserId, Consumer<String> SendChat) {
        this.setText("Chat");
        this.setFont(Main.globalFont);

        final VBox vb = new VBox(chatView = new ScrollPane(chatContainer = new VBox()),
                new HBox(chatInput = new JFXTextField(), chatSendBtn = new JFXButton("Send")));
        vb.setSpacing(10);
        this.setContent(vb);

        this.getStyleClass().add("chatBox");
        this.getStylesheets().add(ChatBox.class.getResource("chat.css").toExternalForm());
        chatView.getStyleClass().add("chatView");
        chatInput.getStyleClass().add("chatInput");
        chatInput.setFont(Main.globalFont);
        chatSendBtn.getStyleClass().add("chatSendBtn");
        chatSendBtn.setFont(Main.globalFont);
        chatSendBtn.setButtonType(JFXButton.ButtonType.RAISED);
        chatSendBtn.setRipplerFill(Color.web("#BD6300"));

        this.userId = UserId;

        final EventHandler sendMessage = (evt) -> {
            if(!chatInput.getText().trim().equals(""))
                SendChat.accept(chatInput.getText().trim());
            chatInput.clear();
        };

        chatInput.setOnAction(sendMessage);
        chatSendBtn.addEventFilter(MouseEvent.MOUSE_CLICKED, sendMessage);

        chatContainer.getChildren().addListener((ListChangeListener<Node>) c -> Sounds.Chat.play());
        chatView.vvalueProperty().addListener((ob, oldV, newV) -> {
            if(!scrollToEnd) return;

            scrollToEnd = false;
            chatView.setVvalue(1.0);
        });
    }

    public void addChat(Chat<Player> Message) {
        final Label sender = getChatEntry(Message.sender.Username.get()), message = getChatEntry(Message.message);

        if(Message.sender.getId() == userId){
            sender.setAlignment(Pos.CENTER_RIGHT);
            message.setAlignment(Pos.CENTER_RIGHT);
        }

        if(Message.sender.getColor() != null){
            sender.setTextFill(Message.sender.Color.hexColor);
            message.setTextFill(Message.sender.Color.hexColor);
        }

        if(Platform.isFxApplicationThread()){
            if(Message.sender.getId() != lastSender)
                chatContainer.getChildren().add(sender);

            lastSender = Message.sender.getId();

            scrollToEnd = true;
            chatContainer.getChildren().add(message);
            chatView.setVvalue(1.0);
            return;
        }

        Platform.runLater(() -> {
            if(Message.sender.getId() != lastSender)
                chatContainer.getChildren().add(sender);

            lastSender = Message.sender.getId();

            scrollToEnd = true;
            chatContainer.getChildren().add(message);
            chatView.setVvalue(1.0);
        });
    }

    private Label getChatEntry(String Text) {
        final Label chatEntry = new Label(Text);
        chatEntry.getStyleClass().add("chat");
        chatEntry.setWrapText(true);

        return chatEntry;
    }
}
