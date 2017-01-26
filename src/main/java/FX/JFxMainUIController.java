package FX;

import Net.WirelessNode;
import UART.UARTConnector;
import UART.UARTConnectorDelegate;
import UART.UARTPackage;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.VBox;
import jssc.SerialPortList;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

import static UART.UARTGateProtocolEnum.PACKAGE_ECHO_STATE;

/**
 * Created by Jiro on 23.01.17.
 */
public class JFxMainUIController implements Initializable, UARTConnectorDelegate {
    @FXML
    private ChoiceBox<String> uartDevices;
    @FXML
    private Button connectButton;
    @FXML
    private Button findNodes;
    @FXML
    private TabPane tabPane;
    @FXML
    private Tab nodesTab;
    @FXML
    private VBox nodesTabContent;

    private UARTConnector uartConnector;
    private List<WirelessNode> nodes = new ArrayList<>();
    private boolean isConfAlertShown = false;

    void setUartConnector(UARTConnector uartConnector) {
        this.uartConnector = uartConnector;

        uartConnector.setDelegate(this);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        uartDevices.setOnMouseClicked(event -> {
            if (event.getButton().equals(MouseButton.PRIMARY)) {
                String[] portNames = SerialPortList.getPortNames();
                if (portNames.length > 0) {
                    uartDevices.setItems(FXCollections.observableArrayList(portNames));
                }
            }
        });

        findNodes.setDisable(true);
        findNodes.setOnMouseClicked(event -> updateNetwork());

        connectButton.setOnMouseClicked(event -> {
            switch (event.getButton()) {
                case PRIMARY: {
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
    }

    private void showConfAlert(UARTPackage uartPackage) {
        TextInputDialog dialog = new TextInputDialog("");
        dialog.setTitle("Node Address Configuration");
        dialog.setContentText("Please Enter Node Address less 65535");

        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            isConfAlertShown = false;
            System.out.println("configured");
            int address = Integer.valueOf(result.get());
            if (address >= 4 && address < 0xffff) {
                uartConnector.SetNodeNewAddress(address);
            }
        } else {
            isConfAlertShown = false;
            System.out.println("canceled");
        }
    }

    private void updateNetwork() {
        new Thread(() -> {
            for (int i = 10; i < 100; i++) {
                try {
                    Thread.sleep(20);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                uartConnector.GetNodeStatistic(i);
            }

            Platform.runLater(this::updateNodesTableUI);
        }).start();
    }

    private void updateNodesTableUI() {
        if (nodesTabContent.getChildren().size() > 1) {
            nodesTabContent.getChildren().remove(1, nodesTabContent.getChildren().size());
        }

        for (WirelessNode node : nodes) {
            Label label = new Label(node.address + " dB:" + node.dB);
            nodesTabContent.getChildren().add(label);
        }
    }

    private void updateNodeInfoInTable(UARTPackage uartPackage) {
        boolean found = false;

        for (WirelessNode node : nodes) {
            if (node.address == uartPackage.sourceAddress) {
                found = true;
                int value = 0;
                value += uartPackage.data[0] & 0xff;
//                        value += (uartPackage.data[1] << 8 & 0xff00);
                node.dB = value;
                break;
            }
        }

        if (!found) {
            WirelessNode newNode = new WirelessNode();
            newNode.address = uartPackage.sourceAddress;
            newNode.dB += uartPackage.data[0] & 0xff;
//                    newNode.dB += (uartPackage.data[1] << 8 & 0xff00);
            nodes.add(newNode);
        }
    }

    @Override
    public void OnConnectionClosed() {

    }

    @Override
    public void OnConnectionOpened() {

    }

    @Override
    public void OnConnectionDidRecivePackege(UARTPackage uartPackage) {
        Platform.runLater(() -> {
            if (uartPackage.type == 0xee && !isConfAlertShown) {
                isConfAlertShown = true;
                showConfAlert(uartPackage);
            } else if (uartPackage.type == PACKAGE_ECHO_STATE) {
                updateNodeInfoInTable(uartPackage);
                updateNodesTableUI();
            }
        });
    }

    @Override
    public void OnDebugMessageRecived(String message) {

    }
}
