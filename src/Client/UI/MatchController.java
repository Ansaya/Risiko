package Client.UI;

import Client.Main;
import Client.Game.Observables.*;
import Client.Game.ServerTalk;
import Game.Connection.*;
import Client.Game.Connection.MessageType;
import Game.Map.Territories;
import com.jfoenix.controls.*;
import com.jfoenix.controls.datamodels.treetable.RecursiveTreeObject;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
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
            server.SendMessage(MessageType.Chat, new Chat<>(server.getUser(), chatMessage.getText().trim()));

        chatMessage.clear();
    };

    /* Map */
    @FXML
    protected AnchorPane worldMap;

    @FXML
    protected Pane mapPane;

    @FXML
    protected JFXTreeTableView<ObservableUser> playersList;

    private volatile MapHandler mapHandler;

    /* Game */
    @FXML
    protected JFXButton endTurnBtn;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        /* Chat setup */
        this.server.setChatUpdate(chatSP, chatContainer);
        chatSendBtn.addEventHandler(MouseEvent.MOUSE_CLICKED, sendMessage);
        chatMessage.setOnAction(sendMessage);

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

        /* Map handler setup */
        ArrayList<SVGPath> svgPaths = new ArrayList<>();
        ArrayList<Label> labels = new ArrayList<>();

        // Retrieve territories and labels from map view
        mapPane.getChildren().forEach((c) -> {
            if(c instanceof Label) {
                labels.add((Label) c);
            }

            if(c instanceof SVGPath) {
                svgPaths.add((SVGPath) c);
            }
        });

        HashMap<Territories, ObservableTerritory> map = new HashMap<>();

        // Bind territories and labels, then put them in HashMap
        svgPaths.forEach((svg) -> {
            Label l = null;

            for (Label lb: labels) {
                if(lb.getId().contains(svg.getId())) {
                    l = lb;
                    labels.remove(lb);
                    break;
                }
            }

            Territories t = Territories.valueOf(svg.getId());

            map.put(t, new ObservableTerritory(t, svg, l));
        });
        mapHandler = new MapHandler(mapPane, map);


        /* Players table setup */
        JFXTreeTableColumn<ObservableUser, Integer> idColumn = new JFXTreeTableColumn<>("ID");
        idColumn.setCellValueFactory((TreeTableColumn.CellDataFeatures<ObservableUser, Integer> param) -> {
            if(idColumn.validateValue(param)) return param.getValue().getValue().id.asObject();
            else return idColumn.getComputedValue(param);
        });

        JFXTreeTableColumn<ObservableUser, String> usernameColumn = new JFXTreeTableColumn<>("Username");
        usernameColumn.setCellValueFactory((TreeTableColumn.CellDataFeatures<ObservableUser, String> param) -> {
            if(usernameColumn.validateValue(param)) return param.getValue().getValue().username;
            else return usernameColumn.getComputedValue(param);
        });

        JFXTreeTableColumn<ObservableUser, Integer> territoriesColumn = new JFXTreeTableColumn<>("Territories");
        territoriesColumn.setCellValueFactory((TreeTableColumn.CellDataFeatures<ObservableUser, Integer> param) -> {
            if(territoriesColumn.validateValue(param)) return param.getValue().getValue().territories.asObject();
            else return territoriesColumn.getComputedValue(param);
        });

        final RecursiveTreeItem<ObservableUser> rootItem = new RecursiveTreeItem<ObservableUser>(FXCollections.observableArrayList(), RecursiveTreeObject::getChildren);
        playersList.getColumns().setAll(idColumn, usernameColumn, territoriesColumn);
        playersList.setRoot(rootItem);
        playersList.setShowRoot(false);

        /* End phase button setup */
        endTurnBtn.addEventFilter(MouseEvent.MOUSE_CLICKED, (e) -> {
            Main.showDialog("Heading text", "Tua mamma troia", "Avanti");
        });

        // Update server talk objects
        server.setUsersUpdate(rootItem.getChildren());
        server.setMapUpdate(mapHandler);
    }
}
