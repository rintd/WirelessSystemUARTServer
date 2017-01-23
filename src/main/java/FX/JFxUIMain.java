package FX; /**
 * Created by Jiro on 23.01.17.
 */

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextInputDialog;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.Optional;

public class JFxUIMain extends Application {

    public TabPane TabPanel;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {

        TabPanel = FXMLLoader.load(getClass().getResource("../fxinterface.fxml"));

        Scene scene = new Scene(TabPanel, 600, 480);

        Tab nodesTab = new Tab();
        nodesTab.setText("Nodes");
        VBox vBox = new VBox();
        vBox.getChildren().add(new Label("nodes will be Here..."));
        vBox.setAlignment(Pos.TOP_CENTER);
        nodesTab.setContent(vBox);

        TabPanel.getTabs().add(nodesTab);

        primaryStage.setTitle("FXML Welcome");
        primaryStage.setScene(scene);
        primaryStage.show();

        TextInputDialog dialog = new TextInputDialog("");
        dialog.setTitle("Node Address Configuration");
        dialog.setContentText("Please Enter Node Address less 65535");

        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()){
            this.addNodeInList(result.get());
        }
    }

    public void addNodeInList (String nodeName){
        System.out.println("Node added: " + nodeName);
        Tab fst = TabPanel.getTabs().get(0);
        VBox fstVBox =  (VBox)fst.getContent();

        Label label = new Label(nodeName);
        label.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if (event.getButton().equals(MouseButton.PRIMARY)){
                    //TODO: диалог для выбора события нода, пока только лог его

                    Label l = (Label)event.getTarget();
                    System.out.println("Clicked" + l.getText());
                }
            }
        });
        fstVBox.getChildren().add(label);
    }

}
