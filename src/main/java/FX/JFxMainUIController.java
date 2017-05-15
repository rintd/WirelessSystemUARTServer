package FX;

import FX.SystemUI.WirelessRoom;
import FX.SystemUI.WirelessSensor;
import Net.WirelessNode;
import UART.UARTConnector;
import UART.UARTConnectorDelegate;
import UART.UARTPackage;
import com.sun.xml.internal.ws.api.streaming.XMLStreamReaderFactory;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Group;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import jssc.SerialPortList;

import java.net.URL;
import java.util.*;

import static UART.UARTGateProtocolEnum.PACKAGE_ALARM;
import static UART.UARTGateProtocolEnum.PACKAGE_ECHO_STATE;
import static javafx.scene.input.MouseEvent.MOUSE_CLICKED;

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
    @FXML
    private Pane canvas;

    @FXML
    private Group root;

    @FXML
    private CheckBox demoModeCheck;

    @FXML
    private Label netLog;

    List<Shape> rooms = new ArrayList<>();
    List<Shape> sensors = new ArrayList<>();

    List<Shape> doors = new ArrayList<>();
    List<Shape> arrows = new ArrayList<>();


    private UARTConnector uartConnector;
    private List<WirelessNode> nodes = new ArrayList<>();
    private boolean isConfAlertShown = false;
    private boolean requiredSetNodeAddress = false;

    private Thread demoThread = null;
    boolean isDemoMode = false;
    boolean isAppWorking = true;

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

        demoModeCheck.setSelected(isDemoMode);
        demoModeCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                isDemoMode = newValue;
                if(!isDemoMode){
                    JFxMainUIController.this.showAlarmAlert(0,-1);
                } else {
                    new Thread(() -> {
                        while (isDemoMode){
                            try {
                               System.out.print("someTrash");
                               Platform.runLater(JFxMainUIController.this::DemoEmergencyMode);
                               Thread.sleep(5000);
                            } catch (InterruptedException ex){
                                Thread.currentThread().interrupt();
                            }
                        }
                    }).start();
                }
            }
        });

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


//        Circle c = new Circle(100, 100, 10, Color.AQUAMARINE);
//        root.getChildren().add(c);

        this.drawBuilding();
    }

    private void drawBuilding(){
        root.getChildren().remove(0, root.getChildren().size());

        Color fillColor = Color.WHITE;
        Color strokeColor = Color.BLACK;
        double factor = 4;
        double xOffset = 250;
        double yOffset = 80;
        double strokeWidth = 4.0;

        //0
        WirelessRoom r = new WirelessRoom(50 * factor, 50 * factor, fillColor);
        r.setX(xOffset);
        r.setY(yOffset);
        r.setStroke(strokeColor);
        r.setStrokeWidth(strokeWidth);
        rooms.add(r);

        //1
        r = new WirelessRoom(50 * factor, 50 * factor, fillColor);
        r.setX(xOffset + 50 * factor);
        r.setY(yOffset);
        r.setStroke(strokeColor);
        r.setStrokeWidth(strokeWidth);
        rooms.add(r);

        //2
        r = new WirelessRoom(25 * factor, 50 * factor, fillColor);
        r.setX(xOffset + 100 * factor);
        r.setY(yOffset);
        r.setStroke(strokeColor);
        r.setStrokeWidth(strokeWidth);
        rooms.add(r);

        //3
        r = new WirelessRoom(50 * factor, 30 * factor, fillColor);
        r.setX(xOffset + 125 * factor);
        r.setY(yOffset);
        r.setStroke(strokeColor);
        r.setStrokeWidth(strokeWidth);
        rooms.add(r);

        //4
        r = new WirelessRoom(25 * factor, 20 * factor, fillColor);
        r.setX(xOffset + 125 * factor);
        r.setY(yOffset + 30  * factor);
        r.setStroke(strokeColor);
        r.setStrokeWidth(strokeWidth);
        rooms.add(r);

        //5
        r = new WirelessRoom(25 * factor, 20 * factor, fillColor);
        r.setX(xOffset + 150 * factor);
        r.setY(yOffset + 30  * factor);
        r.setStroke(strokeColor);
        r.setStrokeWidth(strokeWidth);
        rooms.add(r);

        //6
        r = new WirelessRoom(70 * factor, 40 * factor, fillColor);
        r.setX(xOffset);
        r.setY(yOffset + 50  * factor);
        r.setStroke(strokeColor);
        r.setStrokeWidth(strokeWidth);
        rooms.add(r);

        //7
        r = new WirelessRoom(70 * factor, 40 * factor, fillColor);
        r.setX(xOffset);
        r.setY(yOffset + 90  * factor);
        r.setStroke(strokeColor);
        r.setStrokeWidth(strokeWidth);
        rooms.add(r);

        //Draw
        for (Shape p : rooms){
            root.getChildren().add(p);
        }

        for (int i = 0; i < rooms.size(); i++){
            WirelessRoom   rectangle = (WirelessRoom)rooms.get(i);
            WirelessSensor sensor = new WirelessSensor(rectangle.getX() + rectangle.getWidth() / 2.0, rectangle.getY() + rectangle.getHeight() / 2.0, 10, Color.GREEN);
            sensor.sensor_id = i + 4;
            rectangle.sensor = sensor;
            sensors.add(sensor);
            root.getChildren().add(sensor);

            sensor.setOnMouseClicked(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {
                    if (event.getEventType() == MOUSE_CLICKED && event.getButton() == MouseButton.PRIMARY) {
                        if(requiredSetNodeAddress && !sensor.isInited && uartConnector.IsConnected()){
                            uartConnector.SetNodeNewAddress(sensor.sensor_id);

                            if (requiredSetNodeAddress){
                                requiredSetNodeAddress = false;
                                netLog.setText("Информация:");
                                netLog.setTextFill(Color.BLACK);
                            }
                        } else {
                            if(uartConnector.IsConnected())
                                uartConnector.GetNodeStatistic(sensor.sensor_id);
                        }
                    }
                }
            });

//            c.setOnMouseClicked(new EventHandler<MouseEvent>() {
//                @Override
//                public void handle(MouseEvent event) {
//                    //TODO: get log
//                    if (event.getEventType() == MOUSE_CLICKED && event.getButton() == MouseButton.MIDDLE) {
//                        System.out.println("disable");
//                        if(JFxMainUIController.this.uartConnector != null){
//                            JFxMainUIController.this.uartConnector.SetArrowDisable(13);
//                        }
//
//                    } else if (event.getEventType() == MOUSE_CLICKED && event.getButton() == MouseButton.PRIMARY) {
//                        System.out.println("primary");
//                        if(JFxMainUIController.this.uartConnector != null) {
//                            JFxMainUIController.this.uartConnector.SetArrowRight(13);
//                        }
//                    } else if (event.getEventType() == MOUSE_CLICKED && event.getButton() == MouseButton.SECONDARY) {
//                        System.out.println("secondary");
//                        if(JFxMainUIController.this.uartConnector != null) {
//                            JFxMainUIController.this.uartConnector.SetArrowLeft(13);
//                        }
//                    }
//                }
//            });
        }
    }


    private void showConfAlert(UARTPackage uartPackage) {
        isConfAlertShown = true;
        requiredSetNodeAddress = true;

        netLog.setText("Необходимо установить адрес узла");
        netLog.setTextFill(Color.GREEN);
//        TextInputDialog dialog = new TextInputDialog("");
//        dialog.setTitle("Node Address Configuration");
//        dialog.setContentText("Please Enter Node Address less 65535");
//
//        Optional<String> result = dialog.showAndWait();
//        if (result.isPresent()) {
//            isConfAlertShown = false;
//            System.out.println("configured");
//            int address = Integer.valueOf(result.get());
//            if (address >= 4 && address < 0xffff) {
//                uartConnector.SetNodeNewAddress(address);
//            }
//        } else {
//            isConfAlertShown = false;
//            System.out.println("canceled");
//        }

    }

    private void DemoEmergencyMode(){
        Random r = new Random();
        this.showAlarmAlert(0,(r.nextInt(8) + 4));
    }

    private void EmergencyOnNode(int nodeAddress) {
        this.ClearSystemUI();

        if(nodeAddress > 0){
            for (int  i = 0; i < rooms.size(); i++){
                WirelessRoom room  = (WirelessRoom)rooms.get(i);
                if(room.sensor.sensor_id == nodeAddress){
                    room.sensor.setFill(Color.RED);
                    room.setFill(Color.CORAL);
                    break;
                }
            }
            //TODO:calc arrow and show and send to arrow nods

            try {
                switch (nodeAddress){
                    case 4: {
                        uartConnector.SetArrowLeft(21);
                        Thread.sleep(20);
                        uartConnector.SetArrowRight(22);
                        Thread.sleep(20);
                        uartConnector.SetArrowRight(23);
                        Thread.sleep(20);
                        uartConnector.SetArrowRight(24);
                        Thread.sleep(20);
                        uartConnector.SetArrowRight(25);
                        Thread.sleep(20);
                    } break;

                    case 5: {
                        uartConnector.SetArrowLeft(21);
                        Thread.sleep(20);
                        uartConnector.SetArrowRight(22);
                        Thread.sleep(20);
                        uartConnector.SetArrowLeft(23);
                        Thread.sleep(20);
                        uartConnector.SetArrowRight(24);
                        Thread.sleep(20);
                        uartConnector.SetArrowRight(25);
                        Thread.sleep(20);
                    } break;

                    case 6: {
                        uartConnector.SetArrowLeft(21);
                        Thread.sleep(20);
                        uartConnector.SetArrowRight(22);
                        Thread.sleep(20);
                        uartConnector.SetArrowLeft(23);
                        Thread.sleep(20);
                        uartConnector.SetArrowRight(24);
                        Thread.sleep(20);
                        uartConnector.SetArrowRight(25);
                        Thread.sleep(20);
                    } break;

                    case 7: {
                        uartConnector.SetArrowLeft(21);
                        Thread.sleep(20);
                        uartConnector.SetArrowRight(22);
                        Thread.sleep(20);
                        uartConnector.SetArrowLeft(23);
                        Thread.sleep(20);
                        uartConnector.SetArrowLeft(24);
                        Thread.sleep(20);
                        uartConnector.SetArrowRight(25);
                        Thread.sleep(20);
                    } break;

                    case 8: {
                        uartConnector.SetArrowLeft(21);
                        Thread.sleep(20);
                        uartConnector.SetArrowRight(22);
                        Thread.sleep(20);
                        uartConnector.SetArrowLeft(23);
                        Thread.sleep(20);
                        uartConnector.SetArrowLeft(24);
                        Thread.sleep(20);
                        uartConnector.SetArrowRight(25);
                        Thread.sleep(20);
                    } break;

                    case 9: {
                        uartConnector.SetArrowLeft(21);
                        Thread.sleep(20);
                        uartConnector.SetArrowRight(22);
                        Thread.sleep(20);
                        uartConnector.SetArrowLeft(23);
                        Thread.sleep(20);
                        uartConnector.SetArrowLeft(24);
                        Thread.sleep(20);
                        uartConnector.SetArrowLeft(25);
                        Thread.sleep(20);
                    }break;

                    case 10: {
                        uartConnector.SetArrowLeft(21);
                        Thread.sleep(20);
                        uartConnector.SetArrowRight(22);
                        Thread.sleep(20);
                        uartConnector.SetArrowLeft(23);
                        Thread.sleep(20);
                        uartConnector.SetArrowRight(24);
                        Thread.sleep(20);
                        uartConnector.SetArrowRight(25);
                        Thread.sleep(20);
                    } break;

                    case 11: {
                        uartConnector.SetArrowRight(21);
                        Thread.sleep(20);
                        uartConnector.SetArrowRight(22);
                        Thread.sleep(20);
                        uartConnector.SetArrowLeft(23);
                        Thread.sleep(20);
                        uartConnector.SetArrowRight(24);
                        Thread.sleep(20);
                        uartConnector.SetArrowRight(25);
                        Thread.sleep(20);
                    } break;

                    default: {

                    } break;
                }
            } catch (InterruptedException ex ){

            }
        } else {
            try {
                uartConnector.SetArrowDisable(21);
                Thread.sleep(20);
                uartConnector.SetArrowDisable(22);
                Thread.sleep(20);
                uartConnector.SetArrowDisable(23);
                Thread.sleep(20);
                uartConnector.SetArrowDisable(24);
                Thread.sleep(20);
                uartConnector.SetArrowDisable(25);
                Thread.sleep(20);
            } catch (InterruptedException ex ){

            }
        }
    }

    private void ClearSystemUI(){
        for (int i = 0; i<rooms.size(); i++){
            WirelessRoom r = (WirelessRoom)rooms.get(i);
            r.sensor.setFill(Color.GREEN);
            r.setFill(Color.WHITE);
        }
    }


    private void showAlarmAlert (int type,int nodeAddress){
        this.EmergencyOnNode(nodeAddress);
//
//        Alert alert = new Alert(Alert.AlertType.WARNING);
//        alert.setTitle("Опасность в комнате №" + (nodeAddress - 4));
        String typeString = "";
        switch (type){
            case 0: {
                typeString = "Пожарная угроза";
            } break;

            default: {
                typeString = "Неизвестная угроза";
            }break;
        }

        if(nodeAddress>=0){
            netLog.setTextFill(Color.RED);
            netLog.setText("Внимание, в комнате №" + (nodeAddress - 4) + " " + typeString);
        } else {
            netLog.setTextFill(Color.BLACK);
            netLog.setText("Информация");
        }

//        alert.setContentText("Внимание, в здании " + typeString);
//        alert.showAndWait();
    }

    private void updateNetwork() {
        new Thread(() -> {
            for (int i = 0; i<rooms.size(); i++) {
                WirelessRoom room = (WirelessRoom) rooms.get(i);
                try {
                    Thread.sleep(20);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                if(uartConnector.IsConnected())
                    uartConnector.GetNodeStatistic(room.sensor.sensor_id);
            }
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
            if (uartPackage.type == 0xee) {
                if(!isDemoMode) {
                    showConfAlert(uartPackage);
                }
            } else if (uartPackage.type == PACKAGE_ECHO_STATE) {
                for (int i = 0; i<rooms.size(); i++) {
                    WirelessRoom room = (WirelessRoom)rooms.get(i);
                    if(room.sensor.sensor_id == uartPackage.sourceAddress) {
                        room.sensor.isInited = true;
                    }
                }
                updateNodeInfoInTable(uartPackage);
                updateNodesTableUI();
            } else if(uartPackage.type == PACKAGE_ALARM){
                if(isDemoMode) {
                    isDemoMode = false;
                }

                showAlarmAlert(0, uartPackage.sourceAddress);
            }
        });
    }

    @Override
    public void OnDebugMessageRecived(String message) {

    }
}
