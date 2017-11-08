package FX;

import FX.elements.JFxFakeBuilding;
import FX.elements.JFxNode;
import Net.IWirelessNetworkDelegate;
import Net.WirelessNetwork;
import Net.WirelessNode;
import UART.UARTConnector;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;

import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.text.Text;
import jssc.SerialPortList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.*;
import java.util.List;

/**
 * Created by Jiro on 23.01.17.
 */
public class JFxMainUIController implements Initializable, IWirelessNetworkDelegate {
    private static final Logger log = LoggerFactory.getLogger(JFxMainUIController.class);

    @FXML
    private ChoiceBox<String> uartDevices;
    @FXML
    private Button connectButton;
    @FXML
    private Button findNodes;
    @FXML
    private Label netLog;

    @FXML
    private TableColumn column_address;
    @FXML
    private TableColumn column_rssi;
    @FXML
    private TableColumn column_stats;

    @FXML
    private Pane network_ui;

    @FXML
    private TableView<WirelessNode> NodesTable;

    private UARTConnector uartConnector;
    private boolean isConfAlertShown = false;

    private List<Thread> threads = new ArrayList<>();
    private boolean appWorking = true;

    private WirelessNetwork network = null;
    private WirelessNode configurationNode = null;

    void setUartConnector(UARTConnector uartConnector) {
        this.uartConnector = uartConnector;
        uartConnector.setDelegate(network);
    }

    public void stop(){
        appWorking = false;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        network = new WirelessNetwork();
        network.setDelegate(this);

        column_address.setCellValueFactory(new PropertyValueFactory<WirelessNode,Integer>("address"));
        column_rssi.setCellValueFactory(new PropertyValueFactory<WirelessNode,Integer>("dB"));
        column_stats.setCellValueFactory(new PropertyValueFactory<WirelessNode,String>("state"));

        NodesTable.setItems(network.getNodes());

        connectButton.setOnMouseClicked(event -> {
            switch (event.getButton()) {
                case PRIMARY: {
//                    redrawNetUI();
                    if (connectButton.getText().equals("connect")) {
                        String s = uartDevices.getSelectionModel().getSelectedItem();

                        uartConnector.Init(s);
                        if (uartConnector.Connect()) {
                            uartDevices.setDisable(true);
                            findNodes.setDisable(false);
                            connectButton.setText("disconnect");
                        }
                    } else if (uartConnector.IsConnected()) {
                        if (uartConnector.Disconnect()) {
                            uartDevices.setDisable(false);
                            findNodes.setDisable(true);
                            connectButton.setText("connect");
                        }
                    }
                    break;
                }
            }
        });

        threads.add(new Thread(() -> {
            while (!Thread.currentThread().isInterrupted() && appWorking) {
                try {
                    network.dropNet();
                    if(!uartConnector.IsConnected()){
                        String[] portNames = SerialPortList.getPortNames();
                        if(portNames.length != uartDevices.getItems().size()){
                            Platform.runLater(() -> {
                                if (portNames.length > 0) {
                                    uartDevices.setItems(FXCollections.observableArrayList(portNames));
                                }
                            });
                        }
                    }
                    Thread.currentThread().sleep(3000);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                } catch (Exception e){

                }
            }
        }));

        threads.get(threads.size() - 1).start();
    }

    private void showConfAlert() {
        if(!isConfAlertShown){
            isConfAlertShown = true;

            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    TextInputDialog dialog = new TextInputDialog("");
                    dialog.setTitle("Node Address Configuration");
                    dialog.setContentText("Please Enter Node Address less 65535");

                    Optional<String> result = dialog.showAndWait();
                    if (result.isPresent()) {
                        isConfAlertShown = false;
                        System.out.println("configured");
                        int address = Integer.valueOf(result.get());
                        if (address >= 4 && address < 0xffff) {
                            configurationNode.setAddress(address);
                            uartConnector.SetNodeNewAddress(address);
                        }
                    } else {
                        isConfAlertShown = false;
                        System.out.println("canceled");
                    }
                }
            });
        }
    }

    private void redrawNetUI(){
        if(network_ui.getChildren().size() == 0){
            Group arrows = new Group();
//            arrows.setPrefSize(network_ui.getWidth(),network_ui.getHeight());

            Group circles = new Group();
//            circles.setPrefSize(network_ui.getWidth(),network_ui.getHeight());

            Group titles = new Group();
            JFxFakeBuilding building  = new JFxFakeBuilding(network_ui.getWidth(),network_ui.getHeight());

            network_ui.getChildren().addAll(arrows,circles,titles,building);
        }

        if (network_ui != null){
            double centerX = network_ui.getWidth() / 2;
            double centerY = network_ui.getHeight() / 2;

            ((Group)network_ui.getChildren().get(2)).getChildren().clear();
            ((Group)network_ui.getChildren().get(1)).getChildren().clear();

            for (WirelessNode node : network.getNodes()){
                Node finded = null;
                for (Node ui_node: ((Group)network_ui.getChildren().get(1)).getChildren()){
                    if(ui_node.getId().equals(String.valueOf(node.getAddress()))) {
                        finded = ui_node;
                        break;
                    }
                }

                if(finded == null){
                    JFxNode circle = null;
                    if (node.getState().equals("Server")){
                        circle = new JFxNode(centerX, centerY,20,Color.BURLYWOOD);
                    } else {
                        int xR = (int)((Math.random() - 1.0) * network_ui.getWidth() / 2);
                        int yR = (int)((Math.random() - 1.0) * network_ui.getHeight() / 2);
                        circle = new JFxNode(centerX + xR, centerY + yR,20,Color.GREEN);
                    }
                    circle.setId(String.valueOf(node.getAddress()));
                    ((Group)network_ui.getChildren().get(1)).getChildren().add(circle);

                    Text title = new Text(circle.getCenterX() + circle.getTranslateX(),
                            circle.getCenterY() + circle.getTranslateY(), String.valueOf(node.getAddress()));
                    ((Group)network_ui.getChildren().get(2)).getChildren().add(title);
                } else{
                    if (!node.getState().equals("Server")){
                        int xR = (int)((Math.random() - 1.0) * network_ui.getWidth() / 2);
                        int yR = (int)((Math.random() - 1.0) * network_ui.getHeight() / 2);
                        finded.setTranslateX(centerX + xR);
                        finded.setTranslateY(centerY + yR);
                        JFxNode circle = (JFxNode)finded;

                        Text title = new Text(circle.getCenterX() + circle.getTranslateX(),
                                circle.getCenterY() + circle.getTranslateY(), String.valueOf(node.getAddress()));
                        ((Group)network_ui.getChildren().get(2)).getChildren().add(title);
                    }
                }
            }

            ((Group)network_ui.getChildren().get(0)).getChildren().clear();

            for (WirelessNode node : network.getNodes()){
                if (node.getState().equals("Server")){ continue; }

                JFxNode circle = null;
                for (Node ui_node : ((Group)network_ui.getChildren().get(1)).getChildren()){
                    JFxNode tcircle = (JFxNode)ui_node;
                    if (String.valueOf(node.getAddress()).equals(tcircle.getId())){
                        circle = tcircle;
                        break;
                    }
                }

                JFxNode hopeCircle = null;
                for (WirelessNode.Route route : node.getRoutingTable()){
                    for (Node ui_node : ((Group)network_ui.getChildren().get(1)).getChildren()){
                        JFxNode tcircle = (JFxNode)ui_node;
                        if (String.valueOf(route.HopeAddress).equals(tcircle.getId())){
                            hopeCircle = tcircle;
                            break;
                        }
                    }

                    if (circle != null && hopeCircle != null){
                        Line line = new Line(circle.getCenterX() + circle.getTranslateX(),circle.getCenterY()+ circle.getTranslateY(),
                                hopeCircle.getCenterX() + hopeCircle.getTranslateX(),hopeCircle.getCenterY() + hopeCircle.getTranslateY());

                        ((Group)network_ui.getChildren().get(0)).getChildren().add(line);
                    }
                    hopeCircle = null;
                }
            }
        }
    }

    @Override
    public void onNetworkConnected() {

    }

    @Override
    public void onNetworkDisconnected() {

    }

    @Override
    public void onNetworkUpdated() {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                redrawNetUI();
            }
        });
    }

    @Override
    public void needSetNodeAddress(WirelessNode node) {
        configurationNode = node;
        showConfAlert();
    }
}
