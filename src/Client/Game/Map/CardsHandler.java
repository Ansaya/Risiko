package Client.Game.Map;

import Client.Main;
import Game.Connection.Cards;
import Game.Logger;
import Game.Map.Card;
import Game.Map.Figure;
import Game.Sounds.Sounds;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXDialog;
import com.jfoenix.controls.JFXDialogLayout;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.BlurType;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Handler for cards in UI
 */
public class CardsHandler {

    private final ResourceBundle resources;

    /**
     * Dialog containing cards UI objects
     */
    private final JFXDialog cardsDialog = new JFXDialog();

    public void setCardsButton(Button CardsBtn) {
        CardsBtn.setOnMouseClicked(evt -> Main.showDialog(cardsDialog));
    }

    /**
     * Cards container
     */
    private final HBox container = new HBox();

    private final ArrayList<Card> cards = new ArrayList<>();

    /**
     * Redeem button in dialog
     */
    private final JFXButton redeemBtn;

    /**
     * List of selected cards
     */
    private final ArrayList<Card> selected = new ArrayList<>();

    private final AtomicBoolean isWaiting = new AtomicBoolean(false);

    public CardsHandler(ResourceBundle Resources) {
        this.resources = Resources;

        /* Container setup */
        container.setSpacing(15.0f);
        container.setPadding(new Insets(0, 7.5, 0, 22.5));
        container.setAlignment(Pos.CENTER);
        container.setPrefHeight(227.0f);

        /* Close button setup */
        final JFXButton closeBtn = new JFXButton(resources.getString("close"));
        closeBtn.setButtonType(JFXButton.ButtonType.RAISED);
        closeBtn.setStyle("-fx-background-color: #44B449");
        closeBtn.setOnMouseClicked(event -> cardsDialog.close());

        /* Redeem cards button setup */
        redeemBtn = new JFXButton(resources.getString("redeem"));
        redeemBtn.setButtonType(JFXButton.ButtonType.RAISED);
        redeemBtn.setStyle("-fx-background-color: red");
        redeemBtn.setOnMouseClicked(event -> cardsDialog.close());
        redeemBtn.setDisable(true);

        /* Layout setup */
        final JFXDialogLayout dl = new JFXDialogLayout();
        dl.setHeading(new Label("     " + resources.getString("cardsTitle")));
        dl.setBody(container);
        dl.setActions(new HBox(15, redeemBtn, closeBtn));

        cardsDialog.setContent(dl);
        cardsDialog.setOnDialogClosed(event -> {
            if(!isWaiting.get()) return;

            synchronized (selected) {
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
    private StackPane loadCard(Card Card) {
        final ImageView cardImage = new ImageView(Card.getImage(resources.getLocale()));
        cardImage.setMouseTransparent(true);

        final Label cardLabel = new Label(Card.Figure != Figure.Jolly ? Card.Name : "");
        cardLabel.setEffect(new DropShadow(BlurType.GAUSSIAN, Color.BLACK, 1.5, 1.0, 0, 0));
        cardLabel.setAlignment(Pos.CENTER);
        cardLabel.setStyle("-fx-font-size: 18px;-fx-text-alignment: center center");
        cardLabel.setMouseTransparent(true);
        cardLabel.setFont(Main.globalFont);
        cardLabel.setTextFill(Color.WHITE);
        cardLabel.setWrapText(true);

        final StackPane card = new StackPane(cardImage, cardLabel);
        card.setPrefSize(137.0f, 212.0f);
        card.setAlignment(Pos.CENTER);
        card.setOnMouseClicked(evt -> {
            Node source = (Node) evt.getSource();

            if(!source.getStyleClass().remove("card-selected")) {
                source.getStyleClass().add("card-selected");
                selected.add(Card);
            }
            else
                selected.remove(Card);
        });

        return card;
    }

    /**
     * Ask the user to play a combination
     *
     * @return Message containing combination played from the user
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

        isWaiting.set(true);
        System.out.println("Cards handler: Waiting for user");
        try {
            synchronized (selected) {
                selected.wait();
            }
        } catch (InterruptedException e) {
            Logger.err("Cards handler: Error waiting for cards dialog", e);
        }
        isWaiting.set(false);
        System.out.println("Cards handler: Checking selection");

        // If no cards are selected and less than five cards are present, return empty message
        if(selected.isEmpty() && container.getChildren().size() < 5)
            return new Cards();

        // If combination is valid return initialized Cards message
        if(Card.isCombinationValid(selected)) {
            Sounds.CardTris.play();
            cards.removeAll(selected);
            return new Cards(selected);
        }

        // Else show error dialog and retry
        Main.showDialog(resources.getString("cardsErrorTitle"),
                resources.getString("cardsErrorMessage"),
                resources.getString("continue"));

        return requestCombination();
    }

    /**
     * Add new card to the list
     *
     * @param Card Card to add
     */
    public void addCard(Card Card) {
        System.out.println("Cards handler: Adding card " + Card.Id);
        if(!Platform.isFxApplicationThread()) {
            Platform.runLater(() -> addCard(Card));
            return;
        }

        container.getChildren().add(loadCard(Card));
        cards.add(Card);

        // Notify user
        Main.showDialog(resources.getString("cardsTitle"),
                String.format(resources.getString("cardsMessage"), Card.toString()),
                resources.getString("continue"));
    }

    /**
     * Remove all remaining cards from player's hand
     *
     * @return Remaining cards
     */
    public ArrayList<Card> returnCards() {
        final ArrayList<Card> remaining = new ArrayList<>(cards);
        container.getChildren().clear();
        cards.clear();
        return remaining;
    }
}
