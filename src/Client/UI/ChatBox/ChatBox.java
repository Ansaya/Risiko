package Client.UI.ChatBox;

import Client.Game.GameController;
import Client.Game.Observables.ObservableUser;
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

    public ChatBox(int UserId) {
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

        EventHandler sendMessage = (evt) -> {
            if(!chatInput.getText().trim().equals(""))
                GameController.getInstance().SendChat(chatInput.getText().trim());
            chatInput.clear();
        };

        chatInput.setOnAction(sendMessage);
        chatSendBtn.addEventFilter(MouseEvent.MOUSE_CLICKED, sendMessage);

        chatContainer.getChildren().addListener(new ListChangeListener<Node>() {
            @Override
            public void onChanged(Change<? extends Node> c) {
                chatView.setVvalue(1);
                Sounds.Chat.play();
            }
        });
    }

    public void addChat(Chat<ObservableUser> Message) {
        final Label sender = getChatEntry(Message.sender.username.get()), message = getChatEntry(Message.message);

        if(Message.sender.getId() == userId){
            sender.setAlignment(Pos.CENTER_RIGHT);
            message.setAlignment(Pos.CENTER_RIGHT);
        }

        if(Message.sender.getColor() != null){
            sender.setTextFill(Message.sender.color.hexColor);
            message.setTextFill(Message.sender.color.hexColor);
        }

        if(Platform.isFxApplicationThread()){
            if(Message.sender.getId() != lastSender)
                chatContainer.getChildren().add(sender);

            lastSender = Message.sender.getId();

            chatContainer.getChildren().add(message);
            return;
        }

        Platform.runLater(() -> {
            if(Message.sender.getId() != lastSender)
                chatContainer.getChildren().add(sender);

            lastSender = Message.sender.getId();

            chatContainer.getChildren().add(message);
        });
    }

    private Label getChatEntry(String Text) {
        final Label chatEntry = new Label(Text);
        chatEntry.getStyleClass().add("chat");
        chatEntry.setWrapText(true);

        return chatEntry;
    }
}
