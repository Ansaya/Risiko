package Client;

import Client.Observables.ObservableTerritory;
import Client.Observables.ObservableUser;
import Game.Connection.Chat;
import Game.Connection.MessageType;
import Game.Map.Territories;
import com.jfoenix.controls.*;
import com.jfoenix.controls.datamodels.treetable.RecursiveTreeObject;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.SVGPath;
import javafx.scene.transform.Scale;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.ResourceBundle;

/**
 * Match view controller
 */
public class MatchController implements Initializable {

    private StackPane parent;

    public void setParent(StackPane Parent) { this.parent = Parent; }

    private double mapRatio = 725.0f / 480.0f;

    private ServerTalk server = ServerTalk.getInstance();

    /* Chat fields */
    @FXML
    protected ScrollPane chatSP;

    @FXML
    protected VBox chatContainer;

    @FXML
    protected TextField chatMessage;

    @FXML
    protected Button chatSendBtn;

    @FXML
    protected JFXBadge chatBadge;

    /**
     * Lambda for chat message sending
     */
    private EventHandler sendMessage = (evt) -> {
        if(!chatMessage.getText().trim().equals(""))
            server.SendMessage(MessageType.Chat, new Chat(server.getUser(), chatMessage.getText().trim()));

        chatMessage.clear();
    };

    /* Map */
    @FXML
    protected AnchorPane worldMap;

    @FXML
    protected Pane mapPane;

    @FXML
    protected JFXTreeTableView<ObservableUser> playersList;

    private HashMap<Territories, ObservableTerritory> map = new HashMap<>();

    /* Game */
    @FXML
    protected JFXButton endTurnBtn;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        /* Chat setup */
        chatSendBtn.addEventHandler(MouseEvent.MOUSE_CLICKED, sendMessage);
        chatMessage.setOnAction(sendMessage);

        // Set chat updatable fields
        this.server.setChatUpdate(chatSP, chatContainer);


        /* Map rescaling */
        worldMap.widthProperty().addListener((ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
            double containerRatio = (double) newValue / worldMap.getHeight();

            if(containerRatio < mapRatio){
                Scale newScale = new Scale();
                double scaleValue = (double) newValue / 725.0f;
                newScale.setPivotX(0);
                newScale.setPivotY(0);
                newScale.setX(scaleValue);
                newScale.setY(scaleValue);

                mapPane.getTransforms().clear();
                mapPane.getTransforms().add(newScale);

                AnchorPane.setLeftAnchor(mapPane, -180.0f * scaleValue);
                AnchorPane.setTopAnchor(mapPane, -130.0f * scaleValue);
            }

        });

        worldMap.heightProperty().addListener((ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
            double containerRatio = worldMap.getWidth() / (double) newValue;

            if(containerRatio > mapRatio) {
                Scale newScale = new Scale();
                double scaleValue = (double) newValue / 480.0f;
                newScale.setPivotX(0);
                newScale.setPivotY(0);
                newScale.setX(scaleValue);
                newScale.setY(scaleValue);

                mapPane.getTransforms().clear();
                mapPane.getTransforms().add(newScale);

                AnchorPane.setLeftAnchor(mapPane, -180.0f * scaleValue);
                AnchorPane.setTopAnchor(mapPane, -130.0f * scaleValue);
            }
        });

        /* Map territories setup */
        ArrayList<SVGPath> svgPaths = new ArrayList<>();
        ArrayList<Label> labels = new ArrayList<>();

        mapPane.getChildren().forEach((c) -> {
            if(c instanceof Label) {
                labels.add((Label) c);
            }

            if(c instanceof SVGPath) {
                svgPaths.add((SVGPath) c);
            }
        });

        svgPaths.forEach((svg) -> {
            Label l = null;

            for (Label lb: labels) {
                if(lb.getId().contains(svg.getId())) {
                    l = lb;
                    labels.remove(lb);
                    break;
                }
            }

            map.put(Territories.valueOf(svg.getId()), new ObservableTerritory(svg, l));
        });
        ObservableTerritory.setMapPane(mapPane);
        server.setMapUpdate(map);


        /* Players table setup */
        JFXTreeTableColumn<ObservableUser, Integer> idColumn = new JFXTreeTableColumn<>("ID");
        idColumn.setCellValueFactory((TreeTableColumn.CellDataFeatures<ObservableUser, Integer> param) -> {
            if(idColumn.validateValue(param)) return param.getValue().getValue().UserId.asObject();
            else return idColumn.getComputedValue(param);
        });

        JFXTreeTableColumn<ObservableUser, String> usernameColumn = new JFXTreeTableColumn<>("Username");
        usernameColumn.setCellValueFactory((TreeTableColumn.CellDataFeatures<ObservableUser, String> param) -> {
            if(usernameColumn.validateValue(param)) return param.getValue().getValue().Username;
            else return usernameColumn.getComputedValue(param);
        });

        JFXTreeTableColumn<ObservableUser, Integer> territoriesColumn = new JFXTreeTableColumn<>("Territories");
        territoriesColumn.setCellValueFactory((TreeTableColumn.CellDataFeatures<ObservableUser, Integer> param) -> {
            if(territoriesColumn.validateValue(param)) return param.getValue().getValue().Territories.asObject();
            else return territoriesColumn.getComputedValue(param);
        });

        final RecursiveTreeItem<ObservableUser> rootItem = new RecursiveTreeItem<ObservableUser>(FXCollections.observableArrayList(), RecursiveTreeObject::getChildren);
        playersList.getColumns().setAll(idColumn, usernameColumn, territoriesColumn);
        playersList.setRoot(rootItem);
        playersList.setShowRoot(false);
        server.setUsersUpdate(rootItem.getChildren());

        endTurnBtn.addEventFilter(MouseEvent.MOUSE_CLICKED, (e) -> {
            JFXDialog popup = Main.getDialog("Heading text", "Tua mamma troia", "Avanti");
            popup.show(parent);
        });

    }
}
