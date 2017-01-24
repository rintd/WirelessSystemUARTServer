package FX; /**
 * Created by Jiro on 23.01.17.
 */

import Net.WirelessNode;
import UART.UARTConnector;
import UART.UARTConnectorDelegate;
import UART.UARTPackage;
import com.sun.javafx.scene.control.skin.LabeledText;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import jssc.SerialPortList;

import java.util.ArrayList;
import java.util.Optional;

import static UART.UARTGateProtocolEnum.*;

public class JFxUIMain extends Application implements UARTConnectorDelegate {
    public VBox vbox;
    public UARTConnector uartConnector = null;

    private boolean isConfAlertShown = false;
    private Scene scene = null;

    private ArrayList<WirelessNode> nodes;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        uartConnector = new UARTConnector(this);
        nodes = new ArrayList<>();

        vbox = FXMLLoader.load(getClass().getResource("../fxinterface.fxml"));
        scene = new Scene(vbox, 600, 480);

        primaryStage.setTitle("RINTD Wireless System");
        primaryStage.setScene(scene);
        primaryStage.show();

        ChoiceBox choiceBox = (ChoiceBox) scene.lookup("#uart_devices");
        choiceBox.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if (event.getButton().equals(MouseButton.PRIMARY)){
                    String[] portNames = SerialPortList.getPortNames();
                    if(portNames.length>0){
                        choiceBox.setItems(FXCollections.observableArrayList(portNames));
                    }
                }
            }
        });

        Button findNodes = (Button) scene.lookup("#find_nodes");
        findNodes.setDisable(true);
        findNodes.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if(uartConnector != null){
                    JFxUIMain.this.UpdateNetwork();
                }
            }
        });
        Button connectButton = (Button) scene.lookup("#connect_button");

        connectButton.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                switch (event.getButton()) {
                    case PRIMARY: {
                        if(connectButton.getText().equals("connect")){
                            ChoiceBox choiceBox = (ChoiceBox) scene.lookup("#uart_devices");
                            String s = (String)choiceBox.getSelectionModel().getSelectedItem();

                            uartConnector.Init(s);
                            if(uartConnector.Connect()){
                                choiceBox.setDisable(true);
                                findNodes.setDisable(false);
                                connectButton.setText("disconnect");
                            }
                        } else if(uartConnector.IsConnected()){
                            if(uartConnector.Disconnect()){
                                choiceBox.setDisable(false);
                                findNodes.setDisable(true);
                                connectButton.setText("connect");
                            }
                        }
                    }
                    break;

                    default: {

                    }
                    break;
                }
            }
        });

        TabPane TabPanel = (TabPane)scene.lookup("#tab_panel");

        Tab nodesTab = new Tab();
        nodesTab.setText("Nodes");

        VBox vBox = new VBox();
        vBox.getChildren().add(new Label("nodes will be Here..."));
        vBox.setAlignment(Pos.TOP_CENTER);
        nodesTab.setContent(vBox);
        TabPanel.getTabs().add(nodesTab);
    }

    private  void ShowConfAlert (UARTPackage uartPackage){

        Platform.runLater(new Runnable() {
            @Override public void run() {

                TextInputDialog dialog = new TextInputDialog("");
                dialog.setTitle("Node Address Configuration");
                dialog.setContentText("Please Enter Node Address less 65535");

                Optional<String> result = dialog.showAndWait();
                if (result.isPresent()){
                    isConfAlertShown = false;
                    System.out.println("configured");
                    int address = Integer.valueOf(result.get()).intValue();
                    if (address >= 4 && address < 0xffff){

                        JFxUIMain.this.uartConnector.SetNodeNewAddress(address);
                    }
                } else {
                    isConfAlertShown = false;
                    System.out.println("canceled");
                }
            }
        });
    }

    private void UpdateNetwork (){
        for (int i = 10; i<100; i++){
            try {
                Thread.sleep(20);
                uartConnector.GetNodeStatistic(i);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        UpdateNodesTableUI();
    }

    private void UpdateNodesTableUI (){
        Platform.runLater(new Runnable() {
            @Override public void run() {
                TabPane TabPanel = (TabPane)JFxUIMain.this.scene.lookup("#tab_panel");
                Tab fst = TabPanel .getTabs().get(0);
                VBox fstVBox = (VBox) fst.getContent();

                if(fstVBox.getChildren().size() > 1){
                    fstVBox.getChildren().remove(1,fstVBox.getChildren().size());
                }

                for (WirelessNode node: nodes){
                    Label label = new Label(node.address + " dB:" + node.dB);
                    fstVBox.getChildren().add(label);
                }
            }
        });

    }
    private void UpdateNodeInfoInTable (UARTPackage uartPackage){
        Platform.runLater(new Runnable() {
            @Override public void run() {

                boolean finded = false;

                for (WirelessNode node : nodes){
                    if (node.address == uartPackage.sourceAddress) {
                        finded = true;
                        int value = 0;
                        value += uartPackage.data[0] & 0xff;
//                        value += (uartPackage.data[1] << 8 & 0xff00);
                        node.dB = value;
                        break;
                    }
                }

                if(!finded){
                    WirelessNode newNode = new WirelessNode();
                    newNode.address = uartPackage.sourceAddress;
                    newNode.dB += uartPackage.data[0] & 0xff;
//                    newNode.dB += (uartPackage.data[1] << 8 & 0xff00);
                    nodes.add(newNode);
                }
            }
        });
    }


    @Override
    public void OnConnectionClosed() {

    }

    @Override
    public void OnConnectionOpened() {

    }

    @Override
    public void OnConnectionDidRecivePackege(UARTPackage uartPackage) {
        if(uartPackage.type == 0xee && !isConfAlertShown){
            isConfAlertShown = true;
            try {
                JFxUIMain.this.ShowConfAlert(uartPackage);
            } catch (Exception e) {

            }
        } else if (uartPackage.type == PACKAGE_ECHO_STATE){
            try {
                JFxUIMain.this.UpdateNodeInfoInTable(uartPackage);
                JFxUIMain.this.UpdateNodesTableUI();
            } catch (Exception e) {

            }
        }
    }

    @Override
    public void OnDebugMessageRecived(String message) {

    }


}