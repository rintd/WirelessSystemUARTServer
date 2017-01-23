package FX; /**
 * Created by Jiro on 23.01.17.
 */

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import jssc.SerialPortList;

import java.util.Optional;

public class JFxUIMain extends Application {
    public VBox vbox;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {

        vbox = FXMLLoader.load(getClass().getResource("../fxinterface.fxml"));

        Scene scene = new Scene(vbox, 600, 480);

        primaryStage.setTitle("RINTD Wireless System");
        primaryStage.setScene(scene);
        primaryStage.show();


        Button resetButton = (Button) scene.lookup("#scan_button");

        resetButton.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                switch (event.getButton()) {
                    case PRIMARY: {
                        ChoiceBox choiceBox = (ChoiceBox) scene.lookup("#uart_devices");

                        String[] portNames = SerialPortList.getPortNames();
                        choiceBox.setItems(FXCollections.observableArrayList(portNames));
                    }
                    break;

                    default: {

                    }
                    break;
                }
            }
        });

        Button connectButton = (Button) scene.lookup("#connect_button");

        connectButton.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                switch (event.getButton()) {
                    case PRIMARY: {
                        //TODO: сделать подключение
                        ChoiceBox choiceBox = (ChoiceBox) scene.lookup("#uart_devices");
//                        choiceBox.getSelectionModel().getSelectedIndex()
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

//        TextInputDialog dialog = new TextInputDialog("");
//        dialog.setTitle("Node Address Configuration");
//        dialog.setContentText("Please Enter Node Address less 65535");
//
//        Optional<String> result = dialog.showAndWait();
//        if (result.isPresent()){
//            this.addNodeInList(result.get());
//        }
    }

    public void addNodeInList (TabPane tabPanel,String nodeName) {
        System.out.println("Node added: " + nodeName);
        Tab fst = tabPanel.getTabs().get(0);
        VBox fstVBox = (VBox) fst.getContent();

        Label label = new Label(nodeName);
        label.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if (event.getButton().equals(MouseButton.PRIMARY)) {
                    //TODO: диалог для выбора события нода, пока только лог его

                    Label l = (Label) event.getTarget();
                    System.out.println("Clicked" + l.getText());
                }
            }
        });
        fstVBox.getChildren().add(label);
    }
}
