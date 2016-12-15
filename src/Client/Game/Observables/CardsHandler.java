package Client.Game.Observables;

import Client.Main;
import Client.UI.MatchController;
import Game.Connection.Cards;
import Game.Map.Card;
import Game.Map.Territories;
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
    private final ArrayList<Territories> selected = new ArrayList<>();

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
        redeemBtn.addEventFilter(MouseEvent.MOUSE_CLICKED, evt -> {
            redeemBtn.setDisable(true);
            container.getChildren().forEach(card -> {
                if(card.getStyleClass().remove("card-selected"))
                    selected.add(Territories.valueOf(card.getId()));
            });

            synchronized (selected){
                selected.notify();
            }
            cardsDialog.close();
        });
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
     * @param Territory Territory To use for card initialization
     * @return Initialized card
     */
    private AnchorPane getCard(Territories Territory) {

        final String img = MatchController.class.getResource("Cards/" + Territory.name() + ".jpg").toExternalForm();
        final AnchorPane card = new AnchorPane();
        card.setPrefSize(137.0f, 212.0f);
        card.setStyle("-fx-background-image: url('" + img + "');" +
                "-fx-background-position: center center;" +
                "-fx-background-repeat: no-repeat;" +
                "-fx-background-size: 137 212;");

        card.setId(Territory.name());

        card.addEventFilter(MouseEvent.MOUSE_CLICKED, evt -> {
            Node source = (Node) evt.getSource();

            if(!source.getStyleClass().remove("card-selected"))
                source.getStyleClass().add("card-selected");
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
        if(Card.isCombinationValid(selected))
            return new Cards(selected);

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
     * @param Territory Territory relative To the card To add
     */
    public void addCard(Territories Territory) {
        if(!Platform.isFxApplicationThread()) {
            Platform.runLater(() -> addCard(Territory));
            return;
        }

        container.getChildren().add(getCard(Territory));
    }
}
