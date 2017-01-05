package Client.Game.Observables;

import Client.Main;
import Game.Connection.Cards;
import Game.Map.Card;
import Game.Sounds.Sounds;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXDialog;
import com.jfoenix.controls.JFXDialogLayout;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.DialogEvent;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import java.util.ArrayList;

/**
 * Handler for cards in UI
 */
public class CardsHandler {

    /**
     * Dialog containing cards UI objects
     */
    private final JFXDialog cardsDialog = new JFXDialog();

    public void setCardsButton(Button CardsBtn) {
        CardsBtn.addEventFilter(MouseEvent.MOUSE_CLICKED, evt -> Main.showDialog(cardsDialog));
    }

    /**
     * Cards container
     */
    private final HBox container = new HBox();

    /**
     * Redeem button in dialog
     */
    private final JFXButton redeemBtn = new JFXButton("Redeem");

    /**
     * List of selected cards
     */
    private final ArrayList<Card> selected = new ArrayList<>();

    public CardsHandler() {
        /* Container setup */
        container.setSpacing(15.0f);
        container.setPadding(new Insets(0, 7.5, 0, 22.5));
        container.setAlignment(Pos.CENTER);
        container.setPrefHeight(227.0f);

        /* Close button setup */
        final JFXButton closeBtn = new JFXButton("Close");
        closeBtn.setButtonType(JFXButton.ButtonType.RAISED);
        closeBtn.setStyle("-fx-background-color: #44B449");
        closeBtn.addEventFilter(MouseEvent.MOUSE_CLICKED, (e) -> {
            synchronized (selected){
                selected.notify();
            }
            cardsDialog.close();
        });

        /* Redeem cards button setup */
        redeemBtn.setButtonType(JFXButton.ButtonType.RAISED);
        redeemBtn.setStyle("-fx-background-color: red");
        redeemBtn.addEventFilter(MouseEvent.MOUSE_CLICKED, evt -> cardsDialog.close());
        redeemBtn.setDisable(true);

        /* Layout setup */
        final JFXDialogLayout dl = new JFXDialogLayout();
        dl.setHeading(new Label("     Territories cards"));
        dl.setBody(container);
        dl.setActions(new HBox(15, redeemBtn, closeBtn));

        cardsDialog.setContent(dl);
        cardsDialog.addEventFilter(DialogEvent.DIALOG_CLOSE_REQUEST, evt -> {
            synchronized (selected){
                selected.notify();
            }
        });
    }

    /**
     * Initialize card object
     *
     * @param Card Card to initialize
     * @return Initialized card
     */
    private ImageView getCard(Card Card) {
        final ImageView card = new ImageView(Card.getImage());
        card.setPreserveRatio(true);
        card.setSmooth(true);
        card.setCache(true);
        card.setX(137.0f);
        card.setY(212.0f);

        card.addEventFilter(MouseEvent.MOUSE_CLICKED, evt -> {
            Node source = (Node) evt.getSource();

            if(!source.getStyleClass().remove("card-selected")) {
                source.getStyleClass().add("card-selected");
                selected.add(Card);
            }
            else {
                selected.remove(Card);
            }
        });

        return card;
    }

    /**
     * Ask the user To play a combination
     *
     * @return Message containing combination played From the user
     */
    public Cards requestCombination() {
        // If cards needed for combination are not present return empty list
        if(container.getChildren().size() < 3)
            return new Cards();

        selected.clear();

        Platform.runLater(() -> {
            redeemBtn.setDisable(false);
            Main.showDialog(cardsDialog);
        });

        synchronized (selected) {
            try {
                selected.wait();
            } catch (Exception e) {}
        }

        // If no cards are selected and less than five cards are present, return empty message
        if(selected.isEmpty() && container.getChildren().size() < 5)
            return new Cards();

        // If combination is valid return initialized Cards message
        if(Card.isCombinationValid(selected)) {
            Sounds.CardTris.play();
            return new Cards(selected);
        }

        // Else show error dialog and retry
        Main.showDialog("Error message", "Combination are of three cards only:\n" +
                                                      " - three infantry, cavalry or artillery\n" +
                                                      " - two infantry, cavalry, or artillery and one jolly\n" +
                                                      " - one infantry, one cavalry and one artillery\n" +
                                                      "If you have five cards you must select a combination To redeem.",
                         "Continue");

        synchronized (Main.dialogClosed){
            try {
                Main.dialogClosed.wait();
            } catch (Exception e) {}

        }

        return requestCombination();
    }

    /**
     * Add new card To the list
     *
     * @param Card Card to add
     */
    public void addCard(Card Card) {
        if(!Platform.isFxApplicationThread()) {
            Platform.runLater(() -> addCard(Card));
            return;
        }

        container.getChildren().add(getCard(Card));
    }
}
